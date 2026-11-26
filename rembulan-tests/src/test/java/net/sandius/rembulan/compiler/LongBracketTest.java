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
 * A long string or comment ends only at a closing bracket of its own level; at the end of the
 * input it is unfinished, even right after a closing bracket of another level (#57).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LongBracketTest extends TestBase {

  /** The error compiling {@code source} as chunk "t" gives. */
  private static LoaderException errorOf(String source) {
    try {
      CompilerChunkLoader.of("LongBracketTest")
          .loadTextChunk(new Variable(new DefaultTable()), "t", source);
    }
    catch (LoaderException ex) {
      return ex;
    }
    throw new AssertionError("compiles: " + source);
  }

  private static void assertUnfinished(String source, int line) {
    LoaderException ex = errorOf(source);
    assertThat(ex.getLuaStyleErrorMessage()).as(source).startsWith("t:" + line + ": Lexical error");
    assertThat(ex.isPartialInputError()).as(source).isTrue();
  }

  @Test
  public void test_unfinished_long_string_ending_in_another_closing_bracket() {
    assertUnfinished("x = [==[ abc ]]", 1);
    assertUnfinished("x = [[ abc ]=]", 1);
    assertUnfinished("x = [=[ a ]==]", 1);
    assertUnfinished("x = [==[ a ]=] b ]=]", 1);
    assertUnfinished("x = [==[ a\n\n]=]", 3);
  }

  @Test
  public void test_unfinished_long_comment_ending_in_another_closing_bracket() {
    assertUnfinished("--[==[ c ]=]", 1);
    assertUnfinished("x = 1 --[[ c\n]=]", 2);
  }

  /** The values are PUC-Lua 5.3.6's. */
  @Test
  public void test_closing_brackets_of_another_level_are_content() throws Exception {
    assertThat(run("return [==[ a ]=] b ]] c ]==]")).containsExactly(" a ]=] b ]] c ");
    assertThat(run("return [==[ a ]=]]==]")).containsExactly(" a ]=]");
    assertThat(run("return [[]]")).containsExactly("");
    assertThat(run("return [=[]]]=]")).containsExactly("]]");
    assertThat(run("return [==[]=]]==]")).containsExactly("]=]");
    assertThat(run("return [=[ a ]==]]=]")).containsExactly(" a ]==]");
    assertThat(run("--[==[ c ]=] ]==] return 1")).containsExactly(1L);
    assertThat(run("--[==[ c ]=]]==] return 2")).containsExactly(2L);
  }

}
