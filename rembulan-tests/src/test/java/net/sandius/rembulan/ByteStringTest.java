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
}
