package net.sandius.rembulan.impl;

import net.sandius.rembulan.ByteString;

import java.util.ArrayList;
import java.util.List;

/**
 * Packs the constants of a compiled Lua function into strings that fit into the constant pool of
 * a class file, and unpacks them again when the class is initialised, used by compiled Lua
 * functions with many constants.
 *
 * <p>A class file limits each string constant to 65535 bytes, its constant pool to 65535 entries
 * and each method to 64 KB of bytecode. Storing each constant of a large chunk (for instance a
 * table literal with thousands of entries) in a field of its own exceeds these limits; packing
 * them into a few string chunks does not.</p>
 *
 * <p>The constants are integers ({@link Long}), floats ({@link Double}) and strings
 * ({@link ByteString}). Each is written as a tag character followed by its value: four characters
 * holding the 64 bits of a number, or two characters holding the length of a string followed by
 * its raw characters (see {@link ByteString#toRawString()}).</p>
 */
public final class PackedConstants {

	/**
	 * The maximum number of bytes of a string constant in a class file.
	 */
	public static final int MAX_CHUNK_BYTES = 65535;

	private static final char TAG_INTEGER = 'I';
	private static final char TAG_FLOAT = 'F';
	private static final char TAG_STRING = 'S';

	private PackedConstants() {
		// not to be instantiated
	}

	/**
	 * Packs the given constants into strings whose modified UTF-8 encoding fits into a string
	 * constant of a class file.
	 *
	 * @param constants  the constants: instances of {@link Long}, {@link Double}
	 *                   or {@link ByteString}
	 * @return  the packed constants
	 *
	 * @throws IllegalArgumentException  if a constant is of another type
	 */
	public static List<String> pack(List<?> constants) {
		StringBuilder packed = new StringBuilder();
		for (Object c : constants) {
			if (c instanceof Long) {
				packed.append(TAG_INTEGER);
				appendBits(packed, (Long) c);
			}
			else if (c instanceof Double) {
				packed.append(TAG_FLOAT);
				appendBits(packed, Double.doubleToRawLongBits((Double) c));
			}
			else if (c instanceof ByteString) {
				String raw = ((ByteString) c).toRawString();
				packed.append(TAG_STRING);
				packed.append((char) (raw.length() >>> 16));
				packed.append((char) raw.length());
				packed.append(raw);
			}
			else {
				throw new IllegalArgumentException("Illegal constant: " + c);
			}
		}
		return split(packed);
	}

	private static void appendBits(StringBuilder sb, long bits) {
		sb.append((char) (bits >>> 48));
		sb.append((char) (bits >>> 32));
		sb.append((char) (bits >>> 16));
		sb.append((char) bits);
	}

	// the number of bytes of c in modified UTF-8, the encoding of strings in class files
	private static int encodedLength(char c) {
		if (c >= 0x0001 && c <= 0x007f) return 1;
		else if (c <= 0x07ff) return 2;
		else return 3;
	}

	private static List<String> split(CharSequence packed) {
		List<String> chunks = new ArrayList<>();
		int start = 0;
		int bytes = 0;
		for (int i = 0; i < packed.length(); i++) {
			int len = encodedLength(packed.charAt(i));
			if (bytes + len > MAX_CHUNK_BYTES) {
				chunks.add(packed.subSequence(start, i).toString());
				start = i;
				bytes = 0;
			}
			bytes += len;
		}
		if (start < packed.length()) {
			chunks.add(packed.subSequence(start, packed.length()).toString());
		}
		return chunks;
	}

	/**
	 * Unpacks the constants packed by {@link #pack(List)}.
	 *
	 * @param chunks  the packed constants
	 * @return  the constants, in the order they were packed
	 *
	 * @throws IllegalArgumentException  if {@code chunks} are malformed
	 */
	public static Object[] unpack(String[] chunks) {
		StringBuilder sb = new StringBuilder();
		for (String chunk : chunks) {
			sb.append(chunk);
		}
		String packed = sb.toString();

		List<Object> constants = new ArrayList<>();
		int i = 0;
		while (i < packed.length()) {
			char tag = packed.charAt(i++);
			switch (tag) {
				case TAG_INTEGER:
					constants.add(bits(packed, i));
					i += 4;
					break;
				case TAG_FLOAT:
					constants.add(Double.longBitsToDouble(bits(packed, i)));
					i += 4;
					break;
				case TAG_STRING:
					int length = (packed.charAt(i) << 16) | packed.charAt(i + 1);
					i += 2;
					constants.add(ByteString.fromRaw(packed.substring(i, i + length)));
					i += length;
					break;
				default:
					throw new IllegalArgumentException("Illegal tag at index " + (i - 1) + ": " + tag);
			}
		}
		return constants.toArray();
	}

	private static long bits(String s, int i) {
		return ((long) s.charAt(i) << 48)
				| ((long) s.charAt(i + 1) << 32)
				| ((long) s.charAt(i + 2) << 16)
				| (long) s.charAt(i + 3);
	}

}
