/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 * Portions of this file are licensed under the Lua license. For Lua
 * licensing details, please visit
 *
 *     http://www.lua.org/license.html
 *
 * Copyright (C) 1994-2016 Lua.org, PUC-Rio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.sandius.rembulan.lib;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.ByteStringBuilder;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>This library provides basic support for UTF-8 encoding. It provides all its functions
 * inside the table {@code utf8}. This library does not provide any support for Unicode other
 * than the handling of the encoding. Any operation that needs the meaning of a character,
 * such as character classification, is outside its scope.</p>
 *
 * <p>Unless stated otherwise, all functions that expect a byte position as a parameter assume
 * that the given position is either the start of a byte sequence or one plus the length of the
 * subject string. As in the string library, negative indices count from the end
 * of the string.</p>
 */
public final class Utf8Lib {

	static final LuaFunction CHAR = new Char();
	static final LuaFunction CODES = new Codes();
	static final LuaFunction CODEPOINT = new CodePoint();
	static final LuaFunction LEN = new Len();
	static final LuaFunction OFFSET = new Offset();

	/**
	 * Returns the function {@code utf8.char}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code utf8.char (···)}
	 *
	 * <p>Receives zero or more integers, converts each one to its corresponding UTF-8
	 * byte sequence and returns a string with the concatenation of all these sequences.</p>
	 * </blockquote>
	 *
	 * @return  the {@code utf8.char} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-utf8.char">
	 *     the Lua 5.3 Reference Manual entry for <code>utf8.char</code></a>
	 */
	public static LuaFunction charFn() {
		return CHAR;
	}

	/**
	 * {@code utf8.charpattern}
	 *
	 * <p>The pattern (a string, not a function) "{@code [\0-\x7F\xC2-\xF4][\x80-\xBF]*}"
	 * (see §6.4.1), which matches exactly one UTF-8 byte sequence, assuming that the subject is
	 * a valid UTF-8 string.</p>
	 */
	public static final ByteString CHARPATTERN =
			ByteString.fromRaw("[\0-\u007F\u00C2-\u00F4][\u0080-\u00BF]*");

	/**
	 * Returns the function {@code utf8.codes}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code utf8.codes (s)}
	 *
	 * <p>Returns values so that the construction</p>
	 * <pre>
	 * {@code
	 * for p, c in utf8.codes(s) do body end
	 * }
	 * </pre>
	 * <p>will iterate over all characters in string {@code s}, with {@code p} being the position
	 * (in bytes) and {@code c} the code point of each character. It raises an error if it meets
	 * any invalid byte sequence.</p>
	 * </blockquote>
	 *
	 * @return  the {@code utf8.codes} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codes">
	 *     the Lua 5.3 Reference Manual entry for <code>utf8.codes</code></a>
	 */
	public static LuaFunction codes() {
		return CODES;
	}

	/**
	 * Returns the function {@code utf8.codepoint}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code utf8.codepoint (s [, i [, j]])}
	 *
	 * <p>Returns the codepoints (as integers) from all characters in {@code s} that start between
	 * byte position {@code i} and {@code j} (both included). The default for {@code i} is 1
	 * and for {@code j} is {@code i}. It raises an error if it meets any invalid byte
	 * sequence.</p>
	 * </blockquote>
	 *
	 * @return  the {@code utf8.XXX} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codepoint">
	 *     the Lua 5.3 Reference Manual entry for <code>utf8.codepoint</code></a>
	 */
	public static LuaFunction codepoint() {
		return CODEPOINT;
	}

	/**
	 * Returns the function {@code utf8.len}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code utf8.len (s [, i [, j]])}
	 *
	 * <p>Returns the number of UTF-8 characters in string {@code s} that start between positions
	 * {@code i} and {@code j} (both inclusive). The default for {@code i} is 1
	 * and for {@code j} is -1. If it finds any invalid byte sequence, returns a <b>false</b>
	 * value plus the position of the first invalid byte.</p>
	 * </blockquote>
	 *
	 * @return  the {@code utf8.len} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-utf8.len">
	 *     the Lua 5.3 Reference Manual entry for <code>utf8.len</code></a>
	 */
	public static LuaFunction len() {
		return LEN;
	}

