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

package net.sandius.rembulan.lib.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.IoFile;

public class InputStreamIoFile2 extends IoFile {

  private static final char LINE_FEED = '\n';
  private static final char CARRIAGE_RETURN = '\r';

  private final InputStream in;
  private final SeekableByteChannel channel;
  private BufferedReader buffer;

  public InputStreamIoFile2(InputStream in, SeekableByteChannel channel, Table metatable,
      Object userValue) {
    super(metatable, userValue);
    this.in = Objects.requireNonNull(in);
    this.channel = Objects.requireNonNull(channel);
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }

  @Override
  public void flush() throws IOException {
    // no-op
  }

  @Override
  public void write(ByteString s) throws IOException {
    throw new UnsupportedOperationException("Bad file descriptor");
  }

  @Override
  public long seek(IoFile.Whence whence, long offset) throws IOException {
    long current = channel.position();
    switch (whence) {
      case BEGINNING:
        channel.position(offset);
      case END:
        long size = channel.size();
        channel.position(size - offset);
      case CURRENT_POSITION:
        channel.position(current + offset);
        break;
      default:
        throw new IllegalArgumentException("Illegal whence: " + whence);
    }
    if (current != channel.position()) {
      buffer = null;
    }
    return channel.position();
  }

  @Override
  public String readLine() throws IOException {
    if (buffer == null) {
      buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
    long mark = channel.position();

    StringBuilder builder = new StringBuilder();
    long skip = 0;
    boolean carriageReturn = false;
    while (true) {
      int c = buffer.read();
      if (c == -1) {
        break;
      } else {
        char ch = (char) c;
        if (ch == CARRIAGE_RETURN) {
          skip++;
          carriageReturn = true;
        } else if (ch == LINE_FEED) {
          skip++;
          break;
        } else if (carriageReturn) {
          break;
        } else {
          builder.append(ch);
        }
      }
    }

    channel.position(mark + skip + builder.length());
    buffer = null;

    if (builder.length() == 0 && skip == 0) {
      return null;
    } else {
      String text = builder.toString();
      return text;
    }
  }

  @Override
  public Number readNumber() throws IOException {
    if (buffer == null) {
      buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
    long mark = channel.position();

    StringBuilder builder = new StringBuilder();
    boolean hasDot = false;
    boolean hasE = false;
    boolean trailing = false;
    boolean carriageReturn = false;
    long skip = 0;
    while (true) {
      int c = buffer.read();
      if (c == -1) {
        break;
      } else {
        char ch = (char) c;
        if (ch == CARRIAGE_RETURN) {
          skip++;
          carriageReturn = true;
        } else if (ch == LINE_FEED) {
          skip++;
          break;
        } else if (carriageReturn) {
          break;
        } else if (Character.isWhitespace(ch)) {
          if (builder.length() > 0) {
            trailing = true;
          }
          skip++;
        } else {
          if (trailing) {
            break;
          } else {
            if (Character.isDigit(ch)) {
              builder.append(ch);
            } else if ('.' == ch && hasDot == false) {
              builder.append(ch);
            } else if ('e' == ch && hasE == false) {
              builder.append(ch);
            } else {
              break;
            }
          }
        }
        // todo minus sign! one at the beginning, one after the e
      }
    }

    channel.position(mark + skip + builder.length());
    buffer = null;

    if (builder.length() == 0) {
      return null;
    } else {
      String text = builder.toString();
      return toNumber(text);
    }
  }

  private Number toNumber(String text) {
    return Conversions.numericalValueOf(ByteString.of(text));
  }

}
