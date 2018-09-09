package net.sandius.rembulan.lib;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sandius.rembulan.testenv.TestBase;

@FixMethodOrder(MethodSorters.JVM)
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
  public void test_readFile_with_two_lines_separated_by_carriage_return() throws Exception {
    // Given:
    String content = "hello\rworld";
    Path path = createTempFile(content);
    String program = loadResource("readFile.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("hello\nworld");
  }
  
  @Test
  public void test_readFile_with_two_lines_separated_by_carriage_return_line_feed() throws Exception {
    // Given:
    String content = "hello\r\nworld";
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
  
  @Test
  public void test_readFile3_with_one_digit() throws Exception {
    // Given:
    String content = "1";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(1L);
  }
  
  @Test
  public void test_readFile3_with_two_digits() throws Exception {
    // Given:
    String content = "12";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(12L);
  }
  
  @Test
  public void test_readFile3_with_two_digits_separated_by_space() throws Exception {
    // Given:
    String content = "8 2";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(10L);
  }
  
  @Test
  public void test_readFile3_with_two_numbers_separated_by_space() throws Exception {
    // Given:
    String content = "10 20";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(30L);
  }
  
  @Test
  public void test_readFile3_with_two_digits_separated_by_newline() throws Exception {
    // Given:
    String content = "8\n2";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(10L);
  }
  
  @Test
  public void test_readFile3_with_two_digits_separated_by_carriage_return() throws Exception {
    // Given:
    String content = "8\r2";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(10L);
  }
  
  @Test
  public void test_readFile3_with_two_numbers_separated_by_newline() throws Exception {
    // Given:
    String content = "10\n20";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(30L);
  }
  
  @Test
  public void test_readFile3_with_three_numbers_separated_by_newline() throws Exception {
    // Given:
    String content = "11\n22\n33";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(66L);
  }
  
  @Test
  public void test_readFile3_with_five_numbers_separated_by_newline() throws Exception {
    // Given:
    String content = "1\n2\n3\n4\n5\n";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(15L);
  }
  
  @Test
  public void test_readFile3_with_one_decimal_number() throws Exception {
    // Given:
    String content = "1.1";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(1.1D);
  }
  
  @Test
  public void test_readFile3_with_two_decimal_numbers_seperated_by_space() throws Exception {
    // Given:
    String content = "1.1 2.1";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(3.2D);
  }
  
  @Test
  public void test_readFile3_with_two_decimal_numbers_seperated_by_newline() throws Exception {
    // Given:
    String content = "1.1\n2.1";
    Path path = createTempFile(content);
    String program = loadResource("readFile3.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(3.2D);
  }

  @Test
  public void test_readFile4_number_and_line() throws Exception {
    // Given:
    String content = "1a";
    Path path = createTempFile(content);
    String program = loadResource("readFile4.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(1L);
    assertThat(actual[1]).isEqualTo("a");
  }

  @Test
  public void test_readFile4_line_and_number() throws Exception {
    // Given:
    String content = "a\n1";
    Path path = createTempFile(content);
    String program = loadResource("readFile4.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(1L);
    assertThat(actual[1]).isEqualTo("a");
  }
  
  @Test
  public void test_readFile4_two_numbers_and_line() throws Exception {
    // Given:
    String content = "1\n2\na";
    Path path = createTempFile(content);
    String program = loadResource("readFile4.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(3L);
    assertThat(actual[1]).isEqualTo("a");
  }
  
  @Test
  public void test_readFile4_two_lines_and_number() throws Exception {
    // Given:
    String content = "a\nb\n1";
    Path path = createTempFile(content);
    String program = loadResource("readFile4.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(1L);
    assertThat(actual[1]).isEqualTo("a\nb");
  }
  
  @Test
  public void test_readFile4_spaces_and_text() throws Exception {
    // Given:
    String content = "   aaaa";
    Path path = createTempFile(content);
    String program = loadResource("readFile4.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(null);
    assertThat(actual[1]).isEqualTo("aaaa");
  }
  
  @Test
  public void test_readFile4_spaces_and_text_and_spaces() throws Exception {
    // Given:
    String content = "   bbbb   ";
    Path path = createTempFile(content);
    String program = loadResource("readFile4.lua");

    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo(null);
    assertThat(actual[1]).isEqualTo("bbbb   ");
  }

}