	/**
	 * Returns the function {@code utf8.offset}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code utf8.offset (s, n [, i])}
	 *
	 * <p>Returns the position (in bytes) where the encoding of the {@code n}-th character
	 * of {@code s} (counting from position {@code i}) starts. A negative {@code n} gets
	 * characters before position {@code i}. The default for {@code i} is 1 when {@code n}
	 * is non-negative and {@code #s + 1} otherwise, so that {@code utf8.offset(s, -n)}
	 * gets the offset of the {@code n}-th character from the end of the string. If the specified
	 * character is neither in the subject nor right after its end, the function
	 * returns <b>nil</b>.</p>
	 *
	 * <p>As a special case, when {@code n} is 0 the function returns the start of the encoding
	 * of the character that contains the {@code i}-th byte of {@code s}.</p>
	 *
	 * <p>This function assumes that {@code s} is a valid UTF-8 string.</p>
	 * </blockquote>
	 *
	 * @return  the {@code utf8.offset} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-utf8.offset">
	 *     the Lua 5.3 Reference Manual entry for <code>utf8.offset</code></a>
	 */
	public static LuaFunction offset() {
		return OFFSET;
	}

	private Utf8Lib() {
		// not to be instantiated
	}

	/**
	 * Installs the UTF-8 library to the global environment {@code env} in the state
	 * context {@code context}.
	 *
	 * <p>If {@code env.package.loaded} is a table, adds the library table
	 * to it with the key {@code "utf8"}, using raw access.</p>
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(env);

		Table t = context.newTable();

		t.rawset("char", charFn());
		t.rawset("charpattern", CHARPATTERN);
		t.rawset("codes", codes());
		t.rawset("codepoint", codepoint());
		t.rawset("len", len());
		t.rawset("offset", offset());

		ModuleLib.install(env, "utf8", t);
	}

	private static final String INVALID_CODE = "invalid UTF-8 code";

	private static final int MAX_UNICODE = 0x10FFFF;

	// the largest code point that fits into one byte less: anything up to it is written longer than
	// it needs to be, which UTF-8 forbids. The first is for a continuation byte on its own.
	private static final int[] LIMITS = { 0xFF, 0x7F, 0x7FF, 0xFFFF };

	// Beyond the end there is nothing, as with the '\0' behind a string in C.
	private static boolean isContinuation(ByteString s, int pos) {
		return pos < s.length() && (s.byteAt(pos) & 0xC0) == 0x80;
	}

	private static long relativePosition(long pos, int len) {
		if (pos >= 0) {
			return pos;
		}
		else if (-pos > len) {
			return 0;
		}
		else {
			return len + pos + 1;
		}
	}

	// returns the code point of the sequence at pos, or -1 if it is invalid
	private static int decode(ByteString s, int pos) {
		int c = s.byteAt(pos) & 0xFF;
		if (c < 0x80) {
			return c;
		}

		int count = 0;
		int result = 0;
		while ((c & 0x40) != 0) {
			count += 1;
			if (!isContinuation(s, pos + count)) {
				return -1;
			}
			result = (result << 6) | (s.byteAt(pos + count) & 0x3F);
			c <<= 1;
		}
		result |= (c & 0x7F) << (count * 5);

		if (count > 3 || result > MAX_UNICODE || result <= LIMITS[count]) {
			return -1;
		}
		return result;
	}

	// the number of bytes of a code point
	private static int sequenceLength(int code) {
		if (code < 0x80) {
			return 1;
		}
		else if (code < 0x800) {
			return 2;
		}
		else if (code < 0x10000) {
			return 3;
		}
		else {
			return 4;
		}
	}

	private static void encode(int code, ByteStringBuilder bld) {
		int length = sequenceLength(code);
		if (length == 1) {
			bld.append((byte) code);
		}
		else {
			int lead = (0xFF00 >> length) & 0xFF;
			bld.append((byte) (lead | (code >> (6 * (length - 1)))));
			for (int shift = 6 * (length - 2); shift >= 0; shift -= 6) {
				bld.append((byte) (0x80 | ((code >> shift) & 0x3F)));
			}
		}
	}

	static class Char extends AbstractLibFunction {

		@Override
		protected String name() {
			return "char";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteStringBuilder bld = new ByteStringBuilder();
			for (int arg = 1; args.hasNext(); arg++) {
				long code = args.nextInteger();
				if (code < 0 || code > MAX_UNICODE) {
					throw new BadArgumentException(arg, name(), "value out of range");
				}
				encode((int) code, bld);
			}
			context.getReturnBuffer().setTo(bld.toByteString());
		}

	}

	static class Codes extends AbstractLibFunction {

		private static final LuaFunction ITERATOR = new CodesIterator();

		@Override
		protected String name() {
			return "codes";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			context.getReturnBuffer().setTo(ITERATOR, s, 0L);
		}

	}

	// gets the position of the last character, and returns the next one
	static class CodesIterator extends AbstractLibFunction {

		@Override
		protected String name() {
			return "codes iterator";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			long last = args.nextInteger() - 1;

			int len = s.length();
			long pos = Math.max(last, 0);
			if (last >= 0 && pos < len) {
				pos += 1;
				while (isContinuation(s, (int) pos)) {
					pos += 1;
				}
			}

			if (pos >= len) {
				context.getReturnBuffer().setTo();
			}
			else {
				int code = decode(s, (int) pos);
				if (code < 0 || isContinuation(s, (int) pos + sequenceLength(code))) {
					throw new LuaRuntimeException(INVALID_CODE);
				}
				context.getReturnBuffer().setTo(pos + 1, (long) code);
			}
		}

	}

	static class CodePoint extends AbstractLibFunction {

		@Override
		protected String name() {
			return "codepoint";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			int len = s.length();
			long first = relativePosition(args.nextOptionalInteger(1), len);
			long last = relativePosition(args.nextOptionalInteger(first), len);

			if (first < 1) {
				throw new BadArgumentException(2, name(), "out of range");
			}
			if (last > len) {
				throw new BadArgumentException(3, name(), "out of range");
			}

			List<Object> result = new ArrayList<>();
			long pos = first - 1;
			while (pos < last) {
				int code = decode(s, (int) pos);
				if (code < 0) {
					throw new LuaRuntimeException(INVALID_CODE);
				}
				result.add((long) code);
				pos += sequenceLength(code);
			}
			context.getReturnBuffer().setToContentsOf(result);
		}

	}

	static class Len extends AbstractLibFunction {

		@Override
		protected String name() {
			return "len";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			int len = s.length();
			long first = relativePosition(args.nextOptionalInteger(1), len);
			long last = relativePosition(args.nextOptionalInteger(-1), len);

			if (first < 1 || first - 1 > len) {
				throw new BadArgumentException(2, name(), "initial position out of string");
			}
			if (last > len) {
				throw new BadArgumentException(3, name(), "final position out of string");
			}

			long count = 0;
			long pos = first - 1;
			while (pos < last) {
				int code = decode(s, (int) pos);
				if (code < 0) {
					context.getReturnBuffer().setTo(null, pos + 1);
					return;
				}
				pos += sequenceLength(code);
				count += 1;
			}
			context.getReturnBuffer().setTo(count);
		}

	}

	static class Offset extends AbstractLibFunction {

		@Override
		protected String name() {
			return "offset";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			long n = args.nextInteger();
			int len = s.length();
			long start = relativePosition(args.nextOptionalInteger(n >= 0 ? 1 : len + 1), len);

			if (start < 1 || start - 1 > len) {
				throw new BadArgumentException(3, name(), "position out of range");
			}
			int pos = (int) start - 1;

			if (n == 0) {
				while (pos > 0 && isContinuation(s, pos)) {
					pos -= 1;
				}
			}
			else {
				if (isContinuation(s, pos)) {
					throw new LuaRuntimeException("initial position is a continuation byte");
				}
				if (n < 0) {
					while (n < 0 && pos > 0) {
						do {
							pos -= 1;
						} while (pos > 0 && isContinuation(s, pos));
						n += 1;
					}
				}
				else {
					n -= 1;
					while (n > 0 && pos < len) {
						do {
							pos += 1;
						} while (isContinuation(s, pos));
						n -= 1;
					}
				}
			}

			if (n == 0) {
				context.getReturnBuffer().setTo((long) pos + 1);
			}
			else {
				context.getReturnBuffer().setTo((Object) null);
			}
		}

	}

}
