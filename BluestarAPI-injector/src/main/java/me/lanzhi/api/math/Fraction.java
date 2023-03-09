package me.lanzhi.api.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * 表示一个分数
 *
 * @author lanzhi
 */
public class Fraction extends Number implements Serializable
{
    private static final long serialVersionUID=1L;

    private BigInteger numerator;
    private BigInteger denominator;

    /**
     * 用分子和分母构造一个分数
     *
     * @param numerator   分子
     * @param denominator 分母
     */
    public Fraction(long numerator,long denominator)
    {
        this(BigInteger.valueOf(numerator),BigInteger.valueOf(denominator));
    }

    /**
     * 用分子和分母构造一个分数
     *
     * @param numerator   分子
     * @param denominator 分母
     */
    public Fraction(BigInteger numerator,BigInteger denominator)
    {
        this.numerator=numerator;
        this.denominator=denominator;
        reduce();
    }

    /**
     * 约分
     */
    public void reduce()
    {
        BigInteger gcd=numerator.gcd(denominator);
        numerator=numerator.divide(gcd);
        denominator=denominator.divide(gcd);
        if (denominator.compareTo(BigInteger.ZERO)<0)
        {
            numerator=numerator.negate();
            denominator=denominator.negate();
        }
    }

    /**
     * 用分子和分母构造一个分数
     *
     * @param numerator   分子
     * @param denominator 分母
     */
    public Fraction(int numerator,int denominator)
    {
        this(BigInteger.valueOf(numerator),BigInteger.valueOf(denominator));
    }

    /**
     * 小数转分数
     *
     * @param value 小数
     */
    public Fraction(double value)
    {
        this(value,1e-10);
    }

    /**
     * 小数转分数
     *
     * @param value   小数
     * @param epsilon 精度
     */
    public Fraction(double value,double epsilon)
    {
        long h1=1;
        long h2=0;
        long k1=0;
        long k2=1;
        double b=value;
        do
        {
            double a=Math.floor(b);
            long aux=h1;
            h1=(long) (a*h1+h2);
            h2=aux;
            aux=k1;
            k1=(long) (a*k1+k2);
            k2=aux;
            b=1/(b-a);
        }while (Math.abs(value-h1/k1)>value*epsilon);
        numerator=BigInteger.valueOf(h1);
        denominator=BigInteger.valueOf(k1);
        reduce();
    }

    /**
     * 从整数构造一个分数
     *
     * @param value 整数
     */
    public Fraction(int value)
    {
        this(BigInteger.valueOf(value),BigInteger.ONE);
    }

    /**
     * 从整数构造一个分数
     *
     * @param value 整数
     */
    public Fraction(long value)
    {
        this(BigInteger.valueOf(value),BigInteger.ONE);
    }

    /**
     * 从整数构造一个分数
     *
     * @param value 整数
     */
    public Fraction(BigInteger value)
    {
        this(value,BigInteger.ONE);
    }

    /**
     * 从字符串构造一个分数
     *
     * @param value 字符串
     */
    public Fraction(String value)
    {
        String[] parts=value.split("/");
        if (parts.length==1)
        {
            numerator=new BigInteger(parts[0]);
            denominator=BigInteger.ONE;
        }
        else if (parts.length==2)
        {
            numerator=new BigInteger(parts[0]);
            denominator=new BigInteger(parts[1]);
        }
        else
        {
            throw new IllegalArgumentException("Invalid fraction value: "+value);
        }
        reduce();
    }

    /**
     * 获取分子
     *
     * @return 分子
     */
    public BigInteger getNumerator()
    {
        return numerator;
    }

    /**
     * 获取分母
     *
     * @return 分母
     */
    public BigInteger getDenominator()
    {
        return denominator;
    }

    /**
     * 分数转字符串
     *
     * @return 字符串
     */
    @Override
    public String toString()
    {
        return numerator+"/"+denominator;
    }

    /**
     * 分数转整数
     *
     * @return 整数
     */
    @Override
    public int intValue()
    {
        return toInt();
    }

    /**
     * 分数转整数
     *
     * @return 整数
     */
    public int toInt()
    {
        return numerator.intValue()/denominator.intValue();
    }

    /**
     * 分数转长整数
     *
     * @return 长整数
     */
    @Override
    public long longValue()
    {
        return toLong();
    }

