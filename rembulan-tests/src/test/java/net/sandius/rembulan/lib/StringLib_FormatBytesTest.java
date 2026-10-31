package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * {@code string.format} works on bytes, as in Lua 5.3: strings needn't be text, and a width or a
 * precision counts bytes.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringLib_FormatBytesTest extends TestBase {

  @Test
  public void test_percent_s_keeps_every_byte() throws Exception {
    Object[] actual = run("local s = '\\xFF\\0\\xFE'; local r = string.format('%s', s); "
        + "return r == s, #r");
    assertThat(actual).isEqualTo(new Object[] {true, 3L});
  }

  @Test
  public void test_format_string_keeps_every_byte() throws Exception {
    Object[] actual = run("return string.format('\\xFF%d\\xFE', 1) == '\\xFF1\\xFE'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }

  @Test
  public void test_text_stays_text() throws Exception {
    Object[] actual = run("return string.format('K\\xC3\\xA4se %s', 'K\\xC3\\xA4se')");
    assertThat(actual).isEqualTo(new Object[] {"Käse Käse"});
  }

  @Test
  public void test_width_counts_bytes() throws Exception {
    Object[] actual = run("return string.format('%4s', '\\xC3\\xA4') == '  \\xC3\\xA4', "
        + "string.format('%-4s', '\\xC3\\xA4') == '\\xC3\\xA4  '");
    assertThat(actual).isEqualTo(new Object[] {true, true});
  }

  @Test
  public void test_precision_counts_bytes() throws Exception {
    Object[] actual = run("local r = string.format('%.1s', '\\xC3\\xA4'); return #r, r:byte()");
    assertThat(actual).isEqualTo(new Object[] {1L, 0xC3L});
  }

  @Test
  public void test_percent_c_is_one_byte() throws Exception {
    Object[] actual = run("local r = string.format('%c', 200); return #r, r:byte()");
    assertThat(actual).isEqualTo(new Object[] {1L, 200L});
  }

  @Test
  public void test_tostring_may_return_bytes() throws Exception {
    Object[] actual = run("local t = setmetatable({}, { __tostring = function () return '\\xFF' end }); "
        + "return string.format('%s', t) == '\\xFF', string.format('%2s', t) == ' \\xFF'");
    assertThat(actual).isEqualTo(new Object[] {true, true});
  }

  @Test
  public void test_percent_s_after_a_function_that_yields() throws Exception {
    Object[] actual = run("local t = setmetatable({}, { __tostring = function () "
        + "coroutine.yield(); return '\\xFE' end }); "
        + "local c = coroutine.wrap(function () return string.format('\\xFF%s', t) end); "
        + "c(); return c() == '\\xFF\\xFE'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }
}
