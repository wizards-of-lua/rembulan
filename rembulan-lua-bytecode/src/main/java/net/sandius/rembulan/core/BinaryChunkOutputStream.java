package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Objects;

public class BinaryChunkOutputStream extends FilterOutputStream {

	protected final boolean bigEndian;

	protected final boolean intIs32Bit;
	protected final boolean sizeTIs32Bit;
	protected final boolean instructionIs32Bit;
	protected final boolean luaIntegerIs32Bit;
	protected final boolean luaFloatIs32Bit;

	private static boolean bitWidthIs32Bit(int bitWidth) {
		if (bitWidth == 4) return true;
		else if (bitWidth == 8) return false;
		else throw new IllegalArgumentException("Illegal bit width: " + bitWidth + ", expected 4 or 8");
	}

	public BinaryChunkOutputStream(OutputStream out, ByteOrder byteOrder, int sizeOfInt, int sizeOfSizeT, int sizeOfInstruction, int sizeOfLuaInteger, int sizeOfLuaFloat) {
		super(out);

		this.bigEndian = Objects.requireNonNull(byteOrder) == ByteOrder.BIG_ENDIAN;

		this.intIs32Bit = bitWidthIs32Bit(sizeOfInt);
		this.sizeTIs32Bit = bitWidthIs32Bit(sizeOfSizeT);
		this.instructionIs32Bit = bitWidthIs32Bit(sizeOfInstruction);
		this.luaIntegerIs32Bit = bitWidthIs32Bit(sizeOfLuaInteger);
		this.luaFloatIs32Bit = bitWidthIs32Bit(sizeOfLuaFloat);
	}

	public ByteOrder byteOrder() {
		return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
	}

	public void writeHeader() throws IOException {
		write(BinaryChunkHeader.SIGNATURE.getBytes());
		write(BinaryChunkHeader.VERSION);
		write(BinaryChunkHeader.FORMAT);
		write(BinaryChunkHeader.TAIL.getBytes());

		write(intIs32Bit ? 4 : 8);
		write(sizeTIs32Bit ? 4 : 8);
		write(instructionIs32Bit ? 4 : 8);
		write(luaIntegerIs32Bit ? 4 : 8);
		write(luaFloatIs32Bit ? 4 : 8);

		writeInteger(BinaryChunkHeader.BYTE_ORDER_TEST_INTEGER);
		writeFloat(BinaryChunkHeader.BYTE_ORDER_TEST_FLOAT);

		// true: function follows
		writeBoolean(true);
	}

	public void writeBoolean(boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	public void writeInt32(int v) throws IOException {
		if (bigEndian) {
			write((v >>> 24) & 0xFF);
			write((v >>> 16) & 0xFF);
			write((v >>>  8) & 0xFF);
			write((v >>>  0) & 0xFF);
		}
		else {
			write((v >>>  0) & 0xFF);
			write((v >>>  8) & 0xFF);
			write((v >>> 16) & 0xFF);
			write((v >>> 24) & 0xFF);
		}
	}

	public void writeInt64(long v) throws IOException {
		byte[] buf = new byte[8];

		if (bigEndian) {
			buf[0] = (byte)(v >>> 56);
			buf[1] = (byte)(v >>> 48);
			buf[2] = (byte)(v >>> 40);
			buf[3] = (byte)(v >>> 32);
			buf[4] = (byte)(v >>> 24);
			buf[5] = (byte)(v >>> 16);
			buf[6] = (byte)(v >>>  8);
			buf[7] = (byte)(v >>>  0);
		}
		else {
			buf[0] = (byte)(v >>>  0);
			buf[1] = (byte)(v >>>  8);
			buf[2] = (byte)(v >>> 16);
			buf[3] = (byte)(v >>> 24);
			buf[4] = (byte)(v >>> 32);
			buf[5] = (byte)(v >>> 40);
			buf[6] = (byte)(v >>> 48);
			buf[7] = (byte)(v >>> 56);
		}

		write(buf, 0, 8);
	}

	public void writeInt(int v) throws IOException {
		if (intIs32Bit) {
			writeInt32(v);
		}
		else {
			writeInt64(v);
		}
	}

	public void writeSizeT(int v) throws IOException {
		if (sizeTIs32Bit) {
			writeInt32(v);
		}
		else {
			writeInt64(v);
		}
	}

	public void writeInteger(long v) throws IOException {
		if (luaIntegerIs32Bit) {
			if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
				writeInt32((int) v);
			}
			else {
				throw new IllegalArgumentException("Integer cannot be represented as 32-bit: 0x" + v);
			}
		}
		else {
			writeInt64(v);
		}
	}

	public void writeFloat(double v) throws IOException {
		if (luaFloatIs32Bit) {
			writeInt32(Float.floatToIntBits((float) v));
		}
		else {
			writeInt64(Double.doubleToLongBits(v));
		}
	}

	public void writeString(String s) throws IOException {
		Objects.requireNonNull(s);

		byte[] bytes = s.getBytes();

		if (bytes.length < 0xff) {
			writeShortString(bytes);
		}
		else {
			writeLongString(bytes);
		}
	}

	public void writeShortString(byte[] bytes) throws IOException {
		Check.gt(bytes.length, 0xff - 1);
		write(bytes.length);
		writeStringBody(bytes);
	}

	public void writeLongString(byte[] bytes) throws IOException {
		write(0xff);
		writeSizeT(bytes.length);
		writeStringBody(bytes);
	}

	protected void writeStringBody(byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
		// no need to write the trailing '\0'
	}

}
