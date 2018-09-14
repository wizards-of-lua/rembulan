package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

public class RestOfFileTokenizer extends AbstractTokenizer<String> {

  public RestOfFileTokenizer(ByteBuffer byteBuffer, CharsetDecoder decoder, CharBuffer charBuffer) {
    super(byteBuffer, decoder, charBuffer);
  }

  @Override
  protected void reset() {}

  @Override
  protected boolean handleChar(char ch) {
    builder.append(ch);
    return true;
  }

  @Override
  protected String toResult(String text) {
    return text;
  }

}
