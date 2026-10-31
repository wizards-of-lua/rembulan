package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * The {@code utf8} library of Lua 5.3, section 6.5 of its manual, down to the details of its
 * {@code lutf8lib.c}. "Käse😀" has 9 bytes: K at 1, ä at 2, s at 4, e at 5, 😀 at 6.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Utf8LibTest extends TestBase {

  private static final String TEXT = "local s = 'K\\xC3\\xA4se\\xF0\\x9F\\x98\\x80'; ";

  private void assertFails(String program, String message) {
    assertThatThrownBy(() -> run(program)).hasStackTraceContaining(message);
  }

  // char

  @Test
  public void test_char_of_nothing_is_empty() throws Exception {
    assertThat(run("return utf8.char()")).isEqualTo(new Object[] {""});
  }

  @Test
  public void test_char_takes_every_length() throws Exception {
    Object[] actual = run("return utf8.char(0x7F) == '\\x7F', utf8.char(0x80) == '\\xC2\\x80', "
        + "utf8.char(0x7FF) == '\\xDF\\xBF', utf8.char(0x800) == '\\xE0\\xA0\\x80', "
        + "utf8.char(0xFFFF) == '\\xEF\\xBF\\xBF', utf8.char(0x10000) == '\\xF0\\x90\\x80\\x80', "
        + "utf8.char(0x10FFFF) == '\\xF4\\x8F\\xBF\\xBF'");
    assertThat(actual).isEqualTo(new Object[] {true, true, true, true, true, true, true});
  }

  @Test
  public void test_char_of_zero_is_a_zero_byte() throws Exception {
    assertThat(run("return #utf8.char(0), utf8.char(0):byte()")).isEqualTo(new Object[] {1L, 0L});
  }

  @Test
  public void test_char_joins_its_arguments() throws Exception {
    assertThat(run("return utf8.char(75, 228, 115, 101)")).isEqualTo(new Object[] {"Käse"});
  }

  @Test
  public void test_char_out_of_range() {
    assertFails("return utf8.char(0x110000)", "value out of range");
    assertFails("return utf8.char(-1)", "value out of range");
  }

  // charpattern

  @Test
  public void test_charpattern_is_the_one_of_the_manual() throws Exception {
    assertThat(run("return utf8.charpattern == '[\\0-\\x7F\\xC2-\\xF4][\\x80-\\xBF]*'"))
        .isEqualTo(new Object[] {true});
  }

  @Test
  public void test_charpattern_walks_through_characters() throws Exception {
    Object[] actual = run(TEXT + "local sizes = {}; "
        + "for c in s:gmatch(utf8.charpattern) do sizes[#sizes + 1] = #c end; "
        + "return table.concat(sizes, ',')");
    assertThat(actual).isEqualTo(new Object[] {"1,2,1,1,4"});
  }

  // codes

  @Test
  public void test_codes_gives_positions_and_code_points() throws Exception {
    Object[] actual = run(TEXT + "local all = {}; "
        + "for p, c in utf8.codes(s) do all[#all + 1] = p .. ':' .. c end; "
        + "return table.concat(all, ' ')");
    assertThat(actual).isEqualTo(new Object[] {"1:75 2:228 4:115 5:101 6:128512"});
  }

  @Test
  public void test_codes_of_nothing_runs_zero_times() throws Exception {
    assertThat(run("local n = 0; for _ in utf8.codes('') do n = n + 1 end; return n"))
        .isEqualTo(new Object[] {0L});
  }

  @Test
  public void test_codes_fails_on_invalid_bytes() {
    assertFails("for p, c in utf8.codes('a\\xFF') do end", "invalid UTF-8 code");
    assertFails("for p, c in utf8.codes('\\xC3') do end", "invalid UTF-8 code");
    assertFails("for p, c in utf8.codes('a\\x80') do end", "invalid UTF-8 code");
  }

  // codepoint

  @Test
  public void test_codepoint_of_the_first_character_by_default() throws Exception {
    assertThat(run(TEXT + "return utf8.codepoint(s)")).isEqualTo(new Object[] {75L});
  }

  @Test
  public void test_codepoint_at_a_position() throws Exception {
    assertThat(run(TEXT + "return utf8.codepoint(s, 2)")).isEqualTo(new Object[] {228L});
  }

  @Test
  public void test_codepoint_from_the_end() throws Exception {
    assertThat(run("return utf8.codepoint('a\\xC3\\xA4', -2)")).isEqualTo(new Object[] {228L});
  }

  @Test
  public void test_codepoint_of_a_range() throws Exception {
    assertThat(run(TEXT + "return utf8.codepoint(s, 1, -1)"))
        .isEqualTo(new Object[] {75L, 228L, 115L, 101L, 128512L});
  }

  @Test
  public void test_codepoint_of_a_range_that_ends_inside_a_character() throws Exception {
    assertThat(run(TEXT + "return utf8.codepoint(s, 1, 2)")).isEqualTo(new Object[] {75L, 228L});
  }

  @Test
  public void test_codepoint_of_an_empty_range() throws Exception {
    assertThat(run(TEXT + "return select('#', utf8.codepoint(s, 3, 2))"))
        .isEqualTo(new Object[] {0L});
  }

  @Test
  public void test_codepoint_out_of_range() {
    assertFails(TEXT + "return utf8.codepoint(s, 0)", "out of range");
    assertFails(TEXT + "return utf8.codepoint(s, 1, 10)", "out of range");
  }

  @Test
  public void test_codepoint_fails_on_invalid_bytes() {
    assertFails("return utf8.codepoint('\\xFF')", "invalid UTF-8 code");
    assertFails(TEXT + "return utf8.codepoint(s, 3)", "invalid UTF-8 code");
  }

  // len

  @Test
  public void test_len_counts_characters() throws Exception {
    assertThat(run(TEXT + "return utf8.len(s), #s")).isEqualTo(new Object[] {5L, 9L});
  }

  @Test
  public void test_len_of_nothing() throws Exception {
    assertThat(run("return utf8.len('')")).isEqualTo(new Object[] {0L});
  }

  @Test
  public void test_len_of_a_part() throws Exception {
    assertThat(run(TEXT + "return utf8.len(s, 2), utf8.len(s, 2, 4), utf8.len(s, -4)"))
        .isEqualTo(new Object[] {4L, 2L, 1L});
  }

  @Test
  public void test_len_right_after_the_end() throws Exception {
    assertThat(run(TEXT + "return utf8.len(s, 10)")).isEqualTo(new Object[] {0L});
  }

  @Test
  public void test_len_finds_the_first_invalid_byte() throws Exception {
    assertThat(run("return utf8.len('ab\\xFFc')")).isEqualTo(new Object[] {null, 3L});
    assertThat(run(TEXT + "return utf8.len(s, 3)")).isEqualTo(new Object[] {null, 3L});
  }

  @Test
  public void test_len_refuses_what_utf8_forbids() throws Exception {
    // a character written longer than it needs to be, and one beyond U+10FFFF
    assertThat(run("return utf8.len('\\xC0\\x80')")).isEqualTo(new Object[] {null, 1L});
    assertThat(run("return utf8.len('\\xE0\\x80\\x80')")).isEqualTo(new Object[] {null, 1L});
    assertThat(run("return utf8.len('\\xF0\\x80\\x80\\x80')")).isEqualTo(new Object[] {null, 1L});
    assertThat(run("return utf8.len('\\xF4\\x90\\x80\\x80')")).isEqualTo(new Object[] {null, 1L});
    assertThat(run("return utf8.len('\\xF8\\x88\\x80\\x80\\x80')"))
        .isEqualTo(new Object[] {null, 1L});
  }

  @Test
  public void test_len_refuses_a_cut_character() throws Exception {
    assertThat(run("return utf8.len('a\\xF0\\x9F\\x98')")).isEqualTo(new Object[] {null, 2L});
  }

  @Test
  public void test_len_out_of_range() {
    assertFails(TEXT + "return utf8.len(s, 11)", "initial position out of string");
    assertFails(TEXT + "return utf8.len(s, 1, 10)", "final position out of string");
    assertFails(TEXT + "return utf8.len(s, 0)", "initial position out of string");
  }

  // offset

  @Test
  public void test_offset_of_the_nth_character() throws Exception {
    assertThat(run(TEXT + "return utf8.offset(s, 1), utf8.offset(s, 2), utf8.offset(s, 3), "
        + "utf8.offset(s, 5), utf8.offset(s, 6)"))
            .isEqualTo(new Object[] {1L, 2L, 4L, 6L, 10L});
  }

  @Test
  public void test_offset_beyond_the_end() throws Exception {
    assertThat(run(TEXT + "return utf8.offset(s, 7)")).isEqualTo(new Object[] {null});
  }

  @Test
  public void test_offset_from_a_position() throws Exception {
    assertThat(run(TEXT + "return utf8.offset(s, 2, 2)")).isEqualTo(new Object[] {4L});
  }

  @Test
  public void test_offset_from_the_end() throws Exception {
    assertThat(run(TEXT + "return utf8.offset(s, -1), utf8.offset(s, -4), utf8.offset(s, -5)"))
        .isEqualTo(new Object[] {6L, 2L, 1L});
    assertThat(run(TEXT + "return utf8.offset(s, -6)")).isEqualTo(new Object[] {null});
  }

  @Test
  public void test_offset_zero_finds_the_start_of_a_character() throws Exception {
    assertThat(run(TEXT + "return utf8.offset(s, 0, 3), utf8.offset(s, 0, 9), utf8.offset(s, 0)"))
        .isEqualTo(new Object[] {2L, 6L, 1L});
  }

  @Test
  public void test_offset_can_cut_a_string_by_characters() throws Exception {
    assertThat(run(TEXT + "return s:sub(1, utf8.offset(s, 3) - 1)"))
        .isEqualTo(new Object[] {"Kä"});
  }

  @Test
  public void test_offset_out_of_range() {
    assertFails(TEXT + "return utf8.offset(s, 1, 11)", "position out of range");
    assertFails(TEXT + "return utf8.offset(s, 1, 0)", "position out of range");
  }

  @Test
  public void test_offset_refuses_the_middle_of_a_character() {
    assertFails(TEXT + "return utf8.offset(s, 1, 3)", "initial position is a continuation byte");
  }

  // the string library next to it

  @Test
  public void test_surrogates_pass_as_in_lua_5_3() throws Exception {
    assertThat(run("return utf8.codepoint(utf8.char(0xD800))")).isEqualTo(new Object[] {0xD800L});
  }

  @Test
  public void test_numbers_count_as_strings() throws Exception {
    assertThat(run("return utf8.len(12)")).isEqualTo(new Object[] {2L});
  }
}
