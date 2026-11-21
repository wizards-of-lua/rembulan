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
}
