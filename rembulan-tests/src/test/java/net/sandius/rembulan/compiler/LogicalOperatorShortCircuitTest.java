package net.sandius.rembulan.compiler;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

@FixMethodOrder(MethodSorters.JVM)
public class LogicalOperatorShortCircuitTest extends TestBase {

  @Test
  public void test_ternaryIdiom_falseCondition() throws Exception {
    // Given:
    String program = "return 5<=3 and 'string of success' or 'string of failure'";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual[0]).isEqualTo("string of failure");
  }

  @Test
  public void test_ternaryIdiom_trueCondition() throws Exception {
    // Given:
    String program = "return 5>=3 and 'string of success' or 'string of failure'";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual[0]).isEqualTo("string of success");
  }

  @Test
  public void test_ternaryIdiom_numericMiddleValue_trueCondition() throws Exception {
    // Regression: the AND result type is JOIN(BOOLEAN, NUMBER) = NON_NIL.
    // The old BranchInlinerVisitor incorrectly treated NON_NIL as always-truthy,
    // causing the OR to be eliminated even though NON_NIL includes false.
    // Given:
    String program = "local function f(v) return v == 42 and 0 or v end  return f(42), f(99)";

    // When:
    Object[] actual = run(program);

    // Then:
    assertThat(actual[0]).isEqualTo(0L);   // f(42): condition true, should return 0
    assertThat(actual[1]).isEqualTo(99L);  // f(99): condition false, should return v
  }
}
