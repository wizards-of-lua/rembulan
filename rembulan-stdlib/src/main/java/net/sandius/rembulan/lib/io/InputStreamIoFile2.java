/*
 * Copyright 2016 Michael Karneim
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.IoFile;

public class InputStreamIoFile2 extends IoFile {

  private final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
  private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
  private final CharBuffer charBuffer = CharBuffer.wrap(new char[4]);

  private final NumberTokenizer numberTokenizer =
      new NumberTokenizer(byteBuffer, decoder, charBuffer);
  private final LineTokenizer lineTokenizer = new LineTokenizer(byteBuffer, decoder, charBuffer);

  private final InputStream in;
  private final SeekableByteChannel channel;

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
        break;
      case END:
        long size = channel.size();
        channel.position(size + offset);
        break;
      case CURRENT_POSITION:
        channel.position(current + offset);
        break;
      default:
        throw new IllegalArgumentException("Illegal whence: " + whence);
    }
    return channel.position();
  }

  ByteBuffer buffer0 = ByteBuffer.wrap(new byte[16]);

  @Override
  public String readLine() throws IOException {
    return lineTokenizer.nextToken(channel);
  }

  @Override
  public Number readNumber() throws IOException {
    return numberTokenizer.nextToken(channel);
  }

}
