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
 * {@code load} names a chunk as Lua 5.3 does ({@code luaO_chunkid}): {@code =name} as {@code name},
 * {@code @file} as {@code file}, any other string as {@code [string "first line..."]}, shortened.
 * The source itself is the default name of a string chunk, so without shortening a source of more
 * than 64 KB could not be compiled at all.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicLib_LoadChunkNameTest extends TestBase {

  /** Runs the program with a standard library that has a chunk loader, so that load exists. */
  private Object[] runWithLoad(String program) throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    ChunkLoader loader = CompilerChunkLoader.of("LoadChunkNameTest");
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).withLoader(loader)
        .installInto(state);
    LuaFunction main = loader.loadTextChunk(new Variable(env), "test", program);
    return DirectCallExecutor.newExecutor().call(state, main);
  }

  /** The chunk name in the message of the syntax error that loading the code gives. */
  private Object chunkNameOf(String loadArgs) throws Exception {
    Object[] actual = runWithLoad("local f, msg = load(" + loadArgs + ")\n"
        + "return (msg:match('^(.-):[%d?]+:'))");
    return actual[0];
  }

  @Test
  public void test_default_name_is_the_source() throws Exception {
    assertThat(chunkNameOf("'x = '")).isEqualTo("[string \"x = \"]");
  }

  @Test
  public void test_given_name_is_wrapped() throws Exception {
    assertThat(chunkNameOf("'x = ', 'name'")).isEqualTo("[string \"name\"]");
  }

  @Test
  public void test_equals_sign_name_is_literal() throws Exception {
    assertThat(chunkNameOf("'x = ', '=my chunk'")).isEqualTo("my chunk");
  }

  @Test
  public void test_at_sign_name_is_a_file_name() throws Exception {
    assertThat(chunkNameOf("'x = ', '@my/file.lua'")).isEqualTo("my/file.lua");
  }

  @Test
  public void test_multi_line_source_shows_its_first_line() throws Exception {
    assertThat(chunkNameOf("'x = 1\\nx = '")).isEqualTo("[string \"x = 1...\"]");
  }

  @Test
  public void test_long_source_is_shortened() throws Exception {
    String expected = "[string \"" + repeat('y', 45) + "...\"]";
    assertThat(chunkNameOf("string.rep('y', 100) .. ' = '")).isEqualTo(expected);
  }

  @Test
  public void test_long_names_are_shortened() throws Exception {
    assertThat(chunkNameOf("'x = ', '=' .. string.rep('n', 100)")).isEqualTo(repeat('n', 59));
    assertThat(chunkNameOf("'x = ', '@' .. string.rep('d/', 50) .. 'f.lua'"))
        .isEqualTo("..." + tail(repeat2("d/", 50) + "f.lua", 56));
  }

  @Test
  public void test_function_chunk_is_named_load() throws Exception {
    Object[] actual = runWithLoad("local parts = { 'x = ' }\n"
        + "local f, msg = load(function() return table.remove(parts) end)\n"
        + "return (msg:match('^(.-):[%d?]+:'))");
    assertThat(actual).isEqualTo(new Object[] {"(load)"});
  }

  @Test
  public void test_source_larger_than_64_kilobytes_loads() throws Exception {
    Object[] actual = runWithLoad("local f, msg = load('-- ' .. string.rep('x', 70000) .. '\\nreturn 42')\n"
        + "return f and f() or msg");
    assertThat(actual).isEqualTo(new Object[] {42L});
  }

  private static String repeat(char c, int n) {
    StringBuilder bld = new StringBuilder();
    for (int i = 0; i < n; i++) {
      bld.append(c);
    }
    return bld.toString();
  }

  private static String repeat2(String s, int n) {
    StringBuilder bld = new StringBuilder();
    for (int i = 0; i < n; i++) {
      bld.append(s);
    }
    return bld.toString();
  }

  private static String tail(String s, int n) {
    return s.substring(s.length() - n);
  }
}
