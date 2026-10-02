package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * A {@code ]} right after the {@code [} or {@code [^} that opens a set is a literal character in
 * the set, as in Lua 5.3, not the end of an empty set.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringPattern_SetTest extends TestBase {

  @Test
  public void test_closing_bracket_first_in_set() throws Exception {
    Object[] actual = run("return string.find('a]b', '[]]')");
    assertThat(actual).isEqualTo(new Object[] {2L, 2L});
  }

  @Test
  public void test_closing_bracket_first_in_negated_set() throws Exception {
    Object[] actual = run("return string.match(']]ab]', '[^]]+')");
    assertThat(actual).isEqualTo(new Object[] {"ab"});
  }

  @Test
  public void test_closing_bracket_first_with_more_elements() throws Exception {
    Object[] actual = run("return (string.gsub('[x]y', '[]x[]', '.'))");
    assertThat(actual).isEqualTo(new Object[] {"...y"});
  }

  @Test
  public void test_closing_bracket_starts_a_range() throws Exception {
    // ']' is 93, 'a' is 97: the range takes ']', '^', '_', '`' and 'a'
    Object[] actual = run("return (string.gsub(']^_`ab', '[]-a]', '.'))");
    assertThat(actual).isEqualTo(new Object[] {".....b"});
  }

  @Test
  public void test_closing_bracket_in_frontier() throws Exception {
    Object[] actual = run("return string.find('ab]', '%f[]]')");
    assertThat(actual).isEqualTo(new Object[] {3L, 2L});
  }

  @Test
  public void test_dash_before_closing_bracket_is_literal() throws Exception {
    Object[] actual = run("return (string.gsub('a-b', '[a-]', '.'))");
    assertThat(actual).isEqualTo(new Object[] {"..b"});
  }

  @Test
  public void test_unclosed_set_is_still_an_error() throws Exception {
    Object[] actual = run("return pcall(string.find, 'a', '[]')");
    assertThat(actual[0]).isEqualTo(Boolean.FALSE);
  }
}
