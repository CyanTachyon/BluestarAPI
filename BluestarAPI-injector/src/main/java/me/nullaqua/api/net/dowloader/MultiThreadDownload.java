package me.nullaqua.api.net.dowloader;

import me.nullaqua.api.collection.FastLinkedList;
import me.nullaqua.api.util.quantity.DataRate;
import me.nullaqua.api.util.quantity.DataSize;
import me.nullaqua.api.util.quantity.Time;
import me.nullaqua.api.util.quantity.unit.DataSizeUnit;
import me.nullaqua.api.util.quantity.unit.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class MultiThreadDownload
{
    private final String serverPath;
    private final String localPath;
    private MultiThreadDownloader downloader;

    public MultiThreadDownload(String serverPath,String localPath)
    {
        this.serverPath=serverPath;
        this.localPath=localPath;
    }

    public static MultiThreadDownloader download(int threadCount,String serverPath,String localPath) throws IOException
    {
        return download(threadCount,serverPath,localPath,-1);
    }

    public static MultiThreadDownloader download(int threadCount,String serverPath,String localPath,int timeOut) throws IOException
    {
        MultiThreadDownload downloader=new MultiThreadDownload(serverPath,localPath);
        long length=downloader.getLength(timeOut);
        if (length<=0)
        {
            throw new IOException("Could not get the length of the file");
        }
        downloader.downloader=new MultiThreadDownloader(threadCount,length,localPath);
        long startIndex=0;
        for (int threadId=0;threadId<threadCount;threadId++)
        {
            long blockSize=(length-startIndex)/(threadCount-threadId);
            long endIndex=startIndex+blockSize-1;
            var thread=downloader.new DownloadThread(threadId,startIndex,endIndex,timeOut);
            startIndex=endIndex+1;
            downloader.downloader.addThread(thread,threadId,blockSize);
            thread.start();
        }
        return downloader.downloader;
    }

    private long getLength(int timeOut) throws IOException
    {
        URL url=new URL(serverPath);
        var conn=url.openConnection();
        if (timeOut>0)
        {
            conn.setConnectTimeout(timeOut);
            conn.setReadTimeout(timeOut);
        }
        long length=conn.getContentLengthLong();
        RandomAccessFile raf=new RandomAccessFile(localPath,"rwd");
        raf.setLength(length);
        raf.close();
        return length;
    }

    public static class MultiThreadDownloader extends Downloader
    {
        private final DownloadThread[] threads;
        private final FastLinkedList<Map.Entry<Long,Long>>[] threadSpeedData;
        private final long[] threadSpeed;
        private final long[] threadCurrentSize;
        private final long[] threadTotalSize;
        private final int totalThread;
        private int currentThread;

        private MultiThreadDownloader(int totalThread,long totalSize,String fileName)
        {
            super(totalSize,fileName);
            this.totalThread=totalThread;
            this.threads=new DownloadThread[totalThread];
            this.threadCurrentSize=new long[totalThread];
            this.threadTotalSize=new long[totalThread];
            this.threadSpeedData=new FastLinkedList[totalThread];
            for (int i=0;i<totalThread;i++)
            {
                threadSpeedData[i]=new FastLinkedList<>();
            }
            currentThread=totalThread;
            threadSpeed=new long[totalThread];
        }

        public int totalThread()
        {
            return totalThread;
        }

        public int currentThread()
        {
            return currentThread;
        }

        public DataSize totalSize(int id)
        {
            return new DataSize(threadTotalSize[id],DataSizeUnit.B);
        }

        public DataSize currentSize(int id)
        {
            return new DataSize(threadCurrentSize[id],DataSizeUnit.B);
        }

        private void finish(int threadId)
        {
            synchronized (this)
            {
                threads[threadId]=null;
                currentThread--;
                if (currentThread==0)
                {
                    super.setSuccess();
                }
            }
        }

        private void addThread(DownloadThread thread,int threadId,long length)
        {
            synchronized (this)
            {
                threads[threadId]=thread;
                threadTotalSize[threadId]=length;
            }
        }

        private void _error(Throwable e)
        {
            synchronized (this)
            {
                for (var thread: threads)
                {
                    if (thread!=null)
                    {
                        if (thread.conn!=null)
                        {
                            try
                            {
                                thread.conn.getInputStream().close();
                                thread.conn.getOutputStream().close();
                            }
                            catch (Throwable ignored)
                            {
                            }
                        }
                        thread.interrupt();
                    }
                }
                super.error(e);
            }
        }

        private void add(int threadId,int size)
        {
            synchronized (this)
            {
                threadCurrentSize[threadId]+=size;
                threadSpeedData[threadId].add(Map.entry(System.currentTimeMillis(),(long) size));
                threadSpeed[threadId]+=size;
                super.add(size);
                update(threadId);
            }
        }

        private void update(int id)
        {
            long time=System.currentTimeMillis();
            while (!threadSpeedData[id].isEmpty()&&time-threadSpeedData[id].getFirst().getKey()>1000)
            {
                threadSpeed[id]-=threadSpeedData[id].getFirst().getValue();
                threadSpeedData[id].removeFirst();
            }
        }

        public final double progressPercent(int id)
        {
            return progress(id)*100.0;
        }

        public final double progress(int id)
        {
            return threadCurrentSize[id]*1.0/threadTotalSize[id];
        }

        public final Time remainingTime(int id)
        {
            return new Time(waitSize(id).size()/speed(id).rate(DataSizeUnit.B,TimeUnit.s),TimeUnit.s);
        }

        public final DataSize waitSize(int id)
        {
            return new DataSize(threadTotalSize[id]-threadCurrentSize[id],DataSizeUnit.B);
        }

        public DataRate speed(int id)
        {
            synchronized (this)
            {
                return new DataRate(threadSpeed[id],DataSizeUnit.B,TimeUnit.s);
            }
        }
    }

    private class DownloadThread extends Thread
    {
        private final long startIndex;
        private final long endIndex;
        private final int threadId;
        private final int timeOut;
        private URLConnection conn;

        public DownloadThread(int threadId,long startIndex,long endIndex,int timeOut)
        {
            this.threadId=threadId;
            this.startIndex=startIndex;
            this.endIndex=endIndex;
            this.timeOut=timeOut;
        }


        @Override
        public void run()
        {
            try (var raf=new RandomAccessFile(localPath,"rwd"))
            {
                URL url=new URL(serverPath);
                conn=url.openConnection();
                conn.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);
                if (timeOut>0)
                {
                    conn.setConnectTimeout(timeOut);
                    conn.setReadTimeout(timeOut);
                }

                InputStream is=conn.getInputStream();
                raf.seek(startIndex);

                DefaultDownload.copy(is,new OutputStream()
                {
                    @Override
                    public void write(byte @NotNull [] b) throws IOException
                    {
                        raf.write(b);
                    }

                    @Override
                    public void write(byte @NotNull [] b,int off,int len) throws IOException
                    {
                        raf.write(b,off,len);
                    }

                    @Override
                    public void write(int b) throws IOException
                    {
                        raf.write(b);
                    }
                },x->downloader.add(threadId,(int) (long) x),downloader);
                if (downloader.status()==Downloader.Status.Cancel||downloader.status()==Downloader.Status.Error)
                {
                    raf.close();
                    new File(localPath).delete();
                }
            }
            catch (Throwable e)
            {
                downloader._error(e);
                return;
            }
            downloader.finish(threadId);
        }
    }
}