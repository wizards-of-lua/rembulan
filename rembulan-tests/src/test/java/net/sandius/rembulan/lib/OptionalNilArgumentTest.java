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
 * An explicit {@code nil} in an optional argument stands for the default and is consumed, so the
 * arguments after it are read from their own positions.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OptionalNilArgumentTest extends TestBase {

  /** Like {@link #run}, but with {@code load} (TestBase's library has no chunk loader). */
  private Object[] runWithLoad(String program) throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    ChunkLoader loader = CompilerChunkLoader.of("OptionalNilArgumentTest");
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).withLoader(loader)
        .installInto(state);
    LuaFunction main = loader.loadTextChunk(new Variable(env), "test", program);
    return DirectCallExecutor.newExecutor().call(state, main);
  }

  @Test
  public void test_string_find_with_nil_init_and_plain() throws Exception {
    Object[] actual = run("return string.find('a.b', '.', nil, true)");

    assertThat(actual).isEqualTo(new Object[] {2L, 2L});
  }

  @Test
  public void test_load_with_nil_name_and_mode_and_an_env() throws Exception {
    String program = String.join("\n",
        "local env = {}",
        "local f = load('a = 3', nil, nil, env)",
        "f()",
        "return env.a, rawget(_G, 'a')");

    Object[] actual = runWithLoad(program);

    assertThat(actual).isEqualTo(new Object[] {3L, null});
  }

  @Test
  public void test_string_byte_with_nil_start() throws Exception {
    Object[] actual = run("return string.byte('abc', nil, 3)");

    assertThat(actual).isEqualTo(new Object[] {97L, 98L, 99L});
  }

}
