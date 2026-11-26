package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.testenv.TestBase;

/**
 * {@code load} with a reader function: "A return of an empty string, nil, or no value signals
 * the end of the chunk" (Lua manual).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicLib_LoadReaderTest extends TestBase {

  private Object[] runWithLoad(String program) throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    ChunkLoader loader = CompilerChunkLoader.of("LoadReaderTest");
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).withLoader(loader)
        .installInto(state);
    LuaFunction main = loader.loadTextChunk(new Variable(env), "test", program);
    return DirectCallExecutor.newExecutor().call(state, main);
  }

  /** A program that loads "return 42" from {@code reader}, with {@code s} and {@code i} set. */
  private Object[] loadWith(String reader) throws Exception {
    return runWithLoad("local s, i = 'return 42', 0\n"
        + "local f = assert(load(" + reader + "))\n"
        + "return f()");
  }

  @Test(timeout = 10000)
  public void test_an_empty_string_ends_the_chunk() throws Exception {
    assertThat(loadWith("function() i = i + 1; return s:sub(i, i) end"))
        .isEqualTo(new Object[] {42L});
  }

  @Test(timeout = 10000)
  public void test_nil_ends_the_chunk() throws Exception {
    assertThat(loadWith("function() i = i + 1; if i <= #s then return s:sub(i, i) end return nil end"))
        .isEqualTo(new Object[] {42L});
  }

  @Test(timeout = 10000)
  public void test_no_value_ends_the_chunk() throws Exception {
    assertThat(loadWith("function() i = i + 1; if i <= #s then return s:sub(i, i) end end"))
        .isEqualTo(new Object[] {42L});
  }

}
