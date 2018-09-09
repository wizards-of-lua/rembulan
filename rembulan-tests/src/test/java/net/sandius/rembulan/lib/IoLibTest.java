package net.sandius.rembulan.lib;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;

import net.sandius.rembulan.testenv.TestBase;

public class IoLibTest extends TestBase {

  @Test
  public void test1() throws Exception {
    // Given:
    String program = loadResource("test1.lua");

    // When:
    Object[] actual = run(program, "aaa", "bbb");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"aaa", "bbb"});
  }

  @Test
  public void test2() throws Exception {
    ByteArrayOutputStream output = captureOutput();
    // Given:
    String program = loadResource("test2.lua");

    // When:
    run(program, "aaa", "bbb");

    // Then:
    String oo = getString(output);
    System.out.println(oo);
    assertThat(oo).isEqualTo("a=aaa\tb=bbb\n");
  }

  @Test
  @Ignore
  public void test3() throws Exception {
    Path path = createTempFile("hello");
    // Given:
    String program = loadResource("test3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello");
  }

}
