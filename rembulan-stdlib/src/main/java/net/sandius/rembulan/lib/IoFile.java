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
    return "file (0x" + Integer.toHexString(hashCode()) + ")";
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

  private NextLine nextLine = new NextLine();

  public abstract boolean isClosed();

  public abstract void close() throws IOException;

  public abstract void flush() throws IOException;

  public abstract void write(ByteString s) throws IOException;

  public abstract ByteString readLine() throws IOException;

  public abstract Number readNumber() throws IOException;

  public abstract ByteString readRestOfFile() throws IOException;

  public abstract ByteString readChunk(long len) throws IOException;

  public enum Whence {
    BEGINNING, CURRENT_POSITION, END
  }
  public enum Format {
    AS_NUMBER, WHOLE_FILE, NEXT_LINE, NUMBER_OF_CHARACTERS;

    public static Format get(Object specifier) {
      if (ByteString.of("*n").equals(specifier)) {
        return Format.AS_NUMBER;
      }
      if (ByteString.of("*a").equals(specifier)) {
        return Format.WHOLE_FILE;
      }
      if (ByteString.of("*l").equals(specifier)) {
        return Format.NEXT_LINE;
      }
      if (specifier instanceof Number) {
        return Format.NUMBER_OF_CHARACTERS;
      }
      throw new IllegalArgumentException("Unknown format specifier: '" + specifier + "'");
    }
  }

  public abstract long seek(Whence whence, long position) throws IOException;

  ///

  class NextLine extends AbstractFunctionAnyArg {

    private Read READ = new Read();

    public NextLine() {}

    @Override
    public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
      READ.invoke(context, IoFile.this);
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
        f.close();
      } catch (ClosedFileException ex) {
        throw new LuaRuntimeException(ex);
      } catch (Exception ex) {
        IoLib.setErrorMessage(context.getReturnBuffer(), ex);
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
        throw new LuaRuntimeException(ex);
      } catch (Exception ex) {
        IoLib.setErrorMessage(context.getReturnBuffer(), ex);
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
      } catch (ClosedFileException ex) {
        throw new LuaRuntimeException(ex);
      } catch (Exception ex) {
        IoLib.setErrorMessage(context.getReturnBuffer(), ex);
        return;
      }
      NextLine nextLineFunc = f.nextLine;
      context.getReturnBuffer().setTo(nextLineFunc);
    }

  }

  static class Read extends AbstractLibFunction {

    private static final ByteString DEFAULT_SPEC = ByteString.of("*l");

    @Override
    protected String name() {
      return "read";
    }

    @Override
    protected void invoke(ExecutionContext context, ArgumentIterator args)
        throws ResolvedControlThrowable {
      final IoFile f = args.nextUserdata(typeName(), IoFile.class);
      List<Object> result = new ArrayList<>();
      try {
        do {
          Object formatSpecfifier = args.nextOptionalAny(DEFAULT_SPEC);
          Format format = Format.get(formatSpecfifier);
          switch (format) {
            case NEXT_LINE:
              ByteString line = f.readLine();
              result.add(line);
              break;
            case AS_NUMBER:
              Number number = f.readNumber();
              result.add(number);
              break;
            case WHOLE_FILE:
              ByteString text = f.readRestOfFile();
              result.add(text);
              break;
            case NUMBER_OF_CHARACTERS:
              Long len = Conversions.integerValueOf(formatSpecfifier);
              ByteString chunk = f.readChunk(len);
              result.add(chunk);
              break;
            default:
              throw new UnsupportedOperationException("Unsupported format: " + format);
          }
        } while (args.hasNext());
      } catch (ClosedFileException ex) {
        throw new LuaRuntimeException(ex);
      } catch (Exception ex) {
        IoLib.setErrorMessage(context.getReturnBuffer(), ex);
        return;
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
        throw new LuaRuntimeException(ex);
      } catch (Exception ex) {
        IoLib.setErrorMessage(context.getReturnBuffer(), ex);
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
      throw new UnsupportedOperationException(); // TODO
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

      while (args.hasNext()) {
        final ByteString s = args.nextString();
        try {
          f.write(s);
        } catch (ClosedFileException ex) {
          // Standard Lua 5.3 (checked on MacOS X) interrupts this function when the file
          // is closed.
          throw new LuaRuntimeException(ex);
        } catch (Exception ex) {
          IoLib.setErrorMessage(context.getReturnBuffer(), ex);
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

