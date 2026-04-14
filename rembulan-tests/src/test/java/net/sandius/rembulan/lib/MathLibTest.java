package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

@FixMethodOrder(MethodSorters.JVM)
public class MathLibTest extends TestBase {
  @Test
  public void test_math_type_with_integer() throws Exception {
    // Given:
    String program = "return math.type(1)";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"integer"});
  }

  @Test
  public void test_math_type_with_float() throws Exception {
    // Given:
    String program = "return math.type(1.1)";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"float"});
  }

  @Test
  public void test_math_type_with_nil() throws Exception {
    // Given:
    String program = "return math.type(nil)";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {null});
  }

  @Test
  public void test_math_type_with_string() throws Exception {
    // Given:
    String program = "return math.type('x')";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {null});
  }

  @Test
  public void test_math_atan_single_arg() throws Exception {
    // Given: math.atan(y) defaults x to 1, so it equals atan2(y, 1)
    String program = "return math.atan(1)";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {Math.PI / 4});
  }

  @Test
  public void test_math_atan_two_args_first_quadrant() throws Exception {
    // Given:
    String program = "return math.atan(1, 1)";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {Math.PI / 4});
  }

  @Test
  public void test_math_atan_two_args_negative_x() throws Exception {
    // Given: regression — the second argument was silently ignored, returning atan(0) = 0 instead
    // of pi
    String program = "return math.atan(0, -1)";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {Math.PI});
  }
}
