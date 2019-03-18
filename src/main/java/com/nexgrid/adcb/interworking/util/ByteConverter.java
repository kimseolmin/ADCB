package com.nexgrid.adcb.interworking.util;

public class ByteConverter
{
	private static final byte[]	snull	= new byte[1];

	public static long initBytes(byte[] bin, int size, int init_value)
	{
		if (bin == null)
			bin = new byte[size];
		else if (bin.length < size)
			bin = new byte[size];

		bin[0] = (byte) init_value;

		int dst_pos = 1;
		while (dst_pos * 2 <= size)
		{
			System.arraycopy(bin, 0, bin, dst_pos, dst_pos);
			dst_pos *= 2;
		}

		System.arraycopy(bin, 0, bin, dst_pos, size - dst_pos);

		return bin.length;

	}

	public static long readHandle(byte[] bin, int offset) throws Exception
	{
		if (bin.length < offset + 6)
			throw new Exception("ByteFormat : Invalid Length");

		return (((0xffL & bin[offset + 0]) << 40) | ((0xffL & bin[offset + 1]) << 32) | ((0xffL & bin[offset + 2]) << 24) | ((0xffL & bin[offset + 3]) << 16) | ((0xffL & bin[offset + 4]) << 8) | ((0xffL & bin[offset + 5])));
	}

	public static long readPointerLong(byte[] bin, int offset) throws Exception
	{
		if (bin.length < offset + 6)
			throw new Exception("ByteFormat : Invalid Length");

		return (((0xffL & bin[offset + 0]) << 40) | ((0xffL & bin[offset + 1]) << 32) | ((0xffL & bin[offset + 2]) << 24) | ((0xffL & bin[offset + 3]) << 16) | ((0xffL & bin[offset + 4]) << 8) | ((0xffL & bin[offset + 5]))) << 5;
	}

	public static long readLong(byte[] bin, int offset) throws Exception
	{
		long l = 0;
		if (bin.length >= offset + 8)
		{
			l = ((0xffL & bin[offset + 0]) << 56) | ((0xffL & bin[offset + 1]) << 48) | ((0xffL & bin[offset + 2]) << 40) | ((0xffL & bin[offset + 3]) << 32) | ((0xffL & bin[offset + 4]) << 24)
					| ((0xffL & bin[offset + 5]) << 16) | ((0xffL & bin[offset + 6]) << 8) | ((0xffL & bin[offset + 7]));
		}
		else
			throw new Exception("ByteFormat : Invalid Length-readLong()");

		return l;
	}

	public static int readInt(byte[] bin, int offset) throws Exception
	{
		int l = 0;
		if (bin.length >= offset + 4)
		{
			l = ((0xff & bin[offset + 0]) << 24) | ((0xff & bin[offset + 1]) << 16) | ((0xff & bin[offset + 2]) << 8) | ((0xff & bin[offset + 3]));
		}
		else
			throw new Exception("ByteFormat : Invalid Length");
		return l;
	}

	public static short readShort(byte[] bin, int offset) throws Exception
	{
		short l = 0;
		if (bin.length >= offset + 2)
		{
			l = (short) (((0xff & bin[offset + 0]) << 8) | ((0xff & bin[offset + 1])));
		}
		else
			throw new Exception("ByteFormat : Invalid Length");
		return l;
	}

	public static char readChar(byte[] bin, int offset) throws Exception
	{
		char l = 0;
		if (bin.length >= offset + 1)
		{
			l = (char) ((0xff & bin[0]));
		}
		else
			throw new Exception("ByteFormat : Invalid Length");
		return l;
	}

	public static String readString(byte[] bin, int offset, int size) throws Exception
	{
		int len = readInt(bin, offset);
		return new String(bin, offset + 4, len);
	}

	public static void writeHandle(byte[] bout, int offset, long value) throws Exception
	{
		if (bout.length < offset + 6)
			throw new Exception("ByteFormat : Invalid Length");

		bout[offset + 0] = (byte) ((value & 0x0000ff0000000000L) >> 40);
		bout[offset + 1] = (byte) ((value & 0x000000ff00000000L) >> 32);
		bout[offset + 2] = (byte) ((value & 0x00000000ff000000L) >> 24);
		bout[offset + 3] = (byte) ((value & 0x0000000000ff0000L) >> 16);
		bout[offset + 4] = (byte) ((value & 0x000000000000ff00L) >> 8);
		bout[offset + 5] = (byte) ((value & 0x00000000000000ffL));
	}

	public static void writePointerLong(byte[] bout, int offset, long value) throws Exception
	{
		if (bout.length < offset + 6)
			throw new Exception("ByteFormat : Invalid Length");

		value = value >> 5;

		bout[offset + 0] = (byte) ((value & 0x0000ff0000000000L) >> 40);
		bout[offset + 1] = (byte) ((value & 0x000000ff00000000L) >> 32);
		bout[offset + 2] = (byte) ((value & 0x00000000ff000000L) >> 24);
		bout[offset + 3] = (byte) ((value & 0x0000000000ff0000L) >> 16);
		bout[offset + 4] = (byte) ((value & 0x000000000000ff00L) >> 8);
		bout[offset + 5] = (byte) ((value & 0x00000000000000ffL));
	}

