package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringPattern_EosTest extends TestBase {

  @Test
  public void test_eos_matches_end_of_string() throws Exception {
    // $ must match at the very end of the string
    String program = "return string.match('hello', 'o$')";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {"o"});
  }

  @Test
  public void test_eos_no_match_when_not_at_end() throws Exception {
    // $ must not match when the pattern does not reach the end
    String program = "return string.match('hello', 'e$')";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {null});
  }

  @Test
  public void test_eos_full_string_anchor() throws Exception {
    // Both ^ and $ anchors together
    String program = "return string.match('hello', '^hello$')";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {"hello"});
  }

  @Test
  public void test_eos_full_string_anchor_no_match() throws Exception {
    // ^ and $ together must reject a partial match
    String program = "return string.match('hello world', '^hello$')";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {null});
  }

  @Test
  public void test_eos_single_char_string() throws Exception {
    // $ on a single-character string
    String program = "return string.match('a', 'a$')";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {"a"});
  }

  @Test
  public void test_gsub_with_eos_anchor() throws Exception {
    // string.gsub should use $ correctly
    String program = "return string.gsub('hello', 'o$', '!')";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {"hell!", 1L});
  }

  @Test
  public void test_find_with_eos_anchor() throws Exception {
    // string.find should return correct positions with $ anchor
    String program = "return string.find('hello', 'o$')";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {5L, 5L});
  }
}
