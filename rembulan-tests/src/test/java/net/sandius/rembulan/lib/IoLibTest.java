package net.sandius.rembulan.lib;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

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
    String actual = getString(output);
    assertThat(actual).isEqualTo("a=aaa\tb=bbb\n");
  }

  @Test
  public void test_readFile_with_single_line() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = loadResource("readFile.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello");
  }

  @Test
  public void test_readFile_with_single_line_and_new_line() throws Exception {
    // Given:
    String content = "hello\n";
    Path path = createTempFile(content);
    String program = loadResource("readFile.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello");
  }
  
  @Test
  public void test_readFile_with_single_line_and_2_new_lines() throws Exception {
    // Given:
    String content = "hello\n\n";
    Path path = createTempFile(content);
    String program = loadResource("readFile.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("hello\n");
  }
  
  @Test
  public void test_readFile_with_single_line_and_3_new_lines() throws Exception {
    // Given:
    String content = "hello\n\n\n";
    Path path = createTempFile(content);
    String program = loadResource("readFile.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("hello\n\n");
  }
  
  @Test
  public void test_readFile_with_two_lines() throws Exception {
    // Given:
    String content = "hello\nworld";
    Path path = createTempFile(content);
    String program = loadResource("readFile.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("hello\nworld");
  }
  
  @Test
  public void test_readFile_with_three_lines() throws Exception {
    // Given:
    String content = "one\ntwo\nthree";
    Path path = createTempFile(content);
    String program = loadResource("readFile.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("one\ntwo\nthree");
  }
  
  @Test
  public void test_readFile2_with_three_lines() throws Exception {
    // Given:
    String content = "one\ntwo\nthree";
    Path path = createTempFile(content);
    String program = loadResource("readFile2.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("one\ntwo\nthree");
  }

}
