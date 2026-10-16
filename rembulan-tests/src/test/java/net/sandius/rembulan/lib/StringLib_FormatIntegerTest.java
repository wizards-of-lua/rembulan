package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * {@code string.format} with {@code %d}, {@code %i}, {@code %u}, {@code %o}, {@code %x} and
 * {@code %X} must handle the flags {@code 0} and space as Lua 5.3 (C's printf) does. They were
 * mixed up: {@code %02d} gave " 8" for 8, and {@code % d} gave no space.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringLib_FormatIntegerTest extends TestBase {

  private void assertFormat(String format, String value, String expected) throws Exception {
    Object[] actual = run("return string.format('" + format + "', " + value + ")");
    assertThat(actual).isEqualTo(new Object[] {expected});
  }

  @Test
  public void test_zero_flag_pads_with_zeros() throws Exception {
    assertFormat("%02d", "8", "08");
    assertFormat("%02d", "32", "32");
    assertFormat("%05d", "42", "00042");
    assertFormat("%05i", "42", "00042");
    assertFormat("%02d", "8.0", "08");
  }

  @Test
  public void test_zero_flag_goes_after_the_sign() throws Exception {
    assertFormat("%03d", "-7", "-07");
    assertFormat("%+05d", "42", "+0042");
    assertFormat("% 05d", "42", " 0042");
  }

  @Test
  public void test_zero_flag_goes_after_the_prefix() throws Exception {
    assertFormat("%04x", "255", "00ff");
    assertFormat("%04X", "255", "00FF");
    assertFormat("%#06x", "255", "0x00ff");
    assertFormat("%05o", "8", "00010");
  }

  @Test
  public void test_zero_flag_is_ignored_with_minus_or_precision() throws Exception {
    assertFormat("%-05d", "42", "42   ");
    assertFormat("%05.3d", "42", "  042");
  }

  @Test
  public void test_space_flag_puts_a_space_before_a_positive_number() throws Exception {
    assertFormat("% d", "7", " 7");
    assertFormat("% d", "-7", "-7");
    assertFormat("%+ d", "7", "+7");
  }

  @Test
  public void test_without_flags_pads_with_spaces() throws Exception {
    assertFormat("%5d", "42", "   42");
    assertFormat("%d", "42", "42");
    assertFormat("%-3d|", "5", "5  |");
    assertFormat("%x", "255", "ff");
  }
}
