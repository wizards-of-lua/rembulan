package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.testenv.TestBase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringLib_Issue_4_Test extends TestBase {
  @Test
  public void _test_a_match_a() throws Exception {
    // Given:
    String program = "return string.match(\"a\",\"a\")";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"a"});
  }

  @Test
  public void _test_1_match_not_1() throws Exception {

    // Given:
    String program = "return string.match(\"1\",\"[^1]\")";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {null});
  }

  @Test
  public void _test_1_match_not_0() throws Exception {

    // Given:
    String program = "return string.match(\"1\",\"[^0]\")";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"1"});
  }

  @Test
  public void _test_1_match_1() throws Exception {

    // Given:
    String program = "return string.match(\"1\",\"[1]\")";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"1"});
  }

  @Test
  public void _test_1_match_0() throws Exception {

    // Given:
    String program = "return string.match(\"1\",\"[0]\")";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {null});
  }

  @Test
  public void _test_plus_match_plus() throws Exception {

    // Given:
    String program = "return string.match(\"+\",\"[+]\")";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {null});
  }

  @Test
  public void test() throws Exception {
    // Given:
    String program = "string.match(tostring(0.5), \"([^05+])\")";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"."});
  }
}
