package net.sandius.rembulan.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.Variable;
import net.sandius.rembulan.impl.DefaultTable;
import net.sandius.rembulan.load.LoaderException;

/**
 * Compile errors that are found after parsing, and lexical errors, have the line of the error
 * (#53), and a few of them read as in PUC-Lua 5.3 (the expected messages are PUC-Lua 5.3.6's
 * unless noted).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CompileErrorLineTest {

  /** The error that loading {@code source} as chunk "t" gives. */
  private static LoaderException loadError(String source) {
    try {
      CompilerChunkLoader.of("CompileErrorLineTest")
          .loadTextChunk(new Variable(new DefaultTable()), "t", source);
    }
    catch (LoaderException ex) {
      return ex;
    }
    throw new AssertionError("compiles: " + source);
  }

  private static String messageOf(String source) {
    return loadError(source).getLuaStyleErrorMessage();
  }

  @Test
  public void test_expression_statement_is_a_syntax_error_near_the_next_token() {
    assertThat(messageOf("x")).isEqualTo("t:1: syntax error near <eof>");
    assertThat(messageOf("x y")).isEqualTo("t:1: syntax error near 'y'");
    assertThat(messageOf("x = 'a\\z\n\n  b' y")).isEqualTo("t:3: syntax error near <eof>");
    assertThat(messageOf("f()\n\nx\n\ny = 1")).isEqualTo("t:5: syntax error near 'y'");
    // PUC-Lua: "t:3: ..." (its end of input is after the last newline, the parser's on it)
    assertThat(messageOf("x\n\n")).isEqualTo("t:2: syntax error near <eof>");
  }

  @Test
  public void test_escape_errors_have_the_line_and_the_literal() {
    assertThat(messageOf("x = '\\300'")).isEqualTo("t:1: decimal escape too large near ''\\300''");
    assertThat(messageOf("x = 1\ny = '\\300'")).isEqualTo("t:2: decimal escape too large near ''\\300''");
    // PUC-Lua shows the literal only up to the digit that is too much
    assertThat(messageOf("x = '\\u{110000}'")).isEqualTo("t:1: UTF-8 value too large near ''\\u{110000}''");
  }

  @Test
  public void test_goto_and_label_errors_have_the_line() {
    // PUC-Lua: "t:3: ..." (the line where the function ends)
    assertThat(messageOf("goto l1\n\nx = 1")).isEqualTo("t:1: no visible label 'l1' for <goto> at line 1");
    assertThat(messageOf("::a::\n\n::a::")).isEqualTo("t:3: label 'a' already defined on line 1");
    assertThat(messageOf("do goto l1\nlocal x = 1\n::l1:: print(x) end"))
        .isEqualTo("t:3: <goto l1> at line 1 jumps into the scope of local 'x'");
  }

  @Test
  public void test_break_outside_a_loop() {
    assertThat(messageOf("x = 1\nbreak")).isEqualTo("t:2: <break> at line 2 not inside a loop");
  }

  @Test
  public void test_varargs_outside_a_vararg_function() {
    assertThat(messageOf("function f()\n  return ...\nend"))
        .isEqualTo("t:2: cannot use '...' outside a vararg function near '...'");
  }

  /** The messages are still the lexer's own; the line is the line of the error. */
  @Test
  public void test_lexical_errors_have_the_line() {
    assertThat(loadError("x = 1\ny = 'abc").getSourceLine()).isEqualTo(2);
    assertThat(loadError("x = '\\q'\n").getSourceLine()).isEqualTo(1);
    assertThat(loadError("x = 1\n\n--[[ abc").getSourceLine()).isEqualTo(3);
  }

  /** For a console that reads more lines: only an error at the end of the input is partial. */
  @Test
  public void test_partial_input() {
    assertThat(loadError("x = 1\ny = 'abc").isPartialInputError()).isTrue();
    assertThat(loadError("x = 1\n\n--[[ abc").isPartialInputError()).isTrue();
    assertThat(loadError("x = '\\q'\n").isPartialInputError()).isFalse();
    assertThat(loadError("x\n\n").isPartialInputError()).isTrue();
    assertThat(loadError("x y").isPartialInputError()).isFalse();
    assertThat(loadError("goto l1").isPartialInputError()).isFalse();
    assertThat(loadError("x = '\\300'").isPartialInputError()).isFalse();
  }

}
