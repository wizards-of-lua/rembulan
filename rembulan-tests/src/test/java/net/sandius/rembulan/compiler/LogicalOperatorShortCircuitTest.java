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
}
