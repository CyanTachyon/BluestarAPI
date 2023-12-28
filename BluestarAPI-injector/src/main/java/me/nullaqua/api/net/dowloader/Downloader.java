package me.nullaqua.api.net.dowloader;

import me.nullaqua.api.collection.FastLinkedList;
import me.nullaqua.api.util.quantity.DataRate;
import me.nullaqua.api.util.quantity.DataSize;
import me.nullaqua.api.util.quantity.Time;
import me.nullaqua.api.util.quantity.unit.DataRateUnit;
import me.nullaqua.api.util.quantity.unit.DataSizeUnit;
import me.nullaqua.api.util.quantity.unit.TimeUnit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;

public abstract class Downloader
{
    //-------------------
    private final FastLinkedList<Map.Entry<Long,Long>> time;
    private final String file;
    private Status status;

    Downloader(long totalSize,String file)
    {
        this.totalSize=totalSize;
        this.time=new FastLinkedList<>();
        currentSize=0;
        startTime=System.currentTimeMillis();
        this.file=file;
        status=Status.Running;
    }

    public static Downloader download(String serverPath,String savePath)
    {
        return download(serverPath,savePath,-1,-1);
    }

    public static Downloader download(String url,String savePath,int threadCount,int timeOut)
    {
        if (threadCount>1)
        {
            try
            {
                return MultiThreadDownload.download(threadCount,url,savePath,timeOut);
            }
            catch (Throwable ignored)
            {
            }
        }
        try
        {
            return DefaultDownload.download(url,savePath);
        }
        catch (Throwable ignored)
        {
        }
        return null;
    }

    public static Downloader download(String serverPath,String savePath,int threadCount)
    {
        return download(serverPath,savePath,threadCount,-1);
    }

    public static Downloader downloadToDir(String serverPath,String dirPath)
    {
        return downloadToDir(serverPath,dirPath,-1,-1);
    }

    public static Downloader downloadToDir(String serverPath,String dirPath,int threadCount,int timeOut)
    {
        var file=createFileForDownload(serverPath,dirPath,timeOut);
        return download(serverPath,file.getAbsolutePath(),threadCount,timeOut);
    }

    private final long totalSize;
    private final long startTime;

    public static File createFileForDownload(String url,String dir,int timeOut)
    {
        File _dir=new File(dir);
        if (_dir.isFile())
        {
            throw new RuntimeException("The file download directory is not a folder");
        }
        try
        {
            _dir.mkdirs();
        }
        catch (Throwable throwable)
        {
            throw new RuntimeException("Unable to create file download directory",throwable);
        }
        String fileName=getFileName(url,timeOut);
        var temp=fileName.lastIndexOf(".");
        var oName=fileName.substring(0,temp>0?temp:fileName.length());
        var suffixName=temp>0?fileName.substring(temp):"";
        File file=new File(_dir,fileName);
        int x=0;
        while (true)
        {
            try
            {
                if (!file.exists())
                {
                    break;
                }
                file=new File(_dir,oName+" ("+(++x)+")"+suffixName);
            }
            catch (Throwable throwable)
            {
                throw new RuntimeException("Unable to create file download directory",throwable);
            }
        }
        return file;
    }

    private long speed=0;
    private long currentSize;
    private Throwable errorCause;
    private DownloadData data;

    public static String getFileName(String url,int timeOut)
    {
        try
        {
            URL u=new URL(url);
            var conn=u.openConnection();
            if (timeOut>0)
            {
                conn.setConnectTimeout(timeOut);
                conn.setReadTimeout(timeOut);
            }
            String fileName=conn.getHeaderField("Content-Disposition");
            int x;
            if (fileName!=null&&(x=fileName.indexOf("filename="))>0)
            {
                int y=fileName.indexOf(";",x);
                if (y<0)
                {
                    y=fileName.length();
                }
                fileName=fileName.substring(x+9,y);
                return fileName;
            }

            int index=conn.getURL().toString().lastIndexOf("/");
            if (index==-1)
            {
                return conn.getURL().toString();
            }
            return conn.getURL().toString().substring(index+1);
        }
        catch (Throwable ignored)
        {
        }
        int index=url.lastIndexOf("/");
        if (index==-1)
        {
            return url;
        }
        return url.substring(index+1);
    }

    public static Downloader downloadToDir(String serverPath,String dirPath,int threadCount) throws IOException
    {
        return downloadToDir(serverPath,dirPath,threadCount,-1);
    }

