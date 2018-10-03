package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;

public class NumberTokenizer extends AbstractTokenizer<Number> {

  Byte lastByte = null;
  boolean hasDigit = false;
  boolean hasDot = false;
  boolean hasE = false;
  boolean isNegative = false;
  boolean isSignedExp = false;

  public NumberTokenizer(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  @Override
  protected void reset() {
    lastByte = null;
    hasDigit = false;
    hasDot = false;
    hasE = false;
    isNegative = false;
    isSignedExp = false;
  }

  @Override
  protected boolean handleByte(byte b) {
    try {
      if (b == LINE_FEED || Character.isWhitespace(b)) {
        if (output.size() > 0) {
          return false;
        }
        skip++;
      } else {
        if (Character.isDigit(b)) {
          output.write(b);
          hasDigit = true;
        } else if ('.' == b && !hasDot) {
          output.write(b);
          hasDot = true;
        } else if (('e' == b || 'E' == b) && !hasE && hasDigit) {
          output.write(b);
          hasE = true;
        } else if ('-' == b && !hasDigit && !isNegative) {
          output.write(b);
          isNegative = true;
        } else if (('-' == b || '+' == b) && (lastByte == 'e' || lastByte == 'E') && !isSignedExp) {
          output.write(b);
          isSignedExp = true;
        } else {
          return false;
        }
      }
      return true;
    } finally {
      lastByte = b;
    }
  }

  @Override
  protected Number toResult() {
    return Conversions.numericalValueOf(ByteString.of(output));
  }

}
