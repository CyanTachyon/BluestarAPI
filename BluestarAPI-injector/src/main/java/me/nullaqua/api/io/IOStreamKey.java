package me.nullaqua.api.io;

import me.nullaqua.api.collection.ByteVector;

import java.io.Serializable;

public abstract class IOStreamKey implements Serializable
{
    public static final IOStreamKey EmptyKey=new IOStreamKey(1)
    {
        @Override
        public byte[] encrypt(byte b)
        {
            return new byte[]{b};
        }

        @Override
        public byte decrypt(byte[] bytes)
        {
            return bytes[0];
        }
    };
    private final int encryptNum;

    public IOStreamKey(int encryptNum)
    {
        this.encryptNum=encryptNum;
    }

    public abstract byte[] encrypt(byte b);

    public abstract byte decrypt(byte[] bytes);

    public int encryptNum()
    {
        return encryptNum;
    }

    public static class XorKey extends IOStreamKey
    {
        private final byte key;

        public XorKey(byte key)
        {
            super(1);
            this.key=key;
        }

        @Override
        public byte[] encrypt(byte bt)
        {
            return new byte[]{(byte) (bt^key)};
        }

        @Override
        public byte decrypt(byte[] bytes)
        {
            return (byte) (bytes[0]^key);
        }

        @Override
        public int encryptNum()
        {
            return 1;
        }
    }

    public static IOStreamKey plus(IOStreamKey... keys)
    {
        if (keys==null||keys.length==0)
        {
            return EmptyKey;
        }
        int sum=1;
        for (var o: keys)
        {
            sum*=o.encryptNum();
        }
        return new IOStreamKey(sum)
        {
            @Override
            public byte[] encrypt(byte b)
            {
                var res=new ByteVector();
                res.put(b);
                for (var o: keys)
                {
                    var x=new ByteVector();
                    for (var b0: res.toByteArray())
                    {
                        x.put(o.encrypt(b0));
                    }
                    res=x;
                }
                return res.toByteArray();
            }

            @Override
            public byte decrypt(byte[] bytes)
            {
                for (int i=keys.length-1;i>=0;i--)
                {
                    var bytes1=new byte[bytes.length/keys[i].encryptNum()][keys[i].encryptNum];
                    var res=new ByteVector();
                    for (int j=0;j<bytes.length;j++)
                    {
                        bytes1[j/keys[i].encryptNum()][j%keys[i].encryptNum()]=bytes[j];
                    }
                    for (var x: bytes1)
                    {
                        res.put(keys[i].decrypt(x));
                    }
                    bytes=res.toByteArray();
                }
                return bytes[0];
            }
        };
    }

    public static class HexKey extends IOStreamKey
    {
        public HexKey()
        {
            super(2);
        }

        @Override
        public byte[] encrypt(byte b)
        {
            return new byte[]{toHex((byte) (b>>4)),toHex(b)};
        }

        @Override
        public byte decrypt(byte[] bytes)
        {
            return (byte) ((fromHex(bytes[0])<<4)|fromHex(bytes[1]));
        }

        private static byte fromHex(byte x)
        {
            if (x>='0'&&x<='9')
            {
                return x-='0';
            }
            return x-='A'-10;
        }

        private static byte toHex(byte x)
        {
            x&=0xf;
            if (x>9)
            {
                return x+='A'-10;
            }
            return x+='0';
        }
    }
}
