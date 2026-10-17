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
 * {@code string.format('%q', s)} must give a literal that Lua reads back as {@code s}. A backslash
 * came out as {@code \x0c}, a form feed, and every other byte written as a hex escape got the
 * wrong high digit: the escape took bits 8 to 11 of the byte instead of bits 4 to 7. Bytes from
 * 0x80 up were decoded as text first, so that 0x80 became the decimal escape {@code \8364}, which
 * Lua refuses as too large.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringLib_FormatQTest extends TestBase {

  /** Runs the program with {@code load}, which needs a chunk loader. */
  private Object[] runWithLoad(String program) throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    ChunkLoader loader = CompilerChunkLoader.of("FormatQTest");
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).withLoader(loader)
        .installInto(state);
    LuaFunction main = loader.loadTextChunk(new Variable(env), "test", program);
    return DirectCallExecutor.newExecutor().call(state, main);
  }

  @Test
  public void test_backslash_is_escaped_with_a_backslash() throws Exception {
    Object[] actual = run("return string.format('%q', 'a\\\\b')");
    assertThat(actual).isEqualTo(new Object[] {"\"a\\\\b\""});
  }

  @Test
  public void test_every_byte_reads_back() throws Exception {
    Object[] actual = runWithLoad(""
        + "local bytes = {}\n"
        + "for i = 0, 255 do bytes[#bytes + 1] = string.char(i) end\n"
        + "local s = table.concat(bytes)\n"
        + "local chunk, err = load('return ' .. string.format('%q', s))\n"
        + "if not chunk then return err end\n"
        + "local back = chunk()\n"
        + "for i = 1, #s do\n"
        + "  if back:byte(i) ~= s:byte(i) then return i - 1 end\n"
        + "end\n"
        + "return #back");
    assertThat(actual).isEqualTo(new Object[] {256L});
  }
}
