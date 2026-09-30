/*
 * Copyright 2026 Michael Karneim
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
import java.nio.channels.Channels;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * A read-only channel over a stream, such as standard input, for the {@link ChannelReader}.
 *
 * <p>
 * A stream cannot go back, but a reader reads ahead and then sets the position back to where its
 * token ended. So this channel keeps the bytes it has read from the stream after its position was
 * last set, and can go back to any of them. Positions before that are gone, and the end of a
 * stream is unknown: seeking there fails with "Illegal seek".
 * </p>
 *
 * <p>
 * A read from the stream blocks at most once, so reading a line from a terminal does not wait for
 * more input than that line.
 * </p>
 */
class InputStreamChannel implements SeekableByteChannel {

  private final ReadableByteChannel in;
  private byte[] buffer = new byte[64];
  /** The stream position of {@code buffer[0]}. */
  private long bufferStart;
  private int bufferLength;
  private long position;
  private boolean open = true;

  InputStreamChannel(InputStream in) {
    this.in = Channels.newChannel(in);
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    if (dst.remaining() == 0) {
      return 0;
    }
    if (position == bufferStart + bufferLength) {
      // everything buffered was read, so take more from the stream
      if (buffer.length - bufferLength < dst.remaining()) {
        buffer = Arrays.copyOf(buffer, Math.max(buffer.length * 2, bufferLength + dst.remaining()));
      }
      int n = in.read(ByteBuffer.wrap(buffer, bufferLength, dst.remaining()));
      if (n <= 0) {
        return n;
      }
      bufferLength += n;
    }
    int offset = (int) (position - bufferStart);
    int n = Math.min(dst.remaining(), bufferLength - offset);
    dst.put(buffer, offset, n);
    position += n;
    return n;
  }

  @Override
  public long position() {
    return position;
  }

  @Override
  public SeekableByteChannel position(long newPosition) throws IOException {
    if (newPosition < bufferStart || newPosition > bufferStart + bufferLength) {
      throw new IOException("Illegal seek");
    }
    // nothing before the new position can be read again
    int dropped = (int) (newPosition - bufferStart);
    System.arraycopy(buffer, dropped, buffer, 0, bufferLength - dropped);
    bufferLength -= dropped;
    bufferStart = newPosition;
    position = newPosition;
    return this;
  }

  @Override
  public long size() throws IOException {
    throw new IOException("Illegal seek");
  }

  @Override
  public int write(ByteBuffer src) {
    throw new NonWritableChannelException();
  }

  @Override
  public SeekableByteChannel truncate(long size) {
    throw new NonWritableChannelException();
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void close() throws IOException {
    open = false;
    in.close();
  }

}
