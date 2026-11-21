package net.sandius.rembulan.lib;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * Files are read through a buffer of 1 KB, in runs of bytes: lines, chunks and the rest of a file
 * longer than the buffer, and the reads after them, must come out as before.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IoLib_ReadBufferTest extends TestBase {

  private Path file(String content) throws Exception {
    Path dir = Files.createTempDirectory("IoLib_ReadBufferTest");
    Path file = dir.resolve("data.txt");
    Files.write(file, content.getBytes(StandardCharsets.ISO_8859_1));
    file.toFile().deleteOnExit();
    dir.toFile().deleteOnExit();
    return file;
  }

  private static String repeat(String s, int n) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) {
      b.append(s);
    }
    return b.toString();
  }

  private static String lua(Path path) {
    return "'" + path.toString().replace("\\", "/") + "'";
  }

  @Test
  public void test_reads_across_the_buffer() throws Exception {
    String a = repeat("a", 3000);
    String b = repeat("b", 1500);
    Path f = file(a + "\n" + b + "\n" + "42 tail\n" + repeat("c", 2500));
    Object[] actual = run("local f = io.open(" + lua(f) + ")\n"
        + "local l1 = f:read('l') local l2 = f:read('L') local n = f:read('n')\n"
        + "local rest = f:read('l') local c = f:read(2000) local d = f:read('a') local e = f:read(1)\n"
        + "f:close()\n"
        + "local count = 0 for l in io.lines(" + lua(f) + ") do count = count + #l end\n"
        + "return #l1, l1:sub(1, 1), #l2, l2:sub(-1) == '\\n', n, rest, #c, #d, e, count");
    assertThat(actual).isEqualTo(new Object[] {
        3000L, "a", 1501L, true, 42L, " tail", 2000L, 500L, null, 3000L + 1500L + 7L + 2500L});
  }

  @Test
  public void test_line_ending_at_the_end_of_the_buffer() throws Exception {
    // the line feed is the buffer's last byte, then the first of the next fill
    String l1 = repeat("x", 1023);
    String l2 = repeat("y", 1024);
    Path f = file(l1 + "\n" + l2 + "\nz");
    Object[] actual = run("local f = io.open(" + lua(f) + ")\n"
        + "local a, b, c, d = f:read('l', 'l', 'l', 'l') f:close()\n"
        + "return #a, #b, c, d");
    assertThat(actual).isEqualTo(new Object[] {1023L, 1024L, "z", null});
  }

  @Test
  public void test_read_zero_peeks_without_reading() throws Exception {
    Path f = file(repeat("q", 1030));
    Object[] actual = run("local f = io.open(" + lua(f) + ")\n"
        + "local a = f:read(0) local b = f:read(1029) local c = f:read(0) local d = f:read(1)\n"
        + "local e = f:read(0) f:close()\n"
        + "return a, #b, c, d, e");
    assertThat(actual).isEqualTo(new Object[] {"", 1029L, "", "q", null});
  }
}
