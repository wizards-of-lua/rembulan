package net.sandius.rembulan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ByteStringTest {

  private static ByteString bytes(String s) {
    return ByteString.copyOf(s.getBytes(StandardCharsets.ISO_8859_1));
  }

  @Test
  public void test_copyOf_with_an_offset() {
    // the third argument is a length, not an end index
    byte[] array = "0123456789".getBytes(StandardCharsets.ISO_8859_1);
    assertThat(ByteString.copyOf(array, 2, 3)).isEqualTo(bytes("234"));
    assertThat(ByteString.copyOf(array, 7, 3)).isEqualTo(bytes("789"));
    assertThat(ByteString.copyOf(array, 4, 0)).isEqualTo(ByteString.empty());
    assertThatThrownBy(() -> ByteString.copyOf(array, 8, 3))
        .isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void test_repeat() {
    assertThat(bytes("ab").repeat(3, ByteString.empty())).isEqualTo(bytes("ababab"));
    assertThat(bytes("ab").repeat(3, bytes(","))).isEqualTo(bytes("ab,ab,ab"));
    assertThat(bytes("ab").repeat(1, bytes(","))).isEqualTo(bytes("ab"));
    assertThat(bytes("ab").repeat(0, bytes(","))).isEqualTo(ByteString.empty());
    assertThat(bytes("ab").repeat(-5, bytes(","))).isEqualTo(ByteString.empty());
    assertThat(ByteString.empty().repeat(4, bytes("-"))).isEqualTo(bytes("---"));
    assertThat(ByteString.empty().repeat(4, ByteString.empty())).isEqualTo(ByteString.empty());
  }

  @Test
  public void test_repeat_long_and_uneven() {
    // the doubling copies must end exactly with a piece, for every count
    for (int n = 1; n <= 70; n++) {
      StringBuilder expected = new StringBuilder();
      for (int i = 0; i < n; i++) {
        if (i > 0) {
          expected.append("--");
        }
        expected.append("abc");
      }
      assertThat(bytes("abc").repeat(n, bytes("--"))).as("n = " + n)
          .isEqualTo(bytes(expected.toString()));
    }
  }

  @Test
  public void test_repeat_too_large_fails_before_allocating() {
    assertThatThrownBy(() -> bytes("ab").repeat(0x40000000, ByteString.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("resulting string too large");
  }

  @Test
  public void test_repeat_of_a_string_made_from_chars() {
    // a byte string made from a String is encoded first
    assertThat(ByteString.of("ä", StandardCharsets.UTF_8).repeat(2, ByteString.of("|")).length())
        .isEqualTo(5);
  }

  private static String chars(char c, int n) {
    return new String(new char[n]).replace('\0', c);
  }

  @Test(timeout = 5000)
  public void test_byteAt_of_a_string_made_from_chars_is_quick() {
    // it encoded the whole string for every byte: 200,000 bytes took minutes
    ByteString s = ByteString.of(chars('a', 200000));
    long sum = 0;
    for (int i = 0; i < s.length(); i++) {
      sum += s.byteAt(i);
    }
    assertThat(sum).isEqualTo(97L * 200000);
  }

  @Test(timeout = 5000)
  public void test_equals_of_array_and_chars_is_quick() {
    // the array side read the other one with byteAt, which encoded it for every byte
    ByteString chars = ByteString.of(chars('a', 200000));
    ByteString array = bytes(chars('a', 200000));
    assertThat(array.equals(chars)).isTrue();
    assertThat(chars.equals(array)).isTrue();
    assertThat(array.hashCode()).isEqualTo(chars.hashCode());
  }

  @Test
  public void test_mismatch() {
    assertThat(bytes("abc").mismatch(bytes("abc"))).isEqualTo(-1);
    assertThat(bytes("abc").mismatch(bytes("abd"))).isEqualTo(2);
    assertThat(bytes("abc").mismatch(bytes("ab"))).isEqualTo(2);
    assertThat(bytes("ab").mismatch(bytes("abc"))).isEqualTo(2);
    assertThat(bytes("").mismatch(bytes(""))).isEqualTo(-1);
    assertThat(bytes("").mismatch(bytes("a"))).isEqualTo(0);
    assertThat(bytes("xbc").mismatch(ByteString.of("abc"))).isEqualTo(0);
    assertThat(ByteString.of("ä", StandardCharsets.UTF_8)
        .mismatch(ByteString.copyOf(new byte[] {(byte) 0xc3, (byte) 0xa5}))).isEqualTo(1);
  }

  @Test
  public void test_compareTo_and_equals_of_both_kinds() {
    String[] values = {"", "a", "ab", "abc", "abd", "b", "\u00e4", "\u00e4b", "z"};
    for (String x : values) {
      for (String y : values) {
        ByteString[] xs = {bytes(x), ByteString.of(x, StandardCharsets.ISO_8859_1)};
        ByteString[] ys = {bytes(y), ByteString.of(y, StandardCharsets.ISO_8859_1)};
        int expected = Integer.signum(x.compareTo(y));
        for (ByteString a : xs) {
          for (ByteString b : ys) {
            assertThat(Integer.signum(a.compareTo(b))).as(x + " <> " + y).isEqualTo(expected);
            assertThat(a.equals(b)).as(x + " == " + y).isEqualTo(x.equals(y));
          }
        }
      }
    }
    // bytes above 0x7f are unsigned
    assertThat(bytes("\u00e4").compareTo(bytes("a"))).isPositive();
  }
}
