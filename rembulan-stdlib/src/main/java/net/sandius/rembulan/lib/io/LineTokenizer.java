package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;

import net.sandius.rembulan.ByteString;

public class LineTokenizer extends AbstractTokenizer<ByteString> {

  private boolean returnEol;

  public LineTokenizer(ByteBuffer byteBuffer) {
    this(byteBuffer, false);
  }

  public LineTokenizer(ByteBuffer byteBuffer, boolean returnEol) {
    super(byteBuffer);
    this.returnEol = returnEol;
  }

  @Override
  protected void reset() {}

  @Override
  protected boolean handleByte(byte b) {
    if (b == LINE_FEED) {
      if (returnEol) {
        output.write(b);
      } else {
        skip++;
      }
      return false;
    } else {
      output.write(b);
    }
    return true;
  }

  @Override
  protected ByteString toResult() {
    return ByteString.of(output);
  }

}
