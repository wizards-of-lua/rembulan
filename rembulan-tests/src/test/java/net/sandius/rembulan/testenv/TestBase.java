package net.sandius.rembulan.testenv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.CallException;
import net.sandius.rembulan.exec.CallPausedException;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.lib.IoLib;
import net.sandius.rembulan.lib.StandardLibrary;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.LuaFunction;

public class TestBase extends Assertions {

  private final Path tempDir = FileSystems.getDefault().getPath("temp").toAbsolutePath();
  private PrintStream out = System.out;

  @Before
  public final void before() throws IOException {
    if (Files.exists(tempDir)) {
      deleteDirectory(tempDir);
    }
    Files.createDirectories(tempDir);
  }

  private void deleteDirectory(Path dir) throws IOException {
    Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(this::deleteFile);
  }

  private void deleteFile(Path p) {
    try {
      Files.delete(p);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public PrintStream getOut() {
    return out;
  }

  public void setOut(PrintStream out) {
    this.out = out;
  }

  public String loadResource(String name) {
    try {
      return new String(Files.readAllBytes(Paths.get(getClass().getResource(name).toURI())),
          "UTF-8");
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public Object[] run(String program, Object... args)
      throws LoaderException, CallException, CallPausedException, InterruptedException {
    // compile the program
    StateContext state = StateContexts.newDefaultInstance();
    RuntimeEnvironment runtimeEnvironment = RuntimeEnvironments.system(System.in, out, System.err);

    Table env = StandardLibrary.in(runtimeEnvironment).installInto(state);
    IoLib.installInto(state, env, runtimeEnvironment);

    ChunkLoader loader = CompilerChunkLoader.of("LuaProgramBytecode");
    LuaFunction main = loader.loadTextChunk(new Variable(env), "LuaProgramChunk", program);

    // run program in order to declare the "dummy" function
    Object[] result = DirectCallExecutor.newExecutor().call(state, main, args);
    return result;
  }

  public String getString(ByteArrayOutputStream output) throws UnsupportedEncodingException {
    return new String(output.toByteArray(), "UTF-8");
  }

  public ByteArrayOutputStream captureOutput() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream captureOut = new PrintStream(output, true);
    setOut(captureOut);
    return output;
  }

  public Path createTempFile(String content) throws IOException {
    Path result = Files.createTempFile(tempDir, "temp", ".txt");
    Files.write(result, content.getBytes(StandardCharsets.UTF_8));
    return result;
  }

  public Path createTempBinaryFile(byte[] content) throws IOException {
    Path result = Files.createTempFile(tempDir, "temp", ".txt");
    Files.write(result, content);
    return result;
  }

  public Path createTempFilename() throws IOException {
    Path result = Files.createTempFile(tempDir, "temp", ".txt");
    deleteFile(result);
    return result;
  }

  public String readFile(Path path) throws IOException {
    byte[] bytes = Files.readAllBytes(path);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public byte[] readBinaryFile(Path path) throws IOException {
    return Files.readAllBytes(path);
  }

}