	public static void writeLong(byte[] bout, int offset, long value) throws Exception
	{
		if (bout.length < offset + 8)
			throw new Exception("ByteFormat : Invalid Length");

		bout[offset + 0] = (byte) ((value & 0xff00000000000000L) >> 56);
		bout[offset + 1] = (byte) ((value & 0x00ff000000000000L) >> 48);
		bout[offset + 2] = (byte) ((value & 0x0000ff0000000000L) >> 40);
		bout[offset + 3] = (byte) ((value & 0x000000ff00000000L) >> 32);
		bout[offset + 4] = (byte) ((value & 0x00000000ff000000L) >> 24);
		bout[offset + 5] = (byte) ((value & 0x0000000000ff0000L) >> 16);
		bout[offset + 6] = (byte) ((value & 0x000000000000ff00L) >> 8);
		bout[offset + 7] = (byte) ((value & 0x00000000000000ffL));
	}

	public static void writeInt(byte[] bout, int offset, int value) throws Exception
	{
		if (bout.length < offset + 4)
			throw new Exception("ByteFormat : Invalid Length");

		bout[offset + 0] = (byte) ((value & 0xff000000) >> 24);
		bout[offset + 1] = (byte) ((value & 0x00ff0000) >> 16);
		bout[offset + 2] = (byte) ((value & 0x0000ff00) >> 8);
		bout[offset + 3] = (byte) ((value & 0x000000ff));
	}

	public static void writeShort(byte[] bout, int offset, short value) throws Exception
	{
		if (bout.length < offset + 2)
			throw new Exception("ByteFormat : Invalid Length");

		bout[offset + 0] = (byte) ((value & 0xff00) >> 8);
		bout[offset + 1] = (byte) ((value & 0x00ff));
	}

	public static void writeChar(byte[] bout, int offset, String value) throws Exception
	{
		if (bout.length < offset + 1)
			throw new Exception("ByteFormat : Invalid Length");
		bout[0] = (byte) (Long.decode("0x" + value) & 0xff);
	}

	public static void writeString(byte[] bout, int offset, int size, String str) throws Exception
	{
		byte[] sin = null;
		int len = 0;

		if (str == null)
			sin = snull;
		else
		{
			sin = str.getBytes();
			len = sin.length;
		}

		if (len > size)
			throw new Exception("ByteFormat : Invalid Length");

		writeInt(bout, offset, len);

		if (len > 0)
			System.arraycopy(sin, 0, bout, offset + 4, len);
	}

	public static boolean equals(byte[] src, byte[] trg, boolean btrim_allow)
	{
		if (src == null || trg == null || src.length <= 0 || trg.length <= 0)
			return false;

		int src_start = 0;
		int trg_start = 0;
		int src_end = src.length - 1;
		int trg_end = trg.length - 1;

		if (btrim_allow == true)
		{
			for (int i = 0; i < src.length; i++)
			{
				if (src[i] == ' ' || src[i] == '\n' || src[i] == '\r' || src[i] == '\t')
					src_start++;
				else
					break;
			}
			for (int i = 0; i < trg.length; i++)
			{
				if (trg[i] == ' ' || trg[i] == '\n' || trg[i] == '\r' || trg[i] == '\t')
					trg_start++;
				else
					break;
			}
			for (int i = src_end; i > src_start; i--)
			{
				if (src[i] == ' ' || src[i] == '\n' || src[i] == '\r' || src[i] == '\t')
					src_end--;
				else
					break;
			}
			for (int i = trg_end; i > trg_start; i--)
			{
				if (trg[i] == ' ' || trg[i] == '\n' || trg[i] == '\r' || trg[i] == '\t')
					trg_end--;
				else
					break;
			}
		}

		if (trg_end - trg_start != src_end - src_start)
			return false;

		while (src_start <= src_end)
		{
			if (src[src_start++] != trg[trg_start++])
				return false;
		}

		return true;
	}

	public static int compare(byte[] src, byte[] trg, boolean btrim_allow)
	{
		if (src == null || trg == null || src.length <= 0 || trg.length <= 0)
			return 0;

		int ret = 0;
		int src_start = 0;
		int trg_start = 0;
		int src_end = src.length - 1;
		int trg_end = trg.length - 1;

		if (btrim_allow == true)
		{
			for (int i = 0; i < src.length; i++)
			{
				if (src[i] == ' ' || src[i] == '\n' || src[i] == '\r' || src[i] == '\t')
					src_start++;
				else
					break;
			}
			for (int i = 0; i < trg.length; i++)
			{
				if (trg[i] == ' ' || trg[i] == '\n' || trg[i] == '\r' || trg[i] == '\t')
					trg_start++;
				else
					break;
			}
			for (int i = src_end; i > src_start; i--)
			{
				if (src[i] == ' ' || src[i] == '\n' || src[i] == '\r' || src[i] == '\t')
					src_end--;
				else
					break;
			}
			for (int i = trg_end; i > trg_start; i--)
			{
				if (trg[i] == ' ' || trg[i] == '\n' || trg[i] == '\r' || trg[i] == '\t')
					trg_end--;
				else
					break;
			}
		}

		if (trg_end - trg_start > src_end - src_start)
			ret = -1;
		else if (trg_end - trg_start < src_end - src_start)
			ret = 1;

		while (src_start <= src_end)
		{
			int d = src[src_start++] - trg[trg_start++];
			if (d != 0)
				return d;
		}

		return ret;
	}

	public static void print(byte[] bin, int size)
	{
		System.out.print("[");
		for (int i = 0; i < size; i++)
		{
			if (i != 0)
				System.out.print(",");
			System.out.print((int) bin[i]);
		}

		System.out.println("]");

	}

	public static String readFixedString(byte[] bin, int offset, int size)
	{
		String str = new String(bin, offset, size);
		return str.trim();
	}

	public static void writeFixedString(byte[] data, int offset, int size, String str)
	{
		byte[] si = null;

		if (str == null || str.length() == 0)
			si = new byte[size];
		else
			si = str.getBytes();

		System.arraycopy(si, 0, data, offset, (size > si.length) ? si.length : size);
	}

}