    /**
     * Set this download task is finished
     */
    protected void setSuccess()
    {
        if (status()==Status.Running||status()==Status.Pause)
        {
            status(Status.Success);
            this.data=new DownloadData(this);
        }
    }

    /**
     * Set this download task is error
     * @param cause the error cause
     */
    protected final void error(Throwable cause)
    {
        synchronized (this)
        {
            if (!running())
            {
                return;
            }
            errorCause=cause;
            status(Status.Error);
        }
    }

    public final double progressPercent()
    {
        double progress=progress();
        if (progress>=0)
        {
            return progress*100;
        }
        return -1;
    }

    public final double progress()
    {
        if (totalSize()!=null)
        {
            return currentSize().size()*1.0/totalSize().size();
        }
        return -1;
    }

    private final void update()
    {
        long time=System.currentTimeMillis();
        while (!this.time.isEmpty()&&time-this.time.getFirst().getKey()>1000)
        {
            speed-=this.time.getFirst().getValue();
            this.time.removeFirst();
        }
    }

    private void status(Status status)
    {
        synchronized (this)
        {
            if (status!=null&&status!=this.status())
            {
                this.status=status;
                this.notifyAll();
            }
        }
    }

    public final Status status()
    {
        synchronized (this)
        {
            return status;
        }
    }

    public final boolean hasError()
    {
        return status()==Status.Error;
    }

    public final boolean success()
    {
        return status()==Status.Success;
    }

    public final boolean pause()
    {
        return status()==Status.Pause;
    }

    public final Downloader cancel()
    {
        status(Status.Cancel);
        return this;
    }

    public final Downloader pause(boolean b)
    {
        status(b?Status.Pause:Status.Running);
        return this;
    }

    public final Throwable errorCause()
    {
        return errorCause;
    }

    public final DownloadData data()
    {
        return data;
    }

    public final Date startTime()
    {
        return new Date(startTime);
    }

    public final DataSize totalSize()
    {
        if (totalSize>0)
        {
            return new DataSize(totalSize);
        }
        return null;
    }

    public final boolean running()
    {
        return status()==Status.Running;
    }

    public String file()
    {
        return file;
    }

    protected final void add(long size)
    {
        synchronized (this)
        {
            if (!running())
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

    public final DataSize currentSize()
    {
        return new DataSize(currentSize);
    }

    public final Time remainingTime()
    {
        if (waitSize()!=null)
        {
            return new Time((long) (waitSize().size()/speed().rate(new DataRateUnit(DataSizeUnit.B,TimeUnit.ms))));
        }
        return null;
    }

    public final DataSize waitSize()
    {
        if (totalSize()!=null)
        {
            return new DataSize(totalSize().size()-currentSize().size());
        }
        return null;
    }

    public final DataRate speed()
    {
        synchronized (this)
        {
            update();
            return new DataRate(new DataSize(speed),TimeUnit.s);
        }
    }

    public boolean finish()
    {
        var status=status();
        return status==Status.Success||status==Status.Cancel||status==Status.Error;
    }

    public Downloader waitFinish()
    {
        while (true)
        {
            synchronized (this)
            {
                if (this.finish()) break;
                else try
                {
                    this.wait();
                }
                catch (Throwable ignored)
                {
                }
            }
        }
        return this;
    }

    public Downloader waitSuccess() throws IOException
    {
        waitFinish();
        if (this.success()) return this;
        if (this.hasError())
            if (this.errorCause instanceof IOException) throw (IOException) errorCause;
            else throw new IOException(errorCause);
        throw new IOException("Unknown error");
    }

    public enum Status
    {
        Running,
        Success,
        Error,
        Cancel,
        Pause
    }

    public static class DownloadData
    {
        private final Downloader downloader;
        private final long endTime;

        private DownloadData(Downloader x)
        {
            this.downloader=x;
            this.endTime=System.currentTimeMillis();
        }

        public Date startTime()
        {
            return downloader.startTime();
        }

        public DataSize currentSize()
        {
            return downloader.currentSize();
        }

        public DataRate averageSpeed()
        {
            return new DataRate(totalSize(),runTime());
        }

        public boolean error()
        {
            return downloader.hasError();
        }

        public Throwable errorCause()
        {
            return downloader.errorCause();
        }

        public Date endTime()
        {
            return new Date(endTime);
        }

        public DataSize totalSize()
        {
            return downloader.currentSize();
        }

        public Time runTime()
        {
            return new Time(endTime-downloader.startTime);
        }
    }
}
