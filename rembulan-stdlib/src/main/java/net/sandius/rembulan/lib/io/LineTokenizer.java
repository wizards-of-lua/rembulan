package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

public class LineTokenizer extends AbstractTokenizer<String> {

  public LineTokenizer(ByteBuffer byteBuffer, CharsetDecoder decoder, CharBuffer charBuffer) {
    super(byteBuffer, decoder, charBuffer);
  }

  @Override
  protected void reset() {}

  @Override
  protected boolean handleChar(char ch) {
    if (ch == LINE_FEED) {
      skip++;
      return false;
    } else {
      builder.append(ch);
    }
    return true;
  }

  @Override
  protected String toResult(String text) {
    return text;
  }

}
