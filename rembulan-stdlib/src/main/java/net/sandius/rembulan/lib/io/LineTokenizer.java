package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;

import net.sandius.rembulan.ByteString;

public class LineTokenizer extends AbstractTokenizer<ByteString> {

  public LineTokenizer(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  @Override
  protected void reset() {}

  @Override
  protected boolean handleByte(byte b) {
    if (b == LINE_FEED) {
      skip++;
      return false;
    } else {
      output.write(b);
    }
    return true;
  }

  @Override
  protected ByteString toResult(byte[] bytes) {
    return ByteString.copyOf(bytes);
  }

}
