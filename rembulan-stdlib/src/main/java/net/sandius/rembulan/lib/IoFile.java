/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.sandius.rembulan.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.DefaultUserdata;
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.runtime.AbstractFunctionAnyArg;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.ReturnBuffer;
import net.sandius.rembulan.runtime.WorkMeter;

/**
 * A file handle used by the {@link IoLib I/O library}.
 */
// Unless otherwise stated, all I/O functions return nil on failure (plus an error message
// as a second result and a system-dependent error code as a third result) and some value
// different from nil on success.
// See https://www.lua.org/manual/5.3/manual.html
public abstract class IoFile extends DefaultUserdata {

  protected IoFile(Table metatable, Object userValue) {
    super(metatable, userValue);
  }

  static String typeName() {
    return "FILE*";
  }

  @Override
  public String toString() {
    return isClosed() ? "file (closed)" : "file (0x" + Integer.toHexString(hashCode()) + ")";
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    // Note that files are automatically closed when their handles are garbage collected, but that
    // takes an unpredictable amount of time to happen.
    // see http://www.lua.org/manual/5.1/manual.html#5.7
    try {
      close();
    } catch (UnsupportedOperationException ex) {
      // ignore here
    }
  }

  protected void checkClosed() throws ClosedFileException {
    if (isClosed()) {
      throw new ClosedFileException();
    }
  }

  public abstract boolean isClosed();

  public abstract void close() throws IOException;

  public abstract void flush() throws IOException;

  public abstract void write(ByteString s) throws IOException;

  public abstract ByteString readLine(boolean returnEol) throws IOException;

  public abstract Number readNumber() throws IOException;

  public abstract ByteString readRestOfFile() throws IOException;

  public abstract ByteString readChunk(long len) throws IOException;

  /**
   * Sets the meter that charges the bytes the following reads take, so that the scheduler can
   * stop a read that takes too long; {@code null} for none. The default ignores it.
   */
  protected void setReadMeter(WorkMeter meter) {}

  public enum Whence {
    BEGINNING, CURRENT_POSITION, END
  }
  public enum Format {
    AS_NUMBER, WHOLE_FILE, NEXT_LINE, NEXT_LINE_WITH_EOL, NUMBER_OF_CHARACTERS;

    /**
     * Like Lua 5.3, the leading '*' is optional and only the first letter after it counts, so
     * "a", "*a" and "*all" all mean {@link #WHOLE_FILE}.
     */
    public static Format get(Object specifier) {
      if (specifier instanceof Number) {
        return Format.NUMBER_OF_CHARACTERS;
      }
      ByteString s = Conversions.stringValueOf(specifier);
      if (s != null) {
        int i = s.length() > 0 && s.byteAt(0) == '*' ? 1 : 0;
        if (i < s.length()) {
          switch (s.byteAt(i)) {
            case 'n':
              return Format.AS_NUMBER;
            case 'a':
              return Format.WHOLE_FILE;
            case 'l':
              return Format.NEXT_LINE;
            case 'L':
              return Format.NEXT_LINE_WITH_EOL;
          }
        }
      }
      throw new IllegalArgumentException("Unknown format specifier: '" + specifier + "'");
    }
  }

  public abstract long seek(Whence whence, long position) throws IOException;

  ///

  /**
   * The iterator of {@code file:lines} and {@code io.lines}: reads with the given formats on each
   * call and returns nothing at the end of the file, closing it if {@code closeAtEof}. Like Lua 5.3,
   * it raises an error when a read fails or the file is already closed.
   */
  static class LinesIterator extends AbstractFunctionAnyArg {

    // as in PUC-Lua: a bad format is argument #2 of the "for iterator"
    private final Read read = new Read("for iterator", 2);
    private final IoFile file;
    private final Object[] readArgs;
    private final boolean closeAtEof;

    LinesIterator(IoFile file, Object[] formats, boolean closeAtEof) {
      this.file = file;
      this.readArgs = new Object[formats.length + 1];
      this.readArgs[0] = file;
      System.arraycopy(formats, 0, this.readArgs, 1, formats.length);
      this.closeAtEof = closeAtEof;
    }

