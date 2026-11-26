package net.sandius.rembulan.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.Variable;
import net.sandius.rembulan.impl.DefaultTable;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.testenv.TestBase;

/**
 * A parenthesised expression is a value, not a variable: it can't be assigned to (#58). The
 * expected messages are PUC-Lua 5.3.6's.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssignmentTargetTest extends TestBase {

  /** The message of the error compiling {@code source} as chunk "t" gives. */
  private static String errorOf(String source) {
    try {
      CompilerChunkLoader.of("AssignmentTargetTest")
          .loadTextChunk(new Variable(new DefaultTable()), "t", source);
    }
    catch (LoaderException ex) {
      return ex.getLuaStyleErrorMessage();
    }
    throw new AssertionError("compiles: " + source);
  }

  @Test
  public void test_parenthesised_variable_is_no_target() {
    assertThat(errorOf("(a) = 1")).isEqualTo("t:1: syntax error near '='");
    assertThat(errorOf("((a)) = 1")).isEqualTo("t:1: syntax error near '='");
    assertThat(errorOf("(a.b) = 1")).isEqualTo("t:1: syntax error near '='");
  }

  @Test
  public void test_parenthesised_variable_is_no_target_in_a_multiple_assignment() {
    assertThat(errorOf("a.b, (c) = 1, 2")).isEqualTo("t:1: syntax error near '='");
    assertThat(errorOf("(a), b = 1, 2")).isEqualTo("t:1: syntax error near ','");
  }

  @Test
  public void test_call_is_no_target() {
    assertThat(errorOf("f() = 1")).isEqualTo("t:1: syntax error near '='");
    assertThat(errorOf("a, f() = 1, 2")).isEqualTo("t:1: syntax error near '='");
  }

  @Test
  public void test_field_of_a_parenthesised_expression_is_a_target() throws Exception {
    assertThat(run("local a = {}; (a).b = 1; return a.b")).containsExactly(1L);
    assertThat(run("local a = {}; local x; x, (a).c = 1, 2; return x, a.c")).containsExactly(1L, 2L);
    assertThat(run("local t = {}; (t)[1] = 3; return t[1]")).containsExactly(3L);
    assertThat(run("local t = {}; local function f() return t end; (f()).x = 4; return t.x"))
        .containsExactly(4L);
  }

}
