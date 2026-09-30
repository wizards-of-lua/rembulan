package net.sandius.rembulan.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.env.TempFileFactory;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.testenv.TestBase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IoLib_TmpFileTest extends TestBase {

  private StateContext state;
  private Table env;
  private IoLib ioLib;
  private int chunkCount;

  private void install(RuntimeEnvironment runtimeEnvironment) {
    state = StateContexts.newDefaultInstance();
    env = StandardLibrary.in(runtimeEnvironment).installInto(state);
    ioLib = IoLib.installInto(state, env, runtimeEnvironment);
  }

  private Object[] exec(String program, Object... args) throws Exception {
    LuaFunction main = CompilerChunkLoader.of("TmpFileTest" + chunkCount++)
        .loadTextChunk(new Variable(env), "TmpFileTest", program);
    return DirectCallExecutor.newExecutor().call(state, main, args);
  }

  /** The system environment, but with its temporary files in {@code dir}. */
  private static RuntimeEnvironment tempFilesIn(Path dir) {
    RuntimeEnvironment system = RuntimeEnvironments.system();
    return new DelegatingEnvironment(system) {
      @Override
      public TempFileFactory tempFileFactory() {
        return () -> {
          Path path = Files.createTempFile(dir, "lua", ".tmp");
          return Files.newByteChannel(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
              StandardOpenOption.DELETE_ON_CLOSE);
        };
      }
    };
  }

  private static long countFiles(Path dir) throws IOException {
    try (Stream<Path> files = Files.list(dir)) {
      return files.count();
    }
  }

  @Test
  public void test_io_tmpfile__is_a_file_opened_for_reading_and_writing() throws Exception {
    // Given:
    install(RuntimeEnvironments.system());
    String program = "" //
        + "local file = io.tmpfile()\n" //
        + "file:write('hello', ' ', 'world')\n" //
        + "file:seek('set', 6)\n" //
        + "local content = file:read('a')\n" //
        + "local open = io.type(file)\n" //
        + "file:close()\n" //
        + "return open, content, io.type(file)\n";

    // When:
    Object[] actual = exec(program);

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"file", "world", "closed file"});
  }

  @Test
  public void test_io_tmpfile__is_removed_when_closed() throws Exception {
    // Given:
    Path dir = createTempFilename();
    Files.createDirectories(dir);
    install(tempFilesIn(dir));

    // When:
    exec("tmp = io.tmpfile(); tmp:write('hello')");
    long whileOpen = countFiles(dir);
    exec("tmp:close()");

    // Then:
    assertThat(whileOpen).isEqualTo(1);
    assertThat(countFiles(dir)).isEqualTo(0);
  }

  @Test
  public void test_io_tmpfile__not_available_without_a_factory() throws Exception {
    // Given: an environment whose tempFileFactory is null
    install(new DelegatingEnvironment(RuntimeEnvironments.system()));
    String program = "" //
        + "local ok, msg = pcall(io.tmpfile)\n" //
        + "return ok, msg\n";

    // When:
    Object[] actual = exec(program);

    // Then:
    assertThat(actual[0]).isEqualTo(false);
    assertThat(String.valueOf(actual[1])).contains("temporary files are not available");
  }

  @Test
  public void test_closeAll__closes_the_open_files_and_removes_temporary_files()
      throws Exception {
    // Given:
    Path dir = createTempFilename();
    Files.createDirectories(dir);
    install(tempFilesIn(dir));
    Path path = createTempFile("one\ntwo\n");
    exec("" //
        + "local path = ...\n" //
        + "tmp = io.tmpfile()\n" //
        + "opened = io.open(path, 'r')\n" //
        + "input = io.input(path)\n" //
        + "lines = io.lines(path)\n" //
        + "lines()\n" //
        + "closed = io.open(path, 'r')\n" //
        + "closed:close()\n", path.toString());

    // When:
    ioLib.closeAll();

    // Then:
    Object[] actual = exec("" //
        + "return io.type(tmp), io.type(opened), io.type(input), io.type(closed),\n" //
        + "  pcall(lines)\n");
    assertThat(actual).startsWith("closed file", "closed file", "closed file", "closed file",
        false);
    assertThat(countFiles(dir)).isEqualTo(0);
  }

  @Test
  public void test_closeAll__leaves_the_standard_files_open() throws Exception {
    // Given:
    captureOutput();
    install(RuntimeEnvironments.system(System.in, getOut(), System.err));

    // When:
    ioLib.closeAll();

    // Then:
    Object[] actual = exec("return io.type(io.stdout), io.type(io.output())");
    assertThat(actual).isEqualTo(new Object[] {"file", "file"});
  }

  /** Delegates everything but the temporary files to another environment: it has none. */
  private static class DelegatingEnvironment implements RuntimeEnvironment {

    private final RuntimeEnvironment delegate;

    DelegatingEnvironment(RuntimeEnvironment delegate) {
      this.delegate = delegate;
    }

    @Override
    public InputStream standardInput() {
      return delegate.standardInput();
    }

    @Override
    public OutputStream standardOutput() {
      return delegate.standardOutput();
    }

    @Override
    public OutputStream standardError() {
      return delegate.standardError();
    }

    @Override
    public FileSystem fileSystem() {
      return delegate.fileSystem();
    }

    @Override
    public String getEnv(String name) {
      return delegate.getEnv(name);
    }

    @Override
    public double getCpuTime() {
      return delegate.getCpuTime();
    }

    @Override
    public TempFileFactory tempFileFactory() {
      return null;
    }

  }

}
