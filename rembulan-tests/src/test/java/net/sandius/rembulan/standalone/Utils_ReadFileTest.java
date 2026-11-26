package net.sandius.rembulan.standalone;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

/**
 * The standalone console reads script files and standard input as UTF-8, as {@code load} treats
 * its strings, so that non-ASCII characters in string literals keep their bytes.
 */
public class Utils_ReadFileTest {

  @Test
  public void test_a_utf8_file_is_read_as_utf8() throws Exception {
    Path file = Files.createTempFile("rembulan-utf8", ".lua");
    try {
      Files.write(file, "return \"汉字\"".getBytes(StandardCharsets.UTF_8));
      assertThat(Utils.readFile(file.toString())).isEqualTo("return \"汉字\"");
    } finally {
      Files.delete(file);
    }
  }

  @Test
  public void test_bytes_are_decoded_as_utf8() throws Exception {
    assertThat(Utils.bytesToString("ä€".getBytes(StandardCharsets.UTF_8))).isEqualTo("ä€");
  }

}
