package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;

import net.sandius.rembulan.ByteString;

public class ChunkTokenizer extends AbstractTokenizer<ByteString> {

  private long length;
  private long count;

  public ChunkTokenizer(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  public void setLength(long length) {
    this.length = length;
  }

  @Override
  protected void reset() {
    count = 0;
  }

  @Override
  protected boolean handleByte(byte b) {
    count++;
    if (count <= length) {
      output.write(b);
    }
    if (count > length) {
      skip++;
    }
    return count < length;
  }

  @Override
  protected ByteString toResult(byte[] bytes) {
    return ByteString.copyOf(bytes);
  }

}
