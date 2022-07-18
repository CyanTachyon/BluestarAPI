package me.lanzhi.bluestarapi.api.net.verification;

import me.lanzhi.bluestarapi.BluestarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

public final class Verification
{
    private final VerificationPlugin plugin;
    private final Socket socket;
    private VerificationRequest message;
    private UUID key;
    private ObjectOutputStream outputStream=null;
    private ObjectInputStream inputStream=null;
    private long time;
    private boolean isRun=false;
    private boolean isSuccess=false;

    public Verification(VerificationPlugin plugin,UUID key)
    {
        this.key=key;
        message=new VerificationRequest(plugin.getName(),key);
        this.plugin=plugin;
        socket=new Socket();
        //start();
    }

    public UUID getKey()
    {
        return key;
    }

    public void setKey(UUID key)
    {
        this.key=key;
        plugin.getKeyFile().set("key",key.toString());
        message=new VerificationRequest(plugin.getName(),key);
    }

    public Verification start()
    {
        if (isRun)
        {
            return this;
        }
        isRun=true;
        boolean accepted=false;
        String error=null;
        for (int i=0;i<5;i++)
        {
            try
            {
                socket.setSoTimeout(1000);
                socket.connect(new InetSocketAddress("ver.bluestarmc.top",7777),1000);
            }
            catch (Exception e)
            {
                //Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 联网检测时出现问题,请检查网络连接.错误代码:0x01");
                //Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 错误信息:"+e.getMessage());
                //Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 重新进行尝试...");
                error=e.getMessage();
                continue;
            }
            accepted=true;
            break;
        }
        if (!accepted)
        {
            Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 无法完成联网检测,插件将被停用.请检查网络连接,或者联系作者.错误代码:0x01,错误信息:"+error);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return this;
        }
        accepted=false;
        for (int i=0;i<5;i++)
        {
            try
            {
                outputStream=new ObjectOutputStream(socket.getOutputStream());
                inputStream=new ObjectInputStream(socket.getInputStream());
            }
            catch (Exception e)
            {
                //Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 联网检测时出现问题,请检查网络连接.错误代码:0x02");
                //Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 错误信息:"+e.getMessage());
                //Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 重新进行尝试...");
                continue;
            }
            accepted=true;
            break;
        }
        if (!accepted)
        {
            Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 无法完成联网检测,插件将被停用.请检查网络连接,或者联系作者.错误代码:0x02");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return this;
        }
        time=System.currentTimeMillis();
        new VerificationSend().runTaskTimerAsynchronously(BluestarAPI.thisPlugin,0,20);
        new VerificationGet().runTaskAsynchronously(BluestarAPI.thisPlugin);
        return this;
    }

    public boolean isSuccess()
    {
        return isSuccess;
    }

    public class VerificationSend extends BukkitRunnable
    {
        @Override
        public void run()
        {
            if (!plugin.isEnabled())
            {
                cancel();
                return;
            }
            //发送消息
            try
            {
                outputStream.writeObject(message);
                if (BluestarAPI.debug)
                {
                    System.out.println("发送消息成功");
                }
            }
            catch (Exception e)
            {
                Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 联网检测时出现问题,可能影响检查结果.错误代码:0x03,详细内容:");
                e.printStackTrace();
            }
            //延迟超过10秒卸载插件
            if (System.currentTimeMillis()-time>10000&&isSuccess)
            {
                Bukkit.getLogger().warning(ChatColor.RED+"["+plugin.getName()+"] 联网检测延迟过高,插件功能被停用.");
                isSuccess=false;
            }
        }
    }

    public class VerificationGet extends BukkitRunnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                if (!plugin.isEnabled())
                {
                    cancel();
                    return;
                }
                try
                {
                    VerificationReceive mess=(VerificationReceive) inputStream.readObject();
                    if (BluestarAPI.debug)
                    {
                        System.out.println("接收到反馈: "+mess.isSuccess()+","+mess.getTime());
                    }
                    isSuccess=mess.isSuccess();
                    time=Math.max(time,mess.getTime());
                }
                catch (Exception e)
                {
                }
            }
        }
    }
}
