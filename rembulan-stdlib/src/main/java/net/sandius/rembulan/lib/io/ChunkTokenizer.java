package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

public class ChunkTokenizer extends AbstractTokenizer<String> {

  private long length;
  private long count;

  public ChunkTokenizer(ByteBuffer byteBuffer, CharsetDecoder decoder, CharBuffer charBuffer) {
    super(byteBuffer, decoder, charBuffer);
  }

  public void setLength(long length) {
    this.length = length;
  }

  @Override
  protected void reset() {
    count = 0;
  }

  @Override
  protected boolean handleChar(char ch) {
    count++;
    if (count <= length) {
      builder.append(ch);
    }
    if (count > length) {
      skip++;      
    }
    return count < length;
  }

  @Override
  protected String toResult(String text) {
    return text;
  }

}