    /**
     * 分数转长整数
     *
     * @return 长整数
     */
    public long toLong()
    {
        return numerator.longValue()/denominator.longValue();
    }

    /**
     * 分数转浮点数
     *
     * @return 浮点数
     */
    @Override
    public float floatValue()
    {
        return toFloat();
    }

    /**
     * 分数转浮点数
     *
     * @return 浮点数
     */
    public float toFloat()
    {
        return numerator.floatValue()/denominator.floatValue();
    }

    /**
     * 分数转双精度浮点数
     *
     * @return 双精度浮点数
     */
    @Override
    public double doubleValue()
    {
        return toDouble();
    }

    /**
     * 分数转小数
     *
     * @return 小数
     */
    public double toDouble()
    {
        return numerator.doubleValue()/denominator.doubleValue();
    }

    /**
     * 分数加法
     *
     * @param fraction 分数
     * @return 结果
     */
    public Fraction add(Fraction fraction)
    {
        return new Fraction(numerator.multiply(fraction.denominator).add(denominator.multiply(fraction.numerator)),
                            denominator.multiply(fraction.denominator));
    }

    /**
     * 分数减法
     *
     * @param fraction 分数
     * @return 结果
     */
    public Fraction subtract(Fraction fraction)
    {
        return new Fraction(numerator.multiply(fraction.denominator).subtract(denominator.multiply(fraction.numerator)),
                            denominator.multiply(fraction.denominator));
    }

    /**
     * 分数乘法
     *
     * @param fraction 分数
     * @return 结果
     */
    public Fraction multiply(Fraction fraction)
    {
        return new Fraction(numerator.multiply(fraction.numerator),denominator.multiply(fraction.denominator));
    }

    /**
     * 分数除法
     *
     * @param fraction 分数
     * @return 结果
     */
    public Fraction divide(Fraction fraction)
    {
        return new Fraction(numerator.multiply(fraction.denominator),denominator.multiply(fraction.numerator));
    }

    /**
     * 分数取反
     *
     * @return 结果
     */
    public Fraction negate()
    {
        return new Fraction(numerator.negate(),denominator);
    }

    /**
     * 分数取倒数
     *
     * @return 结果
     */
    public Fraction reciprocal()
    {
        return new Fraction(denominator,numerator);
    }

    /**
     * 分数绝对值
     *
     * @return 结果
     */
    public Fraction abs()
    {
        return new Fraction(numerator.abs(),denominator.abs());
    }

    /**
     * 分数乘方
     *
     * @param exponent 指数
     */
    public Fraction pow(int exponent)
    {
        return new Fraction(numerator.pow(exponent),denominator.pow(exponent));
    }

    /**
     * 分数取max
     *
     * @param fraction 分数
     */
    public Fraction max(Fraction fraction)
    {
        return new Fraction(numerator.max(fraction.numerator),denominator.max(fraction.denominator));
    }

    /**
     * 分数取min
     *
     * @param fraction 分数
     */
    public Fraction min(Fraction fraction)
    {
        return new Fraction(numerator.min(fraction.numerator),denominator.min(fraction.denominator));
    }

    /**
     * 分数比较
     *
     * @param fraction 分数
     * @return 结果
     */
    @Override
    public boolean equals(Object fraction)
    {
        if (fraction instanceof Fraction)
        {
            return compareTo((Fraction) fraction)==0;
        }
        return false;
    }

    /**
     * 分数比较
     *
     * @param fraction 分数
     * @return 结果
     */
    public int compareTo(Fraction fraction)
    {
        return numerator.multiply(fraction.denominator).compareTo(denominator.multiply(fraction.numerator));
    }

    /**
     * 分数哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode()
    {
        return numerator.hashCode()*denominator.hashCode();
    }

    /**
     * 分数克隆
     *
     * @return 克隆
     */
    @Override
    public Fraction clone()
    {
        return new Fraction(numerator,denominator);
    }

    /**
     * 分数序列化
     *
     * @param out 输出流
     * @throws IOException IO异常
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeObject(numerator);
        out.writeObject(denominator);
    }

    /**
     * 分数反序列化
     *
     * @param in 输入流
     * @throws IOException            IO异常
     * @throws ClassNotFoundException 类未找到异常
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        numerator=(BigInteger) in.readObject();
        denominator=(BigInteger) in.readObject();
    }
}
