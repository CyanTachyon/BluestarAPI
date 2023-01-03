package me.lanzhi.api.net.dowloader;

import me.lanzhi.api.util.collection.FastLinkedList;

import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;

public abstract class DownloadProgressManager
{
    private final FastLinkedList<Map.Entry<Long,Long>> time;
    private final long totalSize;
    private final long startTime;
    private long speed=0;
    private long currentSize;
    private Throwable errorCause;
    private DownloadData data;

    protected DownloadProgressManager(long totalSize)
    {
        this.totalSize=totalSize;
        this.time=new FastLinkedList<>();
        currentSize=0;
        startTime=System.currentTimeMillis();
    }

    public Date getStartTime()
    {
        return new Date(startTime);
    }

    public long getTotalSize()
    {
        return totalSize;
    }

    public long getCurrentSize()
    {
        return currentSize;
    }

    public long speed()
    {
        synchronized (this)
        {
            update();
            return speed;
        }
    }

    private void update()
    {
        long time=System.currentTimeMillis();
        while (!this.time.isEmpty()&&time-this.time.getFirst().getKey()>1000)
        {
            speed-=this.time.getFirst().getValue();
            this.time.removeFirst();
        }
        if (!this.isRunning())
        {
            data=new DownloadData(this);
        }
    }

    public boolean isRunning()
    {
        return !isError()&&!isFinished();
    }

    public boolean isError()
    {
        return errorCause!=null;
    }

    public boolean isFinished()
    {
        return currentSize>=totalSize;
    }

    protected void error(Throwable cause)
    {
        synchronized (this)
        {
            if (!isRunning())
            {
                return;
            }
            errorCause=cause;
        }
    }

    public Throwable error()
    {
        return errorCause;
    }

    protected void add(long size)
    {
        synchronized (this)
        {
            if (!isRunning())
            {
                return;
            }
            currentSize+=size;
            long time=System.currentTimeMillis();
            speed+=size;
            this.time.add(new AbstractMap.SimpleEntry<>(time,size));
            update();
        }
    }

    public DownloadData getData()
    {
        return data;
    }

    public static class DownloadData extends DownloadProgressManager
    {
        private final DownloadProgressManager manager;
        private final long endTime;

        private DownloadData(DownloadProgressManager x)
        {
            super(x.totalSize);
            this.manager=x;
            this.endTime=System.currentTimeMillis();
        }

        @Override
        public Date getStartTime()
        {
            return manager.getStartTime();
        }

        @Override
        public long getCurrentSize()
        {
            return getTotalSize();
        }

        @Override
        public long getTotalSize()
        {
            return manager.getTotalSize();
        }

        @Override
        public long speed()
        {
            return 0;
        }

        @Override
        public boolean isRunning()
        {
            return false;
        }

        @Override
        public boolean isFinished()
        {
            return !isError();
        }

        @Override
        public boolean isError()
        {
            return super.isError();
        }

        @Override
        protected void error(Throwable cause)
        {
            throw new RuntimeException("can't set error");
        }

        @Override
        public Throwable error()
        {
            return manager.error();
        }

        @Override
        protected void add(long size)
        {
            throw new RuntimeException("can't add size");
        }

        public Date getEndTime()
        {
            return new Date(endTime);
        }

        //平均速度
        public long averageSpeed()
        {
            return getTotalSize()/runTime();
        }

        //运行时间
        public long runTime()
        {
            return endTime-manager.startTime;
        }
    }
}
