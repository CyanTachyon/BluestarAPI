package me.nullaqua.api;

import me.nullaqua.api.reflect.BluestarI;

import java.util.Random;

public class BluestarUtils extends BluestarI
{
    private static final Random random = new Random();

    /**
     * 随机一个0到给定数之间的整数
     *
     * @param bound 给定数
     * @return 随机数
     */
    public static int randomInt(int bound)
    {
        return random.nextInt(bound);
    }

    /**
     * 随机一个long范围内的数
     *
     * @return 随机数
     */
    public static long randomLong()
    {
        return random.nextLong();
    }

    /**
     * 一个随机字节
     *
     * @return 随机字节
     */
    public static byte randomByte()
    {
        int x = randomInt();
        return (byte) ((x&0xff)^(x>>8&0xff)^(x>>16&0xff)^(x>>24&0xff));
    }

    /**
     * 随机一个int范围内的数
     *
     * @return 随机数
     */
    public static int randomInt()
    {
        return random.nextInt();
    }

    /**
     * 随机生成一些字节
     *
     * @param len 字节长度
     * @return 随机生成的字节
     */
    public static byte[] randomBytes(int len)
    {
        var x = new byte[len];
        randomBytes(x);
        return x;
    }

    /**
     * 用随机生成的字节填满字节数组
     *
     * @param bytes 字节数组
     */
    public static void randomBytes(byte[] bytes)
    {
        random.nextBytes(bytes);
    }

    /**
     * 按照百分比计算概率
     *
     * @param p 百分比
     *          例如：p=30,则返回true的概率为30%
     */
    public static boolean probabilityPercent(double p)
    {
        return probability(p/100);
    }

    /**
     * 概率随机
     *
     * @param p 概率,应为0~1之间的小数
     *          例如：p=0.30,则返回true的概率为30%
     */
    public static boolean probability(double p)
    {
        return randomDouble() < p;
    }

    /**
     * 随机0-1之间的double
     *
     * @return 随机double
     */
    public static double randomDouble()
    {
        return random.nextDouble();
    }
}
