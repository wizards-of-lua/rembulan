package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.testenv.TestBase;

/**
 * A magic character where it has no special meaning is an ordinary character, as in PUC-Lua:
 * '$' not at the end, '^' not at the start, a quantifier with nothing before it, a lone ']'.
 * Only an unmatched ')' is an error, except in {@code string.find} with a pattern without special
 * characters, which is searched as plain text.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringPattern_MagicLiteralTest extends TestBase {

  @Test
  public void test_dollar_not_at_the_end() throws Exception {
    assertThat(run("return string.find('a$a', '.$.')")).isEqualTo(new Object[] {1L, 3L});
    assertThat(run("return string.find('x$', '$$')")).isEqualTo(new Object[] {2L, 2L});
  }

  @Test
  public void test_caret_not_at_the_start() throws Exception {
    assertThat(run("return string.find('a^b', 'a^b')")).isEqualTo(new Object[] {1L, 3L});
  }

  @Test
  public void test_quantifier_with_nothing_before_it() throws Exception {
    assertThat(run("return string.find('x*y', '*')")).isEqualTo(new Object[] {2L, 2L});
    assertThat(run("return string.match('a+b', '+')")).isEqualTo(new Object[] {"+"});
  }

  @Test
  public void test_lone_closing_bracket() throws Exception {
    assertThat(run("return string.find('a]b', ']')")).isEqualTo(new Object[] {2L, 2L});
  }

  @Test
  public void test_unmatched_closing_parenthesis_is_an_error() throws Exception {
    Object[] match = run("return pcall(string.match, 'a)b', ')')");
    assertThat(match[0]).isEqualTo(false);
    assertThat(String.valueOf(match[1])).contains("invalid pattern capture");
    Object[] find = run("return pcall(string.find, 'a)b', '%a)')");
    assertThat(find[0]).isEqualTo(false);
    assertThat(String.valueOf(find[1])).contains("invalid pattern capture");
  }

  @Test
  public void test_find_searches_a_pattern_without_specials_as_plain_text() throws Exception {
    // as PUC-Lua's nospecials: ')' and ']' aren't specials there
    assertThat(run("return string.find('a)b', ')')")).isEqualTo(new Object[] {2L, 2L});
    assertThat(run("return string.find('a)b]', 'b]')")).isEqualTo(new Object[] {3L, 4L});
  }

}
