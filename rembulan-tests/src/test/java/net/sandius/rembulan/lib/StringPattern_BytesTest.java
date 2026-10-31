package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * Patterns work on bytes, as in Lua 5.3, like {@code #}, {@code string.sub} and
 * {@code string.byte}: in {@code "K\xC3\xA4se"} the letter {@code s} is at position 4, and a
 * {@code .} takes one byte. Strings needn't be text, so what doesn't take part in a match comes
 * back byte by byte.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringPattern_BytesTest extends TestBase {

  private static final String UMLAUT = "'\\xC3\\xA4'";

  @Test
  public void test_find_counts_bytes() throws Exception {
    Object[] actual = run("return string.find(" + UMLAUT + " .. 'x', 'x')");
    assertThat(actual).isEqualTo(new Object[] {3L, 3L});
  }

  @Test
  public void test_plain_find_counts_bytes() throws Exception {
    Object[] actual = run("return string.find(" + UMLAUT + " .. 'x', 'x', 1, true)");
    assertThat(actual).isEqualTo(new Object[] {3L, 3L});
  }

  @Test
  public void test_init_counts_bytes() throws Exception {
    Object[] actual = run("return string.find(" + UMLAUT + " .. 'x' .. " + UMLAUT + " .. 'x', 'x', 4)");
    assertThat(actual).isEqualTo(new Object[] {6L, 6L});
  }

  @Test
  public void test_position_of_find_fits_sub() throws Exception {
    Object[] actual = run("local s = 'K\\xC3\\xA4se'; return s:sub(s:find('se'))");
    assertThat(actual).isEqualTo(new Object[] {"se"});
  }

  @Test
  public void test_position_capture_counts_bytes() throws Exception {
    Object[] actual = run("return string.match(" + UMLAUT + " .. 'x', '()x')");
    assertThat(actual).isEqualTo(new Object[] {3L});
  }

  @Test
  public void test_dot_takes_one_byte() throws Exception {
    Object[] actual = run("return string.byte(string.match(" + UMLAUT + ", '.'))");
    assertThat(actual).isEqualTo(new Object[] {0xC3L});
  }

  @Test
  public void test_bytes_of_a_character_in_a_set_are_two_elements() throws Exception {
    Object[] actual = run("return string.gsub('a' .. " + UMLAUT + " .. 'b', '[\\xC3\\xA4]', '')");
    assertThat(actual).isEqualTo(new Object[] {"ab", 2L});
  }

  @Test
  public void test_range_of_bytes() throws Exception {
    Object[] actual = run("return string.find('x\\xF0', '[\\192-\\255]'), "
        + "string.find('x\\x80', '[\\192-\\255]')");
    assertThat(actual).isEqualTo(new Object[] {2L, null});
  }

  @Test
  public void test_complement_of_a_set_takes_high_bytes() throws Exception {
    Object[] actual = run("return string.find('a\\xFF', '[^a]')");
    assertThat(actual).isEqualTo(new Object[] {2L, 2L});
  }

  @Test
  public void test_a_pattern_for_one_character_of_utf8() throws Exception {
    Object[] actual = run("return select(2, string.gsub('a\\xC3\\xA4\\xF0\\x9F\\x98\\x80', "
        + "'[\\0-\\x7F\\xC2-\\xF4][\\x80-\\xBF]*', ''))");
    assertThat(actual).isEqualTo(new Object[] {3L});
  }

  @Test
  public void test_gsub_without_a_match_leaves_every_byte() throws Exception {
    Object[] actual = run("local s = '\\xFF\\0\\xFE'; local r, n = string.gsub(s, 'z', 'y'); "
        + "return r == s, #r, n");
    assertThat(actual).isEqualTo(new Object[] {true, 3L, 0L});
  }

  @Test
  public void test_gsub_leaves_the_bytes_around_a_match() throws Exception {
    Object[] actual = run("return string.gsub('\\xFFa\\xFE', 'a', 'b') == '\\xFFb\\xFE'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }

  @Test
  public void test_gsub_with_bytes_in_the_replacement() throws Exception {
    Object[] actual = run("return string.gsub('a', 'a', '\\xFF') == '\\xFF'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }

  @Test
  public void test_gsub_with_whole_match_and_capture_in_the_replacement() throws Exception {
    Object[] actual = run("return string.gsub('\\xFF', '.', '%0%0') == '\\xFF\\xFF', "
        + "string.gsub('\\xFEa', '(.)a', '%1%1') == '\\xFE\\xFE'");
    assertThat(actual).isEqualTo(new Object[] {true, true});
  }

  @Test
  public void test_gsub_with_a_position_in_the_replacement() throws Exception {
    Object[] actual = run("return (string.gsub(" + UMLAUT + " .. 'x', '()x', '%1'))"
        + " == " + UMLAUT + " .. '3'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }

  @Test
  public void test_gsub_with_a_function_gets_and_gives_bytes() throws Exception {
    Object[] actual = run("local got; "
        + "local r = string.gsub('\\xFF', '.', function (c) got = c; return c .. '\\xFE' end); "
        + "return got == '\\xFF', r == '\\xFF\\xFE'");
    assertThat(actual).isEqualTo(new Object[] {true, true});
  }

  @Test
  public void test_gsub_with_a_function_that_declines_leaves_the_bytes() throws Exception {
    Object[] actual = run("return string.gsub('\\xFF', '.', function () end) == '\\xFF'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }

  @Test
  public void test_gsub_with_a_table_looks_up_bytes() throws Exception {
    Object[] actual = run("return string.gsub('\\xFF', '.', { ['\\xFF'] = '\\xFE' }) == '\\xFE'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }

  @Test
  public void test_match_returns_bytes() throws Exception {
    Object[] actual = run("return string.match('\\xFFa', '.') == '\\xFF', "
        + "string.match('\\xFEa', '(.)a') == '\\xFE'");
    assertThat(actual).isEqualTo(new Object[] {true, true});
  }

  @Test
  public void test_find_returns_captures_as_bytes() throws Exception {
    Object[] actual = run("return select(3, string.find('\\xFFa', '(.)a')) == '\\xFF'");
    assertThat(actual).isEqualTo(new Object[] {true});
  }

  @Test
  public void test_gmatch_returns_bytes() throws Exception {
    Object[] actual = run("local bytes = {}; "
        + "for c in string.gmatch('\\xFF\\xFE', '.') do bytes[#bytes + 1] = c:byte() end; "
        + "return #bytes, bytes[1], bytes[2]");
    assertThat(actual).isEqualTo(new Object[] {2L, 0xFFL, 0xFEL});
  }

  @Test
  public void test_gmatch_returns_captures_as_bytes() throws Exception {
    Object[] actual = run("for a, b in string.gmatch('\\xFFa', '(.)(.)') do "
        + "return a == '\\xFF', b end");
    assertThat(actual).isEqualTo(new Object[] {true, "a"});
  }

  @Test
  public void test_back_reference_compares_bytes() throws Exception {
    Object[] actual = run("return string.find('\\xFF\\xFF', '(.)%1') , "
        + "string.find('\\xFF\\xFE', '(.)%1')");
    assertThat(actual[0]).isEqualTo(1L);
    assertThat(actual[1]).isNull();
  }

  @Test
  public void test_balanced_with_bytes() throws Exception {
    Object[] actual = run("return string.find('a\\xFFb\\xFEc', '%b\\xFF\\xFE')");
    assertThat(actual).isEqualTo(new Object[] {2L, 4L});
  }

  @Test
  public void test_frontier_with_bytes() throws Exception {
    Object[] actual = run("return string.find('a\\xFF', '%f[\\xFF]')");
    assertThat(actual).isEqualTo(new Object[] {2L, 1L});
  }

  @Test
  public void test_text_stays_text() throws Exception {
    Object[] actual = run("return string.gsub('K\\xC3\\xA4se', 's', 'S')");
    assertThat(actual).isEqualTo(new Object[] {"KäSe", 1L});
  }
}
