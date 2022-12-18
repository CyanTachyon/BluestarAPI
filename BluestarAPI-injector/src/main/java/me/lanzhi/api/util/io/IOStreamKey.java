package me.lanzhi.api.util.io;

public abstract class IOStreamKey
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
}
