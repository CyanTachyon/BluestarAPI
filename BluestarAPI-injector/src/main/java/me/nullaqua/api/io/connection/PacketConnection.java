package me.nullaqua.api.io.connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 一个数据包连接
 */
public class PacketConnection
{
    private final PacketTypeList types;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private Runnable onClose;
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
        this.socket=null;
        this.types=types;
        this.onClose=onClose;
    }

    /**
     * 通过一个Socket连接来创建一个包连接
     *
     * @param socket 连接
     * @throws IOException 获取输入输出流时出现错误抛出
     */
    public PacketConnection(Socket socket) throws IOException
    {
        this(socket,new PacketTypeList());
    }

    /**
     * 通过一个Socket连接来创建一个包连接
     *
     * @param socket 连接
     * @param types  数据包类型列表
     * @throws IOException 获取输入输出流时出现错误抛出
     */
    public PacketConnection(Socket socket,PacketTypeList types) throws IOException
    {
        this(socket,types,()->
        {
        });
    }

    /**
     * 通过一个Socket连接来创建一个包连接
     *
     * @param socket  连接
     * @param types   数据包类型列表
     * @param onClose 关闭时的回调
     * @throws IOException 获取输入输出流时出现错误抛出
     */
    public PacketConnection(Socket socket,PacketTypeList types,Runnable onClose) throws IOException
    {
        this.in=new DataInputStream(socket.getInputStream());
        this.out=new DataOutputStream(socket.getOutputStream());
        this.socket=socket;
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
     * 获取输入流
     */
    @NotNull
    public DataInputStream in()
    {
        return in;
    }

    /**
     * 获取输出流
     */
    @NotNull
    public DataOutputStream out()
    {
        return out;
    }

    /**
     * 获取Socket连接
     */
    @Nullable
    public Socket socket()
    {
        return socket;
    }

    /**
     * 设置关闭时的回调
     *
     * @param onClose 关闭时的回调
     */
    public PacketConnection onClose(Runnable onClose)
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
            packet.write(out);
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
        if (listeningThread!=null)
        {
            return null;
        }
        listeningThread=new Thread()
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
        listeningThread.start();
        return listeningThread;
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
            var type=types.get(id);
            if (type==null)
            {
                return new Packet.UnknownPacket(id);
            }
            return type.read(in);
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