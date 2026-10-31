package net.sandius.rembulan.lib;

import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * The classes of a pattern mean what they mean in Lua 5.3 in the C locale, checked for each of the
 * 256 bytes. So a byte from 128 on belongs to no class but the complements: it may be a part of a
 * character in UTF-8, and {@code %a} can't tell.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringPattern_ClassTest extends TestBase {

  private static boolean isUpper(int c) {
    return c >= 'A' && c <= 'Z';
  }

  private static boolean isLower(int c) {
    return c >= 'a' && c <= 'z';
  }

  private static boolean isDigit(int c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isAlpha(int c) {
    return isUpper(c) || isLower(c);
  }

  private static boolean isAlnum(int c) {
    return isAlpha(c) || isDigit(c);
  }

  private static boolean isGraph(int c) {
    return c > 32 && c < 127;
  }

  private void assertClass(String cls, IntPredicate expected) throws Exception {
    String program = "local members = {}; "
        + "for i = 0, 255 do "
        + "  if string.find(string.char(i), '" + cls + "') then members[#members + 1] = i end "
        + "end; "
        + "return table.concat(members, ',')";
    String wanted = IntStream.range(0, 256).filter(expected).mapToObj(Integer::toString)
        .collect(Collectors.joining(","));
    assertThat(run(program)).isEqualTo(new Object[] {wanted});
  }

  private void assertClassAndComplement(char letter, IntPredicate expected) throws Exception {
    assertClass("%" + letter, expected);
    assertClass("%" + Character.toUpperCase(letter), expected.negate());
    assertClass("[%" + letter + "]", expected);
    assertClass("[^%" + letter + "]", expected.negate());
  }

  @Test
  public void test_letters() throws Exception {
    assertClassAndComplement('a', StringPattern_ClassTest::isAlpha);
  }

  @Test
  public void test_control_characters() throws Exception {
    assertClassAndComplement('c', c -> c < 32 || c == 127);
  }

  @Test
  public void test_digits() throws Exception {
    assertClassAndComplement('d', StringPattern_ClassTest::isDigit);
  }

  @Test
  public void test_printable_except_space() throws Exception {
    assertClassAndComplement('g', StringPattern_ClassTest::isGraph);
  }

  @Test
  public void test_lowercase_letters() throws Exception {
    assertClassAndComplement('l', StringPattern_ClassTest::isLower);
  }

  @Test
  public void test_punctuation() throws Exception {
    assertClassAndComplement('p', c -> isGraph(c) && !isAlnum(c));
  }

  @Test
  public void test_space() throws Exception {
    assertClassAndComplement('s', c -> c == ' ' || (c >= 9 && c <= 13));
  }

  @Test
  public void test_uppercase_letters() throws Exception {
    assertClassAndComplement('u', StringPattern_ClassTest::isUpper);
  }

  @Test
  public void test_alphanumeric() throws Exception {
    assertClassAndComplement('w', StringPattern_ClassTest::isAlnum);
  }

  @Test
  public void test_hexadecimal_digits() throws Exception {
    assertClassAndComplement('x',
        c -> isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
  }

  @Test
  public void test_word_with_an_umlaut_is_not_all_letters() throws Exception {
    assertThat(run("return string.match('K\\xC3\\xA4se', '%a+')")).isEqualTo(new Object[] {"K"});
  }
}
