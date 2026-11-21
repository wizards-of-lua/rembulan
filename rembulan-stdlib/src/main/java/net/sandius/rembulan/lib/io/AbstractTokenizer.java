package net.sandius.rembulan.lib.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public abstract class AbstractTokenizer<R> {

  protected static final byte LINE_FEED = (byte) '\n';

  protected final ByteBuffer byteBuffer;
  protected ByteArrayOutputStream output;
  protected long skip;

  public AbstractTokenizer(ByteBuffer byteBuffer) {
    super();
    this.byteBuffer = byteBuffer;
  }

  public final R nextToken(SeekableByteChannel channel) throws IOException {
    reset();
    output = new ByteArrayOutputStream();

    long mark = channel.position();
    skip = 0;

    while (true) {
      // clear, not rewind: a short read must not shrink the next one
      byteBuffer.clear();
      int bytesReadFromChannel = channel.read(byteBuffer);
      if (bytesReadFromChannel == -1) {
        break;
      }
      byteBuffer.flip();
      if (!byteBuffer.hasRemaining()) {
        continue;
      }

      int from = byteBuffer.arrayOffset() + byteBuffer.position();
      boolean doContinue = handleBytes(byteBuffer.array(), from, from + byteBuffer.remaining());
      if (!doContinue) {
        break;
      }
    }

    // the token took skip + output.size() bytes: the rest of the buffer is read again next time
    channel.position(mark + skip + output.size());

    if (!hasResult()) {
      return null;
    } else {
      return toResult();
    }
  }

  /**
   * Handles the bytes {@code buf[from]} to {@code buf[to - 1]} (at least one): writes those of
   * the token to {@link #output} and counts those it skips in {@link #skip}. The default hands
   * them to {@link #handleByte} one by one; tokenizers that take runs of bytes copy them at once.
   *
   * @return {@code true} if the token goes on after these bytes, {@code false} if it has ended
   */
  protected boolean handleBytes(byte[] buf, int from, int to) {
    for (int i = from; i < to; i++) {
      if (!handleByte(buf[i])) {
        return false;
      }
    }
    return true;
  }

  /** Whether the last token has a result, or else reading it failed at the end of the file. */
  protected boolean hasResult() {
    return output.size() != 0 || skip != 0;
  }

  protected abstract void reset();

  protected abstract boolean handleByte(byte b);

  protected abstract R toResult();

}
