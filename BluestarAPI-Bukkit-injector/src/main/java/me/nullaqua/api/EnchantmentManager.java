package me.nullaqua.api;

import me.nullaqua.api.reflect.FieldAccessor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

/**
 * 附魔管理器,处理附魔相关内容
 * 我的世界本身在服务器启动阶段会注册所有附魔,但是在此之后服务器会彻底关闭附魔注册,所以如果需要注册自定义附魔,需要通过反射
 * 本类提供了一些方法来处理附魔注册.注意注册附魔本身只是让服务器知道这个附魔的存在,但是附魔的效果需要自己实现(比如说用监听器监听附魔物品的使用)
 * 自己注册的附魔没办法像官方附魔一样在物品上显示,需要自己实现显示(通过lore或者其他方式)
 */
public final class EnchantmentManager {
    private static final FieldAccessor acceptRegisterEnchantment;
    private static final Map<NamespacedKey, Enchantment> enchantmentByKey;
    private static final Map<String, Enchantment> enchantmentByName;

    static {
        try {
            acceptRegisterEnchantment = FieldAccessor.getDeclaredField(Enchantment.class, "acceptingNew");
            FieldAccessor byKey = FieldAccessor.getDeclaredField(Enchantment.class, "byKey");
            enchantmentByKey = (Map<NamespacedKey, Enchantment>) byKey.get(null);

            FieldAccessor byName = FieldAccessor.getDeclaredField(Enchantment.class, "byName");
            enchantmentByName = (Map<String, Enchantment>) byName.get(null);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册一个附魔
     *
     * @param enchantment 附魔
     * @return 是否注册成功
     */
    public static boolean registerEnchantment(Enchantment enchantment) {
        if (!openEnchantmentRegistrations()) {
            return false;
        }
        Enchantment.registerEnchantment(enchantment);
        closeEnchantmentRegistrations();
        return true;
    }

    /**
     * 开启附魔注册
     * @return 是否开启成功
     */
    public static boolean openEnchantmentRegistrations() {
        if (acceptRegisterEnchantment == null) {
            return false;
        }
        try {
            acceptRegisterEnchantment.set(null, true);
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    /**
     * 重新关闭附魔注册
     */
    public static void closeEnchantmentRegistrations() {
        Enchantment.stopAcceptingRegistrations();
    }

    /**
     * 移除一个附魔
     * @param key 附魔的key
     * @return 被移除的附魔
     */
    public static Enchantment removeEnchantment(NamespacedKey key) {
        Enchantment enchantment = enchantmentByKey.remove(key);
        enchantmentByName.values().remove(enchantment);
        return enchantment;
    }

    /**
     * 移除一个附魔
     * @param name 附魔的名称
     * @return 被移除的附魔
     */
    public static Enchantment removeEnchantment(String name) {
        Enchantment enchantment = enchantmentByName.remove(name);
        enchantmentByKey.values().remove(enchantment);
        return enchantment;
    }

    /**
     * 获取一个附魔
     * @param key 附魔的key
     * @return 附魔
     */
    public static Enchantment getEnchantment(NamespacedKey key) {
        return enchantmentByKey.get(key);
    }

    /**
     * 获取一个附魔
     * @param name 附魔的名称
     * @return 附魔
     */
    public static Enchantment getEnchantment(String name) {
        return enchantmentByName.get(name);
    }

    /**
     * 获取所有附魔
     * @return 附魔数组
     */
    public static Enchantment[] getEnchantments() {
        return Enchantment.values();
    }
}
