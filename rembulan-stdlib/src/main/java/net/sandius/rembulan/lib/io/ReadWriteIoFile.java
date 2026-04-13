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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.IoFile;

/**
 * A read/write IoFile backed by a single {@link SeekableByteChannel}.
 *
 * <p>
 * Used for Lua update modes: {@code "r+"}, {@code "w+"}, and {@code "a+"}. Both reads and writes
 * share the channel's file position, so seeks affect both. For {@code "a+"} semantics, set
 * {@code appendWrites} to {@code true}: each write will then atomically seek to end-of-file before
 * writing.
 * </p>
 *
 * <p>
 * Note: Java NIO does not allow opening a channel with both {@code READ} and {@code APPEND}
 * options, so {@code "a+"} append-on-write behaviour is emulated by this class.
 * </p>
 */
public class ReadWriteIoFile extends IoFile {

  private final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
  private final NumberTokenizer numberTokenizer = new NumberTokenizer(byteBuffer);
  private final LineTokenizer lineTokenizer = new LineTokenizer(byteBuffer);
  private final LineTokenizer lineWithEoLTokenizer = new LineTokenizer(byteBuffer, true);
  private final RestOfFileTokenizer restOfFileTokenizer = new RestOfFileTokenizer(byteBuffer);
  private final ChunkTokenizer chunkTokenizer = new ChunkTokenizer(byteBuffer);

  private final SeekableByteChannel channel;
  private final OutputStream out;
  private final boolean appendWrites;

  public ReadWriteIoFile(SeekableByteChannel channel, boolean appendWrites, Table metatable,
      Object userValue) {
    super(metatable, userValue);
    this.channel = Objects.requireNonNull(channel);
    this.out = Channels.newOutputStream(channel);
    this.appendWrites = appendWrites;
  }

  @Override
  public boolean isClosed() {
    return !channel.isOpen();
  }

  @Override
  public void close() throws IOException {
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
    if (appendWrites) {
      channel.position(channel.size());
    }
    s.writeTo(out);
  }

  @Override
  public long seek(Whence whence, long offset) throws IOException {
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
  public ByteString readLine(boolean returnEol) throws IOException {
    checkClosed();
    return returnEol ? lineWithEoLTokenizer.nextToken(channel) : lineTokenizer.nextToken(channel);
  }

  @Override
  public Number readNumber() throws IOException {
    checkClosed();
    return numberTokenizer.nextToken(channel);
  }

  @Override
  public ByteString readRestOfFile() throws IOException {
    checkClosed();
    return restOfFileTokenizer.nextToken(channel);
  }

  @Override
  public ByteString readChunk(long len) throws IOException {
    checkClosed();
    chunkTokenizer.setLength(len);
    return chunkTokenizer.nextToken(channel);
  }

}
