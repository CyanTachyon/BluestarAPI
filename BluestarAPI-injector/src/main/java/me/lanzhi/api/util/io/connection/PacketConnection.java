package me.lanzhi.api.util.io.connection;

import java.io.*;
import java.util.function.Supplier;

/**
 * 一个数据包连接
 */
public class PacketConnection
{
    private final PacketTypeList types;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Runnable onClose;
    private boolean heartbeat=false;
    private boolean running=true;
    private Thread listeningThread;

    /**
     * 通过输入输出流来创建一个数据包连接,数据包类型列表为空
     *
     * @param in  输入流
     * @param out 输出流
     */
    public PacketConnection(InputStream in,OutputStream out)
    {
        this(in,out,new PacketTypeList());
    }

    /**
     * 通过输入输出流和数据包类型列表来创建一个数据包连接
     *
     * @param in    输入流
     * @param out   输出流
     * @param types 数据包类型列表
     */
    public PacketConnection(InputStream in,OutputStream out,PacketTypeList types)
    {
        this(in,out,types,()->
        {
        });
    }

    /**
     * 通过输入输出流和数据包类型列表来创建一个数据包连接
     *
     * @param in      输入流
     * @param out     输出流
     * @param types   数据包类型列表
     * @param onClose 关闭时的回调
     */
    public PacketConnection(InputStream in,OutputStream out,PacketTypeList types,Runnable onClose)
    {
        this.in=new DataInputStream(in);
        this.out=new DataOutputStream(out);
        this.types=types;
        this.onClose=onClose;
    }

    /**
     * 获取数据包类型列表
     *
     * @return 数据包类型列表
     */
    public PacketTypeList types()
    {
        return types;
    }

    /**
     * 心跳,每隔一段时间发送一个固定的数据包,若已调用过此方法或{@link #heartbeat(Supplier,long)}方法,则返回null
     *
     * @param packet 用于发送的数据包
     * @param time   时间间隔
     * @return 心跳线程
     */
    public Thread heartbeat(Packet packet,long time)
    {
        return heartbeat(()->packet,time);
    }

    /**
     * 心跳,每隔一段时间发送一个数据包,若已调用过此方法或{@link #heartbeat(Packet,long)}方法,则返回null
     *
     * @param packet 获取用于发送的数据包的函数
     * @param time   时间间隔
     * @return 心跳线程
     */
    public Thread heartbeat(Supplier<Packet> packet,long time)
    {
        if (heartbeat)
        {
            return null;
        }
        heartbeat=true;
        var t=new Thread()
        {
            public void run()
            {
                while (running())
                {
                    try
                    {
                        Thread.sleep(time);
                    }
                    catch (Throwable ignored)
                    {
                    }
                    try
                    {
                        send(packet.get());
                    }
                    catch (Throwable e)
                    {
                        break;
                    }
                }
                close();
            }
        };
        t.start();
        return t;
    }

    /**
     * 连接是否正在运行,若已关闭,则返回false,否则返回true
     *
     * @return 连接是否正在运行
     */
    public boolean running()
    {
        return running;
    }

    /**
     * 发送一个数据包
     */
    public void send(Packet packet) throws IOException
    {
        synchronized (out)
        {
            if (!running)
            {
                throw new IOException("Connection closed");
            }
            out.writeByte(packet.type().id());
            byte[] data=packet.toByteArray();
            out.writeLong(data.length);
            out.write(data);
        }
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
     * 监听数据包
     *
     * @param listener 监听器
     * @return 监听线程
     * @see #receive() 接收数据包
     */
    public Thread listener(Listener listener)
    {
        var t=new Thread()
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        listener.onPacket(receive());
                    }
                    catch (Throwable e)
                    {
                        break;
                    }
                }
                close();
            }
        };
        t.start();
        return t;
    }

    /**
     * 接收一个数据包,若已使用{@link #listener(Listener)}监听,仍调用此方法会抛出异常
     *
     * @return 数据包
     * @throws IOException IO异常
     */
    public Packet receive() throws IOException
    {
        synchronized (in)
        {
            if (!running)
            {
                throw new IOException("Connection closed");
            }
            if (listeningThread!=null&&!listeningThread.equals(Thread.currentThread()))
            {
                throw new IOException("Already listening");
            }
            byte id=in.readByte();
            long size=in.readLong();
            byte[] data=new byte[(int) size];
            in.readFully(data);
            return types.get(id).fromByteArray(data);
        }
    }

    /**
     * 数据包监听器
     */
    public interface Listener
    {
        /**
         * 当接收到一个数据包时调用
         *
         * @param packet 数据包
         * @throws Throwable 抛出的异常
         */
        void onPacket(Packet packet) throws Throwable;
    }
}