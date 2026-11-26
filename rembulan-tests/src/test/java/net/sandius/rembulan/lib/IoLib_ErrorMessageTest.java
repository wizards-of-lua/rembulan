package net.sandius.rembulan.lib;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.testenv.TestBase;

/**
 * The io library reports errors as PUC-Lua 5.3 does (#55): failures as
 * {@code nil, "[file name: ]message", errno} ({@code luaL_fileresult}), with the C library's
 * message and number, and raised errors without Java exception names. The expected messages are
 * PUC-Lua 5.3.6's on Linux.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IoLib_ErrorMessageTest extends TestBase {

  /** Lua: the message of the error {@code f(...)} raises, without a position in front. */
  private static final String ERROR_OF = "local function errorOf(f, ...)\n"
      + "  local ok, msg = pcall(f, ...)\n"
      + "  assert(not ok, 'no error')\n"
      + "  return (msg:gsub('^[^\\n]-:%d+: ', ''))\n"
      + "end\n";

  private static boolean isWindows() {
    return File.separatorChar == '\\';
  }

  @Test
  public void test_open_missing_file() throws Exception {
    Path path = createTempFilename();
    Object[] actual = run("return io.open(...)", path.toString());
    assertThat(actual).containsExactly(null, path + ": No such file or directory", 2L);
  }

  @Test
  public void test_open_for_writing_in_a_missing_directory() throws Exception {
    Path path = createTempFilename().resolve("x");
    Object[] actual = run("return io.open(..., 'w')", path.toString());
    assertThat(actual).containsExactly(null, path + ": No such file or directory", 2L);
  }

  @Test
  public void test_open_without_permission() throws Exception {
    Assume.assumeFalse(isWindows());
    Path path = createTempFile("secret");
    Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("---------"));
    Assume.assumeFalse(Files.isReadable(path)); // e.g. root can read it anyway
    Object[] actual = run("return io.open(...)", path.toString());
    assertThat(actual).containsExactly(null, path + ": Permission denied", 13L);
  }

  @Test
  public void test_open_directory_for_writing() throws Exception {
    Assume.assumeFalse(isWindows());
    Path dir = Files.createDirectory(createTempFilename());
    Object[] actual = run("return io.open(..., 'w')", dir.toString());
    assertThat(actual).containsExactly(null, dir + ": Is a directory", 21L);
  }

  @Test
  public void test_read_directory() throws Exception {
    Assume.assumeFalse(isWindows());
    Path dir = Files.createDirectory(createTempFilename());
    Object[] actual = run("local f = assert(io.open(...)) return f:read('a')", dir.toString());
    assertThat(actual).containsExactly(null, "Is a directory", 21L);
  }

  @Test
  public void test_write_to_a_file_opened_for_reading() throws Exception {
    Path path = createTempFile("x");
    Object[] actual = run("local f = io.open(...) return f:write('y')", path.toString());
    assertThat(actual).containsExactly(null, "Bad file descriptor", 9L);
  }

  @Test
  public void test_read_from_a_file_opened_for_writing() throws Exception {
    Path path = createTempFilename();
    Object[] actual = run("local f = io.open(..., 'w') return f:read('a')", path.toString());
    assertThat(actual).containsExactly(null, "Bad file descriptor", 9L);
  }

  @Test
  public void test_closed_file() throws Exception {
    Path path = createTempFile("x");
    Object[] actual = run(ERROR_OF + "local f = io.open(...) f:close()\n"
        + "return errorOf(f.read, f), errorOf(f.write, f, 'y'), errorOf(f.lines, f),\n"
        + "  errorOf(f.seek, f), errorOf(f.close, f), errorOf(f.flush, f)", path.toString());
    String closed = "attempt to use a closed file";
    assertThat(actual).containsExactly(closed, closed, closed, closed, closed, closed);
  }

  @Test
  public void test_cannot_open_file_for_lines_input_output() throws Exception {
    Path path = createTempFilename();
    Path inMissingDir = path.resolve("x");
    Object[] actual = run(ERROR_OF + "local path, inMissingDir = ...\n"
        + "return errorOf(io.lines, path), errorOf(io.input, path), errorOf(io.output, inMissingDir)",
        path.toString(), inMissingDir.toString());
    assertThat(actual).containsExactly(
        "cannot open file '" + path + "' (No such file or directory)",
        "cannot open file '" + path + "' (No such file or directory)",
        "cannot open file '" + inMissingDir + "' (No such file or directory)");
  }

  @Test
  public void test_invalid_mode() throws Exception {
    Path path = createTempFile("x");
    Object[] actual = run(ERROR_OF + "return errorOf(io.open, ..., 'rz')", path.toString());
    assertThat(actual).containsExactly("bad argument #2 to 'open' (invalid mode)");
  }

  @Test
  public void test_invalid_read_formats() throws Exception {
    Path path = createTempFile("x\ny\n");
    Object[] actual = run(ERROR_OF + "local path = ...\n"
        + "local f = io.open(path)\n"
        + "io.input(f)\n"
        + "return errorOf(f.read, f, 'x'), errorOf(f.read, f, 'l', 'x'), errorOf(f.read, f, 1.5),\n"
        + "  errorOf(io.read, 'x'), errorOf(io.read, 'l', 1.5),\n"
        + "  errorOf(function() for l in io.lines(path, 'x') do end end),\n"
        + "  errorOf(function() for l in f:lines('x') do end end)", path.toString());
    assertThat(actual).containsExactly(
        "bad argument #1 to 'read' (invalid format)",
        "bad argument #2 to 'read' (invalid format)",
        "bad argument #1 to 'read' (number has no integer representation)",
        "bad argument #1 to 'read' (invalid format)",
        "bad argument #2 to 'read' (number has no integer representation)",
        "bad argument #2 to 'for iterator' (invalid format)",
        "bad argument #2 to 'for iterator' (invalid format)");
  }

}
