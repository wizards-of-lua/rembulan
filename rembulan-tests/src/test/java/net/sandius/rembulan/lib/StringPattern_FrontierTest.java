package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * The frontier pattern {@code %f[set]}: a complemented set, and the positions before the first
 * and after the last character, which count as {@code '\0'}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringPattern_FrontierTest extends TestBase {

  @Test
  public void test_frontier_at_start() throws Exception {
    Object[] actual = run("return string.find('5', '%f[%d]5')");
    assertThat(actual).isEqualTo(new Object[] {1L, 1L});
  }

  @Test
  public void test_frontier_mid_string() throws Exception {
    Object[] actual = run("return string.find('x5', '%f[%d]5')");
    assertThat(actual).isEqualTo(new Object[] {2L, 2L});
  }

  @Test
  public void test_complemented_frontier() throws Exception {
    Object[] actual = run("return string.find('5x', '5%f[^%d]')");
    assertThat(actual).isEqualTo(new Object[] {1L, 1L});
  }

  @Test
  public void test_complemented_frontier_at_end() throws Exception {
    Object[] actual = run("return string.find('5', '5%f[^%d]')");
    assertThat(actual).isEqualTo(new Object[] {1L, 1L});
  }

  @Test
  public void test_frontier_whole_words() throws Exception {
    Object[] actual = run("local t = {} for w in string.gmatch('THE (quick) fox', '%f[%a]%a+%f[%A]') do t[#t + 1] = w end return table.concat(t, ',')");
    assertThat(actual).isEqualTo(new Object[] {"THE,quick,fox"});
  }

  @Test
  public void test_frontier_whole_number() throws Exception {
    Object[] actual = run("return string.find('15 5', '%f[%d]5%f[^%d]')");
    assertThat(actual).isEqualTo(new Object[] {4L, 4L});
  }

  @Test
  public void test_frontier_with_zero_in_set() throws Exception {
    // '\0' is in the set: no frontier at the start (before it counts as '\0'), one at the end
    Object[] actual = run("return string.find('a', '%f[%z]')");
    assertThat(actual).isEqualTo(new Object[] {2L, 1L});
  }
}
