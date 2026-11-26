package net.sandius.rembulan.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.CallException;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.runtime.LuaFunction;

/**
 * Errors have the position of the error in front of the message, as in PUC-Lua 5.3 for level 1
 * (#60): errors of the runtime and of library functions, and {@code error} with a string at level
 * 1. The expected messages are PUC-Lua 5.3.6's, without the variable names (#54).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ErrorPositionTest {

  /** Runs {@code program} as chunk "t" and returns the results of calling it in pcall. */
  private static Object[] pcall(String program) throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    ChunkLoader loader = CompilerChunkLoader.of("ErrorPositionTest");
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).withLoader(loader)
        .installInto(state);
    env.rawset("program", loader.loadTextChunk(new Variable(env), "t", program));
    LuaFunction main = loader.loadTextChunk(new Variable(env), "main", "return pcall(program)");
    return DirectCallExecutor.newExecutor().call(state, main);
  }

  /** The error message of running {@code program} as chunk "t" in pcall. */
  private static Object errorOf(String program) throws Exception {
    Object[] result = pcall(program);
    assertThat(result[0]).as(program).isEqualTo(false);
    return result[1];
  }

  @Test
  public void test_runtime_errors() throws Exception {
    assertThat(errorOf("local t = {}\nreturn t.a.b")).isEqualTo("t:2: attempt to index a nil value");
    assertThat(errorOf("local s = 'a' .. {}")).isEqualTo("t:1: attempt to concatenate a table value");
  }

  @Test
  public void test_library_errors() throws Exception {
    assertThat(errorOf("table.insert(nil, 1)"))
        .isEqualTo("t:1: bad argument #1 to 'insert' (table expected, got nil)");
  }

  /** A message made of another error's message has one position. */
  @Test
  public void test_message_of_a_cause_has_one_position() throws Exception {
    assertThat(errorOf("select(1.5, true)"))
        .isEqualTo("t:1: bad argument #1 to 'select' (number has no integer representation)");
    assertThat(errorOf("local f = io.tmpfile()\nf:close()\nf:read()"))
        .isEqualTo("t:3: attempt to use a closed file");
  }

  @Test
  public void test_error_with_a_string() throws Exception {
    assertThat(errorOf("local x = 1\nerror('boom')")).isEqualTo("t:2: boom");
    assertThat(errorOf("error('boom', 1)")).isEqualTo("t:1: boom");
  }

  @Test
  public void test_error_without_position() throws Exception {
    assertThat(errorOf("error('boom', 0)")).isEqualTo("boom");
    assertThat(errorOf("error(42)")).isEqualTo(42L);
    assertThat(errorOf("error({})")).isInstanceOf(Table.class);
  }

  /** PUC-Lua: "t:2: v expected"; the position of a caller is not known after a resume. */
  @Test
  public void test_error_at_level_2_has_no_position() throws Exception {
    assertThat(errorOf("local function check(v) if not v then error('v expected', 2) end end\n"
        + "check(nil)")).isEqualTo("v expected");
  }

  /** Called by a Java function, as by pcall: no position, as in PUC-Lua. */
  @Test
  public void test_called_from_java_has_no_position() throws Exception {
    assertThat(pcall("return pcall(error, 'x')")).containsExactly(true, false, "x");
    Object[] result = pcall("return pcall(table.insert, nil, 1)");
    assertThat(result[2]).isEqualTo("bad argument #1 to 'insert' (table expected, got nil)");
  }

  /**
   * PUC-Lua: "t:1: x" (it calls a C function in a tail call like any other); here the calling
   * function has returned when the tail call runs, so its position is not known.
   */
  @Test
  public void test_java_function_in_a_tail_call_has_no_position() throws Exception {
    assertThat(errorOf("local function f() return error('x') end\n"
        + "local function g()\n  f()\nend\ng()")).isEqualTo("x");
  }

  @Test
  public void test_error_in_a_metamethod() throws Exception {
    assertThat(errorOf("local t = setmetatable({}, {__index = function(t, k)\n"
        + "  error('no ' .. k)\nend})\nreturn t.x")).isEqualTo("t:2: no x");
  }

  @Test
  public void test_assert() throws Exception {
    assertThat(errorOf("assert(false, 'm')")).isEqualTo("t:1: m");
    assertThat(errorOf("\nassert(false)")).isEqualTo("t:2: assertion failed!");
    assertThat(errorOf("assert(false, {})")).isInstanceOf(Table.class);
  }

  /** A function resumed after a pause (here a yield) is on the stack again: it has positions. */
  @Test
  public void test_errors_after_a_resume() throws Exception {
    assertThat(pcall("local co = coroutine.create(function()\n  coroutine.yield()\n"
        + "  local x = nil\n  return x.y\nend)\ncoroutine.resume(co)\nreturn coroutine.resume(co)"))
        .containsExactly(true, false, "t:4: attempt to index a nil value");
    assertThat(pcall("local co = coroutine.create(function()\n  coroutine.yield()\n"
        + "  error('late')\nend)\ncoroutine.resume(co)\nreturn coroutine.resume(co)"))
        .containsExactly(true, false, "t:3: late");
  }

  /** An error nobody catches has the position in its message too. */
  @Test
  public void test_uncaught_error() throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    ChunkLoader loader = CompilerChunkLoader.of("ErrorPositionTest");
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).withLoader(loader)
        .installInto(state);
    LuaFunction main = loader.loadTextChunk(new Variable(env), "t", "local x\nerror('boom')");
    try {
      DirectCallExecutor.newExecutor().call(state, main);
    }
    catch (CallException ex) {
      assertThat(ex.getCause().getMessage()).isEqualTo("t:2: boom");
      return;
    }
    throw new AssertionError("no error");
  }

}
