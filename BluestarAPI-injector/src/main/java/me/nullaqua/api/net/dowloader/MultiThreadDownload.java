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
import java.util.List;
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
            var thread=downloader.new DownloadThread(threadId,startIndex,endIndex,timeOut,blockSize);
            startIndex=endIndex+1;
            downloader.downloader.addThread(thread,threadId);
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
        private final int totalThread;
        private int currentThread;

        private MultiThreadDownloader(int totalThread,long totalSize,String fileName)
        {
            super(totalSize,fileName);
            this.totalThread=totalThread;
            this.threads=new DownloadThread[totalThread];
            currentThread=totalThread;
        }

        public int totalThread()
        {
            return totalThread;
        }

        public int currentThread()
        {
            return currentThread;
        }

        @Deprecated
        public DataSize totalSize(int id)
        {
            return threads[id].totalSize();
        }

        @Deprecated
        public DataSize currentSize(int id)
        {
            return threads[id].currentSize();
        }

        private void finish(int threadId)
        {
            synchronized (this)
            {
                threads[threadId]=null;
                currentThread--;
                if (currentThread==0) super.setSuccess();
            }
        }

        private void addThread(DownloadThread thread,int threadId)
        {
            synchronized (this)
            {
                threads[threadId]=thread;
            }
        }

        private void _error(Throwable e)
        {
            synchronized (this)
            {
                super.error(e);
                for (var thread: threads)
                {
                    if (thread==null) continue;
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
        }

        @Deprecated
        public final double progressPercent(int id)
        {
            synchronized (threads[id])
            {
                return threads[id].progressPercent();
            }
        }

        @Deprecated
        public final double progress(int id)
        {
            synchronized (threads[id])
            {
                return threads[id].progress();
            }
        }

        @Deprecated
        public final Time remainingTime(int id)
        {
            synchronized (threads[id])
            {
                return threads[id].remainingTime();
            }
        }

        @Deprecated
        public final DataSize waitSize(int id)
        {
            synchronized (threads[id])
            {
                return threads[id].waitSize();
            }
        }

        @Deprecated
        public DataRate speed(int id)
        {
            synchronized (threads[id])
            {
                return threads[id].speed();
            }
        }
    }

    private class DownloadThread extends Thread
    {
        private final long startIndex;
        private final long endIndex;
        private final int threadId;
        private final int timeOut;
        private final FastLinkedList<Map.Entry<Long,Long>> threadSpeedData=new FastLinkedList<>();
        private final long totalSize;
        private long currentSize=0;
        private long speed=0;
        private URLConnection conn;

        public DownloadThread(int threadId,long startIndex,long endIndex,int timeOut,long totalSize)
        {
            this.threadId=threadId;
            this.startIndex=startIndex;
            this.endIndex=endIndex;
            this.timeOut=timeOut;
            this.totalSize=totalSize;
        }

        private void add(long size)
        {
            currentSize+=size;
            threadSpeedData.add(Map.entry(System.currentTimeMillis(),size));
            speed+=size;
            downloader.add(size);
            update();
        }

        private void update()
        {
            long time=System.currentTimeMillis();
            while (!threadSpeedData.isEmpty()&&time-threadSpeedData.getFirst().getKey()>1000)
            {
                speed-=threadSpeedData.getFirst().getValue();
                threadSpeedData.removeFirst();
            }
        }


        public DataRate speed()
        {
            return new DataRate(speed,DataSizeUnit.B,TimeUnit.s);
        }

        public DataSize currentSize()
        {
            return new DataSize(currentSize,DataSizeUnit.B);
        }

        public DataSize totalSize()
        {
            return new DataSize(totalSize,DataSizeUnit.B);
        }

        public final double progressPercent()
        {
            synchronized (this)
            {
                return (double) currentSize/totalSize*100;
            }
        }

        public final double progress()
        {
            synchronized (this)
            {
                return (double) currentSize/totalSize;
            }
        }

        public final Time remainingTime()
        {
            synchronized (this)
            {
                return new Time((double) waitSize().size()/speed().rate(DataSizeUnit.B,TimeUnit.s),TimeUnit.s);
            }
        }

        public final DataSize waitSize()
        {
            synchronized (this)
            {
                return new DataSize(totalSize-currentSize,DataSizeUnit.B);
            }
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
                },this::add,downloader);
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