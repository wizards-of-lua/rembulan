package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.exec.CallException;
import net.sandius.rembulan.testenv.TestBase;

/**
 * Endless recursion and loops of {@code __index} or {@code __newindex} tables raise Lua errors, as
 * in PUC-Lua ("stack overflow", "'__index' chain too long; possible loop"), which {@code pcall}
 * catches, instead of a {@link StackOverflowError} that escapes the Lua code and its host.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StackOverflowTest extends TestBase {

  @Test(timeout = 20000)
  public void test_pcall_catches_endless_recursion() throws Exception {
    String program = String.join("\n",
        "local function f(n) return 1 + f(n + 1) end",
        "local ok, msg = pcall(f, 1)",
        "return ok, msg");

    Object[] actual = run(program);

    assertThat(actual[0]).isEqualTo(false);
    assertThat(String.valueOf(actual[1])).contains("stack overflow");
  }

  @Test(timeout = 20000)
  public void test_xpcall_calls_the_handler_on_endless_recursion() throws Exception {
    String program = String.join("\n",
        "local function f(n) return 1 + f(n + 1) end",
        "local ok, msg = xpcall(f, function(m) return 'handled: ' .. tostring(m) end, 1)",
        "return ok, msg");

    Object[] actual = run(program);

    assertThat(actual[0]).isEqualTo(false);
    assertThat(String.valueOf(actual[1])).startsWith("handled: ");
    assertThat(String.valueOf(actual[1])).contains("stack overflow");
  }

  @Test(timeout = 20000)
  public void test_coroutine_resume_returns_the_error_of_endless_recursion() throws Exception {
    String program = String.join("\n",
        "local function f(n) return 1 + f(n + 1) end",
        "local co = coroutine.create(f)",
        "local ok, msg = coroutine.resume(co, 1)",
        "return ok, msg, coroutine.status(co)");

    Object[] actual = run(program);

    assertThat(actual[0]).isEqualTo(false);
    assertThat(String.valueOf(actual[1])).contains("stack overflow");
    assertThat(String.valueOf(actual[2])).isEqualTo("dead");
  }

  @Test(timeout = 20000)
  public void test_unprotected_endless_recursion_is_a_lua_error() throws Exception {
    String program = "local function f(n) return 1 + f(n + 1) end return f(1)";

    CallException error = null;
    try {
      run(program);
    } catch (CallException ex) {
      error = ex;
    }

    assertThat(error).isNotNull();
    assertThat(error.getCause().getMessage()).contains("stack overflow");
  }

  @Test(timeout = 20000)
  public void test_index_loop_is_an_error() throws Exception {
    String program = String.join("\n",
        "local a = {}",
        "setmetatable(a, a)",
        "a.__index = a",
        "local ok, msg = pcall(function() return a.x end)",
        "return ok, msg");

    Object[] actual = run(program);

    assertThat(actual[0]).isEqualTo(false);
    assertThat(String.valueOf(actual[1])).contains("'__index' chain too long; possible loop");
  }

  @Test(timeout = 20000)
  public void test_newindex_loop_is_an_error() throws Exception {
    String program = String.join("\n",
        "local a = {}",
        "setmetatable(a, a)",
        "a.__newindex = a",
        "local ok, msg = pcall(function() a.x = 1 end)",
        "return ok, msg");

    Object[] actual = run(program);

    assertThat(actual[0]).isEqualTo(false);
    assertThat(String.valueOf(actual[1])).contains("'__newindex' chain too long; possible loop");
  }

  @Test(timeout = 20000)
  public void test_long_index_and_newindex_chains_still_work() throws Exception {
    // a chain of 1000 tables, each the __index and __newindex of the one before
    String program = String.join("\n",
        "local first = {}",
        "local t = first",
        "for i = 1, 1000 do",
        "  local nxt = {}",
        "  setmetatable(t, {__index = nxt, __newindex = nxt})",
        "  t = nxt",
        "end",
        "t.x = 42",
        "first.y = 7",
        "return first.x, rawget(first, 'y'), t.y");

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {42L, null, 7L});
  }

}
