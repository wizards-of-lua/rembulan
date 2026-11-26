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
 * The condition of a {@code repeat} loop sees the locals of its body, so a label at the end of
 * the body is still in their scope, and a goto to it from before a local jumps into that scope
 * (#56). The expected messages and values are PUC-Lua 5.3.6's (except the line in front of the
 * message: PUC-Lua gives the line of {@code until}).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepeatGotoScopeTest extends TestBase {

  /** The message of the error compiling {@code source} as chunk "t" gives. */
  private static String errorOf(String source) {
    try {
      CompilerChunkLoader.of("RepeatGotoScopeTest")
          .loadTextChunk(new Variable(new DefaultTable()), "t", source);
    }
    catch (LoaderException ex) {
      return ex.getLuaStyleErrorMessage();
    }
    throw new AssertionError("compiles: " + source);
  }

  @Test
  public void test_goto_to_the_end_of_a_repeat_body_past_a_local() {
    String msg = "t:1: <goto cont> at line 1 jumps into the scope of local 'x'";
    assertThat(errorOf("repeat goto cont; local x; ::cont:: until x")).isEqualTo(msg);
    assertThat(errorOf("repeat goto cont; local x; ::cont:: until true")).isEqualTo(msg);
    assertThat(errorOf("local i = 0 repeat i = i + 1 goto cont; local x = 5; ::cont:: until x"))
        .isEqualTo(msg);
    assertThat(errorOf("repeat goto cont; local a, b; ::cont:: ; ::other:: until true"))
        .isEqualTo("t:1: <goto cont> at line 1 jumps into the scope of local 'a'");
    assertThat(errorOf("repeat\n  goto cont\n  local x = 5\n  ::cont::\nuntil x"))
        .isEqualTo("t:4: <goto cont> at line 2 jumps into the scope of local 'x'");
  }

  @Test
  public void test_gotos_that_stay_valid() throws Exception {
    assertThat(run("local i = 0 repeat i = i + 1; goto cont; ::cont:: local x = i until x >= 3 return i"))
        .containsExactly(3L);
    assertThat(run("local n = 0 repeat local x = n; n = n + 1; if n < 3 then goto cont end; ::cont:: until n >= 3 return n"))
        .containsExactly(3L);
    assertThat(run("local n = 0 while n < 3 do n = n + 1; goto continue; local y; ::continue:: end return n"))
        .containsExactly(3L);
    assertThat(run("local n = 0 repeat n = n + 1; do goto c; local z; ::c:: end until n >= 2 return n"))
        .containsExactly(2L);
  }

}
