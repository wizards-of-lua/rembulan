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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.IoFile;

public class OutputStreamIoFile2 extends IoFile {

  private final OutputStream out;
  private final SeekableByteChannel channel;

  public OutputStreamIoFile2(OutputStream out, SeekableByteChannel channel, Table metatable,
      Object userValue) {
    super(metatable, userValue);
    this.out = out;
    this.channel = channel;
  }

  @Override
  public boolean isClosed() {
    return !channel.isOpen();
  }

  @Override
  public void close() throws IOException {
    out.close();
    channel.close();
  }

  @Override
  public void flush() throws IOException {
    checkClosed();
    out.flush();
  }

  @Override
  public void write(ByteString s) throws IOException {
    checkClosed();
    s.writeTo(out);
  }

  @Override
  public long seek(IoFile.Whence whence, long offset) throws IOException {
    checkClosed();
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

  @Override
  public ByteString readLine() throws IOException {
    throw new UnsupportedOperationException("Bad file descriptor");
  }

  @Override
  public Number readNumber() throws IOException {
    throw new UnsupportedOperationException("Bad file descriptor");
  }

  @Override
  public ByteString readRestOfFile() throws IOException {
    throw new UnsupportedOperationException("Bad file descriptor");
  }

  @Override
  public ByteString readChunk(long len) throws IOException {
    throw new UnsupportedOperationException("Bad file descriptor");
  }

}