    @Override
    public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
      if (file.isClosed()) {
        throw new LuaRuntimeException("file is already closed");
      }
      read.invoke(context, readArgs);
      ReturnBuffer result = context.getReturnBuffer();
      if (result.size() > 0 && result.get0() != null) {
        return;
      }
      if (result.size() > 1) {
        // read stops at the first nil, so a second value is an error message
        throw new LuaRuntimeException(result.get1());
      }
      if (closeAtEof) {
        try {
          file.close();
        } catch (Exception ex) {
          throw IoErrors.error(ex);
        }
      }
      result.setTo();
    }

    @Override
    public void resume(ExecutionContext context, Object suspendedState)
        throws ResolvedControlThrowable {
      throw new NonsuspendableFunctionException(this.getClass());
    }

  }

  ///


  static class Close extends AbstractLibFunction {

    @Override
    protected String name() {
      return "close";
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      final IoFile f = args.nextUserdata(typeName(), IoFile.class);

      try {
        f.checkClosed();
        f.close();
      } catch (ClosedFileException ex) {
        throw IoErrors.error(ex);
      } catch (Exception ex) {
        IoErrors.setFailure(context.getReturnBuffer(), ex, null);
        return;
      }
      context.getReturnBuffer().setTo(true);
    }

  }

  static class Flush extends AbstractLibFunction {

    @Override
    protected String name() {
      return "flush";
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      final IoFile f = args.nextUserdata(typeName(), IoFile.class);
      try {
        f.flush();
      } catch (ClosedFileException ex) {
        throw IoErrors.error(ex);
      } catch (Exception ex) {
        IoErrors.setFailure(context.getReturnBuffer(), ex, null);
        return;
      }
      context.getReturnBuffer().setTo(true);
    }

  }

  static class Lines extends AbstractLibFunction {

    @Override
    protected String name() {
      return "lines";
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      final IoFile f = args.nextUserdata(typeName(), IoFile.class);
      try {
        f.checkClosed();
      } catch (Exception ex) {
        throw IoErrors.error(ex);
      }
      context.getReturnBuffer().setTo(new LinesIterator(f, args.copyRemaining(), false));
    }

  }

  static class Read extends AbstractLibFunction {

    private static final ByteString DEFAULT_SPEC = ByteString.of("*l");

    private final String name;
    private final int firstFormatArgument;

    /**
     * {@code file:read} and {@code io.read}: the first format is argument #1 (a method doesn't
     * count {@code self}).
     */
    Read() {
      this("read", 1);
    }

    /**
     * A read that reports a bad format as argument {@code firstFormatArgument} (the first
     * format) and so on of the function {@code name}.
     */
    Read(String name, int firstFormatArgument) {
      this.name = name;
      this.firstFormatArgument = firstFormatArgument;
    }

    @Override
    protected String name() {
      return name;
    }

    private BadArgumentException badFormat(int formatIndex, String message) {
      return new BadArgumentException(firstFormatArgument + formatIndex, name(), message);
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      final IoFile f = args.nextUserdata(typeName(), IoFile.class);
      List<Object> result = new ArrayList<>();
      try {
        f.checkClosed();
        f.setReadMeter(WorkMeter.forReadBytes(context));
        int formatIndex = -1;
        do {
          formatIndex++;
          Object formatSpecfifier = args.nextOptionalAny(DEFAULT_SPEC);
          final Format format;
          try {
            format = Format.get(formatSpecfifier);
          } catch (IllegalArgumentException ex) {
            throw badFormat(formatIndex, "invalid format");
          }
          final Object value;
          switch (format) {
            case NEXT_LINE:
              value = f.readLine(false);
              break;
            case NEXT_LINE_WITH_EOL:
              value = f.readLine(true);
              break;
            case AS_NUMBER:
              value = f.readNumber();
              break;
            case WHOLE_FILE:
              ByteString text = f.readRestOfFile();
              // "a" never fails: at the end of the file it reads the empty string
              value = text != null ? text : ByteString.empty();
              break;
            case NUMBER_OF_CHARACTERS:
              Long len = Conversions.integerValueOf(formatSpecfifier);
              if (len == null) {
                throw badFormat(formatIndex, "number has no integer representation");
              }
              value = f.readChunk(len);
              break;
            default:
              throw new UnsupportedOperationException("Unsupported format: " + format);
          }
          result.add(value);
          if (value == null) {
            // like Lua 5.3, stop at the first format that fails
            break;
          }
        } while (args.hasNext());
      } catch (BadArgumentException ex) {
        throw ex;
      } catch (ClosedFileException | IllegalArgumentException ex) {
        throw IoErrors.error(ex);
      } catch (LuaRuntimeException ex) {
        // e.g. the scheduler stopping a read that takes too long
        throw ex;
      } catch (Exception ex) {
        // e.g. reading a file opened for writing only
        IoErrors.setFailure(context.getReturnBuffer(), ex, null);
        return;
      } finally {
        f.setReadMeter(null);
      }
      context.getReturnBuffer().setToContentsOf(result);
    }
  }

  static class Seek extends AbstractLibFunction {

    @Override
    protected String name() {
      return "seek";
    }

    private static Whence stringToWhence(String s) {
      switch (s) {
        case "set":
          return Whence.BEGINNING;
        case "cur":
          return Whence.CURRENT_POSITION;
        case "end":
          return Whence.END;
        default:
          return null;
      }
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      IoFile file = args.nextUserdata(typeName(), IoFile.class);

      final Whence whence;
      final long offset;

      if (args.hasNext()) {
        String s = args.nextString().toString(); // FIXME
        Whence w = stringToWhence(s);
        if (w == null) {
          throw new BadArgumentException(1, name(), "invalid option '" + s + "'");
        }

        whence = w;
        offset = args.nextOptionalInteger(0L);
      } else {
        whence = Whence.CURRENT_POSITION;
        offset = 0L;
      }

      final long position;
      try {
        position = file.seek(whence, offset);
      } catch (ClosedFileException ex) {
        throw IoErrors.error(ex);
      } catch (Exception ex) {
        IoErrors.setFailure(context.getReturnBuffer(), ex, null);
        return;
      }

      context.getReturnBuffer().setTo(position);
    }

  }

  static class SetVBuf extends AbstractLibFunction {

    @Override
    protected String name() {
      return "setvbuf";
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      IoFile file = args.nextUserdata(typeName(), IoFile.class);
      try {
        file.checkClosed();
      } catch (ClosedFileException ex) {
        throw IoErrors.error(ex);
      }
      String mode = args.nextString().toString();
      if (!mode.equals("no") && !mode.equals("full") && !mode.equals("line")) {
        throw new BadArgumentException(1, name(), "invalid option '" + mode + "'");
      }
      args.nextOptionalInteger(0L); // the buffer size
      // The files keep their own buffering, so any mode is accepted as it is
      context.getReturnBuffer().setTo(true);
    }

  }

  static class Write extends AbstractLibFunction {

    @Override
    protected String name() {
      return "write";
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      final IoFile f = args.nextUserdata(typeName(), IoFile.class);
      // charges each string before it is written, so that the scheduler can stop a write that
      // would take too long before it has begun
      WorkMeter bytes = WorkMeter.forWriteBytes(context);

      while (args.hasNext()) {
        final ByteString s = args.nextString();
        bytes.steps(s.lengthEstimate());
        try {
          f.write(s);
        } catch (ClosedFileException ex) {
          // Standard Lua 5.3 (checked on MacOS X) interrupts this function when the file
          // is closed.
          throw IoErrors.error(ex);
        } catch (Exception ex) {
          IoErrors.setFailure(context.getReturnBuffer(), ex, null);
          return;
        }
      }
      context.getReturnBuffer().setTo(f);
    }

  }

  static class ToString extends AbstractLibFunction {

    @Override
    protected String name() {
      return "tostring";
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      IoFile f = args.nextUserdata(typeName(), IoFile.class);
      context.getReturnBuffer().setTo(f.toString());
    }

  }

}

