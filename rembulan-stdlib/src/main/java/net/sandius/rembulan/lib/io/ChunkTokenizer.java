package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;

import net.sandius.rembulan.ByteString;

public class ChunkTokenizer extends AbstractTokenizer<ByteString> {

  private long length;
  private long count;
  private boolean peeked;

  public ChunkTokenizer(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  public void setLength(long length) {
    this.length = length;
  }

  @Override
  protected void reset() {
    count = 0;
    peeked = false;
  }

  @Override
  protected boolean handleByte(byte b) {
    if (length <= 0) {
      // read(0) only tests for the end of the file, so the byte stays unread
      peeked = true;
      return false;
    }
    output.write(b);
    count++;
    return count < length;
  }

  @Override
  protected boolean hasResult() {
    return super.hasResult() || peeked;
  }

  @Override
  protected ByteString toResult() {
    return ByteString.of(output);
  }

}
