package me.lanzhi.api;

import java.util.Random;

public class BluestarUtils
{
    private static final Random random=new Random();

    public static int randomInt(int bound)
    {
        return random.nextInt(bound);
    }

    public static int randomInt()
    {
        return random.nextInt();
    }

    public static long randomLong()
    {
        return random.nextLong();
    }

    public static double randomDouble()
    {
        return random.nextDouble();
    }

    public static byte randomByte()
    {
        int x=randomInt();
        return (byte) ((x&0xff)^(x>>8&0xff)^(x>>16&0xff)^(x>>24&0xff));
    }

    public static void randomBytes(byte[] bytes)
    {
        random.nextBytes(bytes);
    }

    public static byte[] randomBytes(int len)
    {
        var x=new byte[len];
        randomBytes(x);
        return x;
    }
}
