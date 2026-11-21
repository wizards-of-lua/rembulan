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
  protected boolean handleBytes(byte[] buf, int from, int to) {
    for (int i = from; i < to; i++) {
      if (buf[i] == LINE_FEED) {
        output.write(buf, from, i - from);
        if (returnEol) {
          output.write(LINE_FEED);
        } else {
          skip++;
        }
        return false;
      }
    }
    output.write(buf, from, to - from);
    return true;
  }

  @Override
  protected ByteString toResult() {
    return ByteString.of(output);
  }

}
