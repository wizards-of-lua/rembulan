package net.sandius.rembulan.lib.io;

import java.nio.ByteBuffer;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;

public class NumberTokenizer extends AbstractTokenizer<Number> {

  boolean hasDot = false;
  boolean hasE = false;
  boolean trailing = false;

  public NumberTokenizer(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  @Override
  protected void reset() {
    hasDot = false;
    hasE = false;
    trailing = false;
  }

  @Override
  protected boolean handleByte(byte b) {
    if (b == LINE_FEED) {
      skip++;
      return false;
    } else if (Character.isWhitespace(b)) {
      if (output.size() > 0) {
        trailing = true;
      }
      skip++;
    } else {
      if (trailing) {
        return false;
      } else {
        // todo minus sign! one at the beginning, one after the e
        if (Character.isDigit(b)) {
          output.write(b);
        } else if ('.' == b && hasDot == false) {
          output.write(b);
        } else if ('e' == b && hasE == false) {
          output.write(b);
        } else {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  protected Number toResult() {
    return Conversions.numericalValueOf(ByteString.of(output));
  }

}
