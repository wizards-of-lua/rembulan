package net.sandius.rembulan.compiler;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * In a multiple assignment, all values are evaluated before any assignment is made. Storing the
 * first value must not clobber the remaining values of a call at the tail of the expression list,
 * even if the store itself calls a function (a __newindex metamethod or a call in the target).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultipleAssignmentTest extends TestBase {

  private static final String RECORDING_TABLE = "local log = {}\n"
      + "local t = setmetatable({}, {__newindex = function(t, k, v) log[#log + 1] = k .. '=' .. tostring(v) end})\n";

  @Test
  public void test_newindex_function_two_values() throws Exception {
    String program = RECORDING_TABLE
        + "local function f() return 10, 20 end\n"
        + "t.a, t.b = f()\n"
        + "return table.concat(log, ' ')";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {"a=10 b=20"});
  }

  @Test
  public void test_newindex_function_three_values() throws Exception {
    String program = RECORDING_TABLE
        + "local function f() return 10, 20, 30 end\n"
        + "t.a, t.b, t.c = f()\n"
        + "return table.concat(log, ' ')";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {"a=10 b=20 c=30"});
  }

  @Test
  public void test_newindex_function_single_value_before_call() throws Exception {
    String program = RECORDING_TABLE
        + "local function f() return 20, 30 end\n"
        + "t.a, t.b, t.c = 10, f()\n"
        + "return table.concat(log, ' ')";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {"a=10 b=20 c=30"});
  }

  @Test
  public void test_newindex_function_fewer_values_than_targets() throws Exception {
    String program = RECORDING_TABLE
        + "local function f() return 10 end\n"
        + "t.a, t.b, t.c = f()\n"
        + "return table.concat(log, ' ')";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {"a=10 b=nil c=nil"});
  }

  @Test
  public void test_newindex_library_function() throws Exception {
    // rawset returns the table, which must not end up in t.b
    String program = "local t = setmetatable({}, {__newindex = rawset})\n"
        + "local function f() return 10, 20 end\n"
        + "t.a, t.b = f()\n"
        + "return rawget(t, 'a'), rawget(t, 'b')";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {10L, 20L});
  }

  @Test
  public void test_call_in_target() throws Exception {
    String program = "local t = {}\n"
        + "local function g() return t end\n"
        + "local function f() return 10, 20 end\n"
        + "g().a, g().b = f()\n"
        + "return t.a, t.b";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {10L, 20L});
  }

  @Test
  public void test_newindex_function_varargs() throws Exception {
    String program = RECORDING_TABLE
        + "local function f(...) t.a, t.b = ... end\n"
        + "f(10, 20)\n"
        + "return table.concat(log, ' ')";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {"a=10 b=20"});
  }

  @Test
  public void test_plain_table_and_locals() throws Exception {
    String program = "local t = {}\n"
        + "local x, y\n"
        + "local function f() return 10, 20, 30, 40 end\n"
        + "t.a, x, t.b, y = f()\n"
        + "return t.a, x, t.b, y";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {10L, 20L, 30L, 40L});
  }
}
