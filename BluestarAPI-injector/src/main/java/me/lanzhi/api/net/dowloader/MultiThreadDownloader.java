package me.lanzhi.api.net.dowloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiThreadDownloader
{
    private final String serverPath;
    private final String localPath;
    private MultiThreadDownloadProgressManager progressManager;

    public MultiThreadDownloader(String serverPath,String localPath)
    {
        this.serverPath=serverPath;
        this.localPath=localPath;
    }

    public static MultiThreadDownloadProgressManager download(int threadCount,String serverPath,String localPath) throws IOException
    {
        MultiThreadDownloader downloader=new MultiThreadDownloader(serverPath,localPath);
        long length=downloader.getLength();
        if (length<=0)
        {
            throw new IOException("Could not get the length of the file");
        }
        downloader.progressManager=new MultiThreadDownloadProgressManager(threadCount,length);
        long startIndex=0;
        for (int threadId=0;threadId<threadCount;threadId++)
        {
            long blockSize=(length-startIndex)/(threadCount-threadId);
            long endIndex=startIndex+blockSize-1;
            Thread thread=downloader.new DownloadThread(threadId,startIndex,endIndex);
            startIndex=endIndex+1;
            downloader.progressManager.addThread(thread,threadId,blockSize);
            thread.start();
        }
        return downloader.progressManager;
    }

    private long getLength() throws IOException
    {
        URL url=new URL(serverPath);
        HttpURLConnection conn=(HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        int code=conn.getResponseCode();
        if (code==200)
        {
            long length=conn.getContentLengthLong();
            RandomAccessFile raf=new RandomAccessFile(localPath,"rwd");
            raf.setLength(length);
            raf.close();
            return length;
        }
        return -1;
    }

    public static class MultiThreadDownloadProgressManager extends DownloadProgressManager
    {
        private final Thread[] threads;
        private final long[] threadCurrentSize;
        private final long[] threadTotalSize;
        private final int totalThread;
        private int currentThread;

        private MultiThreadDownloadProgressManager(int totalThread,long totalSize)
        {
            super(totalSize);
            this.totalThread=totalThread;
            this.threads=new Thread[totalThread];
            this.threadCurrentSize=new long[totalThread];
            this.threadTotalSize=new long[totalThread];
            currentThread=totalThread;
        }

        public int getTotalThread()
        {
            return totalThread;
        }

        public int getCurrentThread()
        {
            return currentThread;
        }

        public long getTotalSize(int id)
        {
            return threadTotalSize[id];
        }

        public long getCurrentSize(int id)
        {
            return threadCurrentSize[id];
        }

        private void addThread(Thread thread,int threadId,long length)
        {
            threads[threadId]=thread;
            threadTotalSize[threadId]=length;
        }

        private void finish(int threadId)
        {
            threads[threadId]=null;
            currentThread--;
        }

        private void add(int ThreadId,int size)
        {
            threadCurrentSize[ThreadId]+=size;
            super.add(size);
        }

        @Override
        public boolean isFinished()
        {
            return currentThread==0;
        }

        protected void error(Throwable e)
        {
            for (Thread thread: threads)
            {
                if (thread!=null)
                {
                    thread.interrupt();
                }
            }
            super.error(e);
        }
    }

    private class DownloadThread extends Thread
    {
        private final long startIndex;
        private final long endIndex;
        private final int threadId;

        public DownloadThread(int threadId,long startIndex,long endIndex)
        {
            this.threadId=threadId;
            this.startIndex=startIndex;
            this.endIndex=endIndex;
        }


        @Override
        public void run()
        {
            try
            {
                URL url=new URL(serverPath);
                HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);
                conn.setConnectTimeout(5000);
                int code=conn.getResponseCode();

                InputStream is=conn.getInputStream();
                RandomAccessFile raf=new RandomAccessFile(localPath,"rwd");
                raf.seek(startIndex);

                int len;
                byte[] buffer=new byte[1024];
                while ((len=is.read(buffer))>0)
                {
                    raf.write(buffer,0,len);
                    progressManager.add(threadId,len);
                }
                is.close();
                raf.close();
            }
            catch (Throwable e)
            {
                progressManager.error(e);
                return;
            }
            progressManager.finish(threadId);
        }
    }
}