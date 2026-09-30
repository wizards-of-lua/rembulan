package net.sandius.rembulan.lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.testenv.TestBase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IoLib_StdinTest extends TestBase {

  private StateContext state;
  private Table env;

  private void install(InputStream stdin) {
    RuntimeEnvironment runtimeEnvironment =
        RuntimeEnvironments.system(stdin, System.out, System.err);
    state = StateContexts.newDefaultInstance();
    env = StandardLibrary.in(runtimeEnvironment).installInto(state);
    IoLib.installInto(state, env, runtimeEnvironment);
  }

  private void install(String stdin) {
    install(new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
  }

  private Object[] exec(String program) throws Exception {
    LuaFunction main = CompilerChunkLoader.of("StdinTest")
        .loadTextChunk(new Variable(env), "StdinTest", program);
    return DirectCallExecutor.newExecutor().call(state, main);
  }

  @Test
  public void test_io_read__all_formats() throws Exception {
    // Given:
    install("12 34\nsecond line\nxyz\nrest");

    // When:
    Object[] actual = exec("" //
        + "return io.read('n'), io.read(), io.read('L'), io.read(0), io.read(2), io.read('l'),\n"
        + "  io.read('a'), io.read('a'), io.read('l')\n");

    // Then:
    assertThat(actual)
        .isEqualTo(new Object[] {12L, " 34", "second line\n", "", "xy", "z", "rest", "", null});
  }

  @Test
  public void test_io_read__lines_longer_than_the_read_buffer() throws Exception {
    // Given:
    String first = "a line that is much longer than sixteen bytes";
    String second = "and another one, just as long as the first one";
    install(first + "\n" + second + "\n");

    // When:
    Object[] actual = exec("return io.stdin:read('l', 'L', 'l')");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {first, second + "\n", null});
  }

  @Test
  public void test_io_lines__without_file_name_reads_standard_input() throws Exception {
    // Given:
    install("one\ntwo\nthree");

    // When:
    Object[] actual = exec("" //
        + "local result = ''\n" //
        + "for line in io.lines() do result = result .. '[' .. line .. ']' end\n" //
        + "return result, io.type(io.stdin)\n");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"[one][two][three]", "file"});
  }

  @Test
  public void test_io_read__takes_no_more_from_a_terminal_than_it_needs() throws Exception {
    // Given: a terminal delivers one line at a time and would block when asked for more
    TerminalInputStream terminal = new TerminalInputStream("one\ntwo\n");
    install(terminal);

    // When:
    Object[] first = exec("return io.read()");
    int consumedAfterFirst = terminal.consumed;
    Object[] second = exec("return io.read()");

    // Then:
    assertThat(first[0]).isEqualTo("one");
    assertThat(consumedAfterFirst).isEqualTo(4);
    assertThat(second[0]).isEqualTo("two");
  }

  @Test
  public void test_File_seek__on_standard_input() throws Exception {
    // Given:
    install("hello world");

    // When:
    Object[] actual = exec("" //
        + "io.read(5)\n" //
        + "local cur = io.stdin:seek('cur')\n" //
        + "local fromEnd, msg = io.stdin:seek('end')\n" //
        + "return cur, fromEnd, type(msg), io.read('a')\n");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {5L, null, "string", " world"});
  }

  /** Delivers its content up to the end of the next line per read, and never has more ready. */
  private static class TerminalInputStream extends InputStream {

    private final byte[] content;
    int consumed;

    TerminalInputStream(String content) {
      this.content = content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int read() throws IOException {
      byte[] b = new byte[1];
      return read(b, 0, 1) == -1 ? -1 : b[0];
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (consumed == content.length) {
        return -1;
      }
      int n = 0;
      while (n < len && consumed < content.length) {
        byte next = content[consumed++];
        b[off + n++] = next;
        if (next == '\n') {
          break;
        }
      }
      return n;
    }

    @Override
    public int available() {
      return 0;
    }

  }

}
