package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * The shortest repetition {@code -} must also try the longest run of matching characters, when
 * the rest of the pattern only matches after all of them.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringPattern_LazyRepeatTest extends TestBase {

  @Test
  public void test_lazy_up_to_eos() throws Exception {
    Object[] actual = run("return string.match('ab', '^(.-)$')");
    assertThat(actual).isEqualTo(new Object[] {"ab"});
  }

  @Test
  public void test_lazy_with_trailing_optional_space() throws Exception {
    // the trim idiom: nothing to trim
    Object[] actual = run("return string.match('SAY: Hi there', '^SAY:%s*(.-)%s*$')");
    assertThat(actual).isEqualTo(new Object[] {"Hi there"});
  }

  @Test
  public void test_lazy_with_trailing_space_to_trim() throws Exception {
    Object[] actual = run("return string.match('  hi  ', '^%s*(.-)%s*$')");
    assertThat(actual).isEqualTo(new Object[] {"hi"});
  }

  @Test
  public void test_lazy_class_consumes_all() throws Exception {
    Object[] actual = run("return string.match('aaa', '^(a-)$')");
    assertThat(actual).isEqualTo(new Object[] {"aaa"});
  }

  @Test
  public void test_lazy_still_shortest() throws Exception {
    Object[] actual = run("return string.match('a,b,c', '^(.-),')");
    assertThat(actual).isEqualTo(new Object[] {"a"});
  }

  @Test
  public void test_lazy_empty_subject() throws Exception {
    Object[] actual = run("return string.match('', '^(.-)$')");
    assertThat(actual).isEqualTo(new Object[] {""});
  }

  @Test
  public void test_gsub_lazy_to_end() throws Exception {
    Object[] actual = run("return string.gsub('key=value', '^(.-)=(.-)$', '%2=%1')");
    assertThat(actual).isEqualTo(new Object[] {"value=key", 1L});
  }
}
