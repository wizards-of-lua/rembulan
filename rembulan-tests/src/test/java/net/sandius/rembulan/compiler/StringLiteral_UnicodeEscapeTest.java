package net.sandius.rembulan.compiler;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.Variable;
import net.sandius.rembulan.impl.DefaultTable;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.testenv.TestBase;

/**
 * {@code \\u{XXX}} escapes take values up to 0x10FFFF, as in PUC-Lua 5.3 (5.4 allows more), and
 * encode them as PUC-Lua does ({@code luaO_utf8esc}): surrogates as they are.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringLiteral_UnicodeEscapeTest extends TestBase {

  /** The bytes of the string literal {@code literal} (Lua source), as a list of numbers. */
  private Object[] bytesOf(String literal) throws Exception {
    return run("return string.byte(" + literal + ", 1, -1)");
  }

  private static Object[] bytes(int... bs) {
    Object[] result = new Object[bs.length];
    for (int i = 0; i < bs.length; i++) {
      result[i] = (long) bs[i];
    }
    return result;
  }

  @Test
  public void test_boundaries_of_the_byte_lengths() throws Exception {
    assertThat(bytesOf("'\\u{7F}'")).isEqualTo(bytes(0x7F));
    assertThat(bytesOf("'\\u{80}'")).isEqualTo(bytes(0xC2, 0x80));
    assertThat(bytesOf("'\\u{7FF}'")).isEqualTo(bytes(0xDF, 0xBF));
    assertThat(bytesOf("'\\u{FFFF}'")).isEqualTo(bytes(0xEF, 0xBF, 0xBF));
    assertThat(bytesOf("'\\u{10000}'")).isEqualTo(bytes(0xF0, 0x90, 0x80, 0x80));
  }

  @Test
  public void test_the_highest_unicode_code_point() throws Exception {
    assertThat(bytesOf("'\\u{10FFFF}'")).isEqualTo(bytes(0xF4, 0x8F, 0xBF, 0xBF));
  }

  @Test
  public void test_leading_zeros() throws Exception {
    assertThat(bytesOf("'\\u{00000000000041}'")).isEqualTo(bytes(0x41));
  }

  @Test
  public void test_a_surrogate_is_encoded_as_it_is() throws Exception {
    assertThat(bytesOf("'\\u{D800}'")).isEqualTo(bytes(0xED, 0xA0, 0x80));
  }

  @Test
  public void test_more_than_0x10FFFF_is_an_error() throws Exception {
    LoaderException error = null;
    try {
      CompilerChunkLoader.of("UnicodeEscapeTest")
          .loadTextChunk(new Variable(new DefaultTable()), "test", "return '\\u{110000}'");
    } catch (LoaderException ex) {
      error = ex;
    }
    assertThat(error).isNotNull();
    assertThat(error.getMessage()).contains("UTF-8 value too large");
  }

}
