package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;

public class NumberTokenizer extends AbstractTokenizer<Number> {

  boolean hasDot = false;
  boolean hasE = false;
  boolean trailing = false;

  public NumberTokenizer(ByteBuffer byteBuffer, CharsetDecoder decoder, CharBuffer charBuffer) {
    super(byteBuffer, decoder, charBuffer);
  }

  @Override
  protected void reset() {
    hasDot = false;
    hasE = false;
    trailing = false;
  }

  @Override
  protected boolean handleChar(char ch) {
    if (ch == LINE_FEED) {
      skip++;
      return false;
    } else if (Character.isWhitespace(ch)) {
      if (builder.length() > 0) {
        trailing = true;
      }
      skip++;
    } else {
      if (trailing) {
        return false;
      } else {
        // todo minus sign! one at the beginning, one after the e
        if (Character.isDigit(ch)) {
          builder.append(ch);
        } else if ('.' == ch && hasDot == false) {
          builder.append(ch);
        } else if ('e' == ch && hasE == false) {
          builder.append(ch);
        } else {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  protected Number toResult(String text) {
    return Conversions.numericalValueOf(ByteString.of(text));
  }



}
