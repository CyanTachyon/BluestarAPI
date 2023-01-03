package me.lanzhi.api.net.dowloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DefaultDownloader
{
    public static DownloadProgressManager download(String serverPath,String localPath) throws IOException
    {
        File file=new File(localPath);
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        URL url=new URL(serverPath);
        URLConnection conn=url.openConnection();
        var downloadProgressManager=new DefaultDownloadProgressManager(conn.getContentLengthLong());
        new Thread()
        {
            @Override
            public void run()
            {
                try (InputStream inputStream=conn.getInputStream();FileOutputStream outputStream=new FileOutputStream(
                        file))
                {
                    byte[] buffer=new byte[1024];
                    int len;
                    while ((len=inputStream.read(buffer))!=-1)
                    {
                        outputStream.write(buffer,0,len);
                        downloadProgressManager.add(len);
                    }
                }
                catch (IOException e)
                {
                    downloadProgressManager.error(e);
                }
            }
        }.start();
        return downloadProgressManager;
    }

    private static class DefaultDownloadProgressManager extends DownloadProgressManager
    {
        public DefaultDownloadProgressManager(long totalSize)
        {
            super(totalSize);
        }
    }
}
