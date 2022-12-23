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
        var x=new byte[1];
        randomBytes(x);
        return x[0];
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
