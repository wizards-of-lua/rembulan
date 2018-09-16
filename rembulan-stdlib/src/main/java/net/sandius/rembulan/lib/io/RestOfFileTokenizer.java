package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;

import net.sandius.rembulan.ByteString;

public class RestOfFileTokenizer extends AbstractTokenizer<ByteString> {

  public RestOfFileTokenizer(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  @Override
  protected void reset() {}

  @Override
  protected boolean handleByte(byte b) {
    output.write(b);
    return true;
  }

  @Override
  protected ByteString toResult() {
    return ByteString.of(output);
  }

}
