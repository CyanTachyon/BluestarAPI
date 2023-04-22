package me.lanzhi.api.io.connection;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

/**
 * 一个连接
 */
public class Connection
{
    private Runnable onClose;

    private static final int DEFAULT_BUFFER_SIZE=8192;
    private final DataInputStream in;
    private final DataOutputStream out;

    public static Connection create(Socket socket) throws IOException
    {
        return new Connection(socket.getInputStream(),socket.getOutputStream())
        {
            @Override
            public Connection onClose(Runnable onClose)
            {
                return super.onClose(()->
                                     {
                                         try
                                         {
                                             socket.close();
                                         }
                                         catch (IOException ignored)
                                         {
                                         }
                                         onClose.run();
                                     });
            }
        };
    }

    public Connection onClose(Runnable onClose)
    {
        if (!Objects.isNull(onClose))
        {
            this.onClose=onClose;
        }
        else
        {
            this.onClose=()->
            {
            };
        }
        return this;
    }
    private boolean running=true;

    /**
     * 通过输入输出流来创建一个连接
     *
     * @param in  输入流
     * @param out 输出流
     */
    public Connection(InputStream in,OutputStream out)
    {
        this(in,out,()->
        {});
    }

    /**
     * 通过输入输出流来创建一个连接
     *
     * @param in      输入流
     * @param out     输出流
     * @param onClose 关闭时的回调
     */
    public Connection(InputStream in,OutputStream out,Runnable onClose)
    {
        this.in=new DataInputStream(in);
        this.out=new DataOutputStream(out);
        this.onClose=onClose;
    }

    /**
     * 获取输入流
     *
     * @return 输入流
     */
    public DataInputStream in()
    {
        return in;
    }

    /**
     * 获取输出流
     *
     * @return 输出流
     */
    public DataOutputStream out()
    {
        return out;
    }

    /**
     * 获取连接是否正在运行
     *
     * @return 连接是否正在运行
     */
    public boolean running()
    {
        return running;
    }

    /**
     * 监听输入流
     *
     * @param listener 监听器
     */
    public void listener(Listener listener)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    byte[] buffer=new byte[DEFAULT_BUFFER_SIZE];
                    int x;
                    while ((x=in.read(buffer,0,DEFAULT_BUFFER_SIZE))>=0)
                    {
                        listener.receive(x,buffer);
                    }
                }
                catch (Throwable e)
                {
                    close();
                }
            }
        }.start();
    }

    /**
     * 关闭连接
     */
    public void close()
    {
        if (!running)
        {
            return;
        }
        running=false;
        try
        {
            onClose.run();
        }
        catch (Throwable ignored)
        {
        }
        try
        {
            in.close();
        }
        catch (Throwable ignored)
        {
        }
        try
        {
            out.close();
        }
        catch (Throwable ignored)
        {
        }
    }

    /**
     * 监听器
     */
    public interface Listener
    {
        /**
         * 接收到数据
         *
         * @param x      数据长度
         * @param buffer 数据,其前x个字节有效
         * @throws Throwable 抛出的异常
         */
        void receive(int x,byte[] buffer) throws Throwable;
    }
}