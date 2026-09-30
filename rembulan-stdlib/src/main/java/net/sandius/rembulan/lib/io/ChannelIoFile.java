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
 * An IoFile backed by a {@link SeekableByteChannel}: a file opened by name, in any Lua mode, or a
 * temporary file.
 *
 * <p>
 * Reads and writes share the channel's file position, so seeks affect both. Reading a file that is
 * not readable, or writing one that is not writable, fails with "Bad file descriptor", like in
 * Lua.
 * </p>
 *
 * <p>
 * For {@code "a+"} semantics, set {@code appendWrites}: each write then seeks to the end of the
 * file first. Java NIO does not allow opening a channel with both {@code READ} and {@code APPEND}
 * options, so this emulates it.
 * </p>
 */
public class ChannelIoFile extends IoFile {

  private final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
  private final NumberTokenizer numberTokenizer = new NumberTokenizer(byteBuffer);
  private final LineTokenizer lineTokenizer = new LineTokenizer(byteBuffer);
  private final LineTokenizer lineWithEoLTokenizer = new LineTokenizer(byteBuffer, true);
  private final RestOfFileTokenizer restOfFileTokenizer = new RestOfFileTokenizer(byteBuffer);
  private final ChunkTokenizer chunkTokenizer = new ChunkTokenizer(byteBuffer);

  private final SeekableByteChannel channel;
  private final OutputStream out;
  private final boolean readable;
  private final boolean writable;
  private final boolean appendWrites;

  /** A file opened with {@code "r"}. */
  public static ChannelIoFile readOnly(SeekableByteChannel channel, Table metatable,
      Object userValue) {
    return new ChannelIoFile(channel, true, false, false, metatable, userValue);
  }

  /** A file opened with {@code "w"} or {@code "a"} (for which the channel appends). */
  public static ChannelIoFile writeOnly(SeekableByteChannel channel, Table metatable,
      Object userValue) {
    return new ChannelIoFile(channel, false, true, false, metatable, userValue);
  }

  /** A file opened with {@code "r+"}, {@code "w+"} or {@code "a+"} (with {@code appendWrites}). */
  public static ChannelIoFile readWrite(SeekableByteChannel channel, boolean appendWrites,
      Table metatable, Object userValue) {
    return new ChannelIoFile(channel, true, true, appendWrites, metatable, userValue);
  }

  private ChannelIoFile(SeekableByteChannel channel, boolean readable, boolean writable,
      boolean appendWrites, Table metatable, Object userValue) {
    super(metatable, userValue);
    this.channel = Objects.requireNonNull(channel);
    this.out = Channels.newOutputStream(channel);
    this.readable = readable;
    this.writable = writable;
    this.appendWrites = appendWrites;
  }

  private void checkReadable() throws IOException {
    checkClosed();
    if (!readable) {
      throw new UnsupportedOperationException("Bad file descriptor");
    }
  }

  private void checkWritable() throws IOException {
    checkClosed();
    if (!writable) {
      throw new UnsupportedOperationException("Bad file descriptor");
    }
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
    checkWritable();
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
    checkReadable();
    return returnEol ? lineWithEoLTokenizer.nextToken(channel) : lineTokenizer.nextToken(channel);
  }

  @Override
  public Number readNumber() throws IOException {
    checkReadable();
    return numberTokenizer.nextToken(channel);
  }

  @Override
  public ByteString readRestOfFile() throws IOException {
    checkReadable();
    return restOfFileTokenizer.nextToken(channel);
  }

  @Override
  public ByteString readChunk(long len) throws IOException {
    checkReadable();
    chunkTokenizer.setLength(len);
    return chunkTokenizer.nextToken(channel);
  }

}
