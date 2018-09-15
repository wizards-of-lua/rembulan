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
  public void test_return__2_values() throws Exception {
    // Given:
    String program = loadResource("prog1.lua");

    // When:
    Object[] actual = run(program, "aaa", "bbb");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"aaa", "bbb"});
  }

  @Test
  public void test_print__2_values() throws Exception {
    ByteArrayOutputStream output = captureOutput();
    // Given:
    String program = loadResource("prog2.lua");

    // When:
    run(program, "aaa", "bbb");

    // Then:
    String actual = getString(output);
    assertThat(actual).isEqualTo("a=aaa\tb=bbb\n");
  }

  @Test
  public void test_File_lines__Read_file_with_single_line_having_one_character() throws Exception {
    // Given:
    Path path = createTempFile("a");
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("a");
  }

  @Test
  public void test_File_lines__Read_file_with_single_line() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello");
  }

  @Test
  public void test_File_lines__Read_file_with_single_line_and_new_line() throws Exception {
    // Given:
    String content = "hello\n";
    Path path = createTempFile(content);
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello");
  }

  @Test
  public void test_File_lines__Read_file_with_single_line_and_2_new_lines() throws Exception {
    // Given:
    String content = "hello\n\n";
    Path path = createTempFile(content);
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello\n");
  }

  @Test
  public void test_File_lines__Read_file_with_single_line_and_3_new_lines() throws Exception {
    // Given:
    String content = "hello\n\n\n";
    Path path = createTempFile(content);
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello\n\n");
  }

  @Test
  public void test_File_lines__Read_file_with_two_lines() throws Exception {
    // Given:
    String content = "hello\nworld";
    Path path = createTempFile(content);
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello\nworld");
  }

  @Test
  public void test_File_lines__Read_file_with_two_lines_separated_by_carriage_return()
      throws Exception {
    // Given:
    String content = "hello\rworld";
    Path path = createTempFile(content);
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello\rworld");
  }

  @Test
  public void test_File_lines__Read_file_with_two_lines_separated_by_carriage_return_line_feed()
      throws Exception {
    // Given:
    String content = "hello\r\nworld";
    Path path = createTempFile(content);
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("hello\r\nworld");
  }

  @Test
  public void test_File_lines__Read_file_with_three_lines() throws Exception {
    // Given:
    String content = "one\ntwo\nthree";
    Path path = createTempFile(content);
    String program = loadResource("prog3.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("one\ntwo\nthree");
  }

  @Test
  public void test_File_read_next_line__Read_file_with_three_lines() throws Exception {
    // Given:
    String content = "one\ntwo\nthree";
    Path path = createTempFile(content);
    String program = loadResource("prog4.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("one\ntwo\nthree");
  }

  @Test
  public void test_File_read_next_numer__Read_file_with_one_digit() throws Exception {
    // Given:
    String content = "1";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_digits() throws Exception {
    // Given:
    String content = "12";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(12L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_digits_separated_by_space()
      throws Exception {
    // Given:
    String content = "8 2";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(10L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_numbers_separated_by_space()
      throws Exception {
    // Given:
    String content = "10 20";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(30L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_long_numbers_separated_by_space()
      throws Exception {
    // Given:
    String content = "123456789 234567890";
    long expected = 123456789L + 234567890;
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(expected);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_digits_separated_by_newline()
      throws Exception {
    // Given:
    String content = "8\n2";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(10L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_digits_separated_by_carriage_return()
      throws Exception {
    // Given:
    String content = "8\r2";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(10L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_numbers_separated_by_newline()
      throws Exception {
    // Given:
    String content = "10\n20";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(30L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_three_numbers_separated_by_newline()
      throws Exception {
    // Given:
    String content = "11\n22\n33";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(66L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_five_numbers_separated_by_newline()
      throws Exception {
    // Given:
    String content = "1\n2\n3\n4\n5\n";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(15L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_one_decimal_number() throws Exception {
    // Given:
    String content = "1.1";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1.1D);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_decimal_numbers_seperated_by_space()
      throws Exception {
    // Given:
    String content = "1.1 2.1";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(3.2D);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_decimal_numbers_seperated_by_newline()
      throws Exception {
    // Given:
    String content = "1.1\n2.1";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(3.2D);
  }

  @Test
  public void test_File_read_next_number_and_next_line__Read_file_number_and_line()
      throws Exception {
    // Given:
    String content = "1a";
    Path path = createTempFile(content);
    String program = loadResource("prog6.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1L);
    assertThat(actual[1]).isEqualTo("a");
  }

  @Test
  public void test_File_read_next_number_and_next_line__Read_file_line_and_number()
      throws Exception {
    // Given:
    String content = "a\n1";
    Path path = createTempFile(content);
    String program = loadResource("prog6.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1L);
    assertThat(actual[1]).isEqualTo("a");
  }

  @Test
  public void test_File_read_next_number_and_next_line__Read_file_two_numbers_and_line()
      throws Exception {
    // Given:
    String content = "1\n2\na";
    Path path = createTempFile(content);
    String program = loadResource("prog6.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(3L);
    assertThat(actual[1]).isEqualTo("a");
  }

  @Test
  public void test_File_read_next_number_and_next_line__Read_file_two_lines_and_number()
      throws Exception {
    // Given:
    String content = "a\nb\n1";
    Path path = createTempFile(content);
    String program = loadResource("prog6.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1L);
    assertThat(actual[1]).isEqualTo("a\nb");
  }

  @Test
  public void test_File_read_next_number_and_next_line__Read_file_spaces_and_text()
      throws Exception {
    // Given:
    String content = "   aaaa";
    Path path = createTempFile(content);
    String program = loadResource("prog6.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(null);
    assertThat(actual[1]).isEqualTo("aaaa");
  }

  @Test
  public void test_File_read_next_number_and_next_line__Read_file_spaces_and_text_and_spaces()
      throws Exception {
    // Given:
    String content = "   bbbb   ";
    Path path = createTempFile(content);
    String program = loadResource("prog6.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(null);
    assertThat(actual[1]).isEqualTo("bbbb   ");
  }
  
  @Test
  public void test_File_read_next_two_numbers__Read_file_with_numbers_separated_by_comma()
      throws Exception {
    // Given:
    String content = "1,2,3,4,5,6";
    Path path = createTempFile(content);
    String program = loadResource("prog19.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1L);    
  }
  
  @Test
  public void test_File_read_next_two_numbers__Read_file_with_numbers_separated_by_space()
      throws Exception {
    // Given:
    String content = "1 2 3 4 5 6";
    Path path = createTempFile(content);
    String program = loadResource("prog19.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(21L);    
  }

  @Test
  public void test_File_seek_and_read_next_line__Seek_set_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog7.lua");

    // When:
    Object[] actual = run(program, path.toString(), "set", 10);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789b123456789c");
  }

  @Test
  public void test_File_seek_and_read_next_line__Seek_set_0() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog7.lua");

    // When:
    Object[] actual = run(program, path.toString(), "set", 0);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789a123456789b123456789c");
  }

  @Test
  public void test_File_seek_and_read_next_line__Seek_set_20() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog7.lua");

    // When:
    Object[] actual = run(program, path.toString(), "set", 20);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789c");
  }

  @Test
  public void test_File_seek_and_read_next_line__Seek_end_1() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog7.lua");

    // When:
    Object[] actual = run(program, path.toString(), "end", -1);

    // Then:
    assertThat(actual[0]).isEqualTo("c");
  }

  @Test
  public void test_File_seek_and_read_next_line__seek_end_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog7.lua");

    // When:
    Object[] actual = run(program, path.toString(), "end", -10);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789c");
  }

  @Test
  public void test_File_seek_and_read_next_line__One_line_set_5_cur_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog8.lua");

    // When:
    Object[] actual = run(program, path.toString(), "set", 5, "cur", 10);

    // Then:
    assertThat(actual[0]).isEqualTo("6789a123456789b123456789c");
    assertThat(actual[1]).isNull();
  }

  @Test
  public void test_File_seek_and_read_next_line__One_line_set_5_end_minus_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog8.lua");

    // When:
    Object[] actual = run(program, path.toString(), "set", 5, "end", -10);

    // Then:
    assertThat(actual[0]).isEqualTo("6789a123456789b123456789c");
    assertThat(actual[1]).isEqualTo("123456789c");
  }

  @Test
  public void test_File_seek_and_read_next_line__Two_lines_set_5_cur_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c\n123456789d123456789e123456789f";
    Path path = createTempFile(content);
    String program = loadResource("prog8.lua");

    // When:
    Object[] actual = run(program, path.toString(), "set", 5, "cur", 10);

    // Then:
    assertThat(actual[0]).isEqualTo("6789a123456789b123456789c");
    assertThat(actual[1]).isEqualTo("123456789e123456789f");
  }

  @Test
  public void test_File_seek_and_read_next_line__One_line_end_0_set_0() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c";
    Path path = createTempFile(content);
    String program = loadResource("prog8.lua");

    // When:
    Object[] actual = run(program, path.toString(), "end", 0, "set", 0);

    // Then:
    assertThat(actual[0]).isNull();
    assertThat(actual[1]).isEqualTo("123456789a123456789b123456789c");
  }

  @Test
  public void test_File_read_rest_of_file__Two_lines() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c\n123456789d123456789e123456789f";
    Path path = createTempFile(content);
    String program = loadResource("prog9.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(content);
  }

  @Test
  public void test_File_seek_and_read_rest_of_file___two_lines_set_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c\n123456789d123456789e123456789f";
    Path path = createTempFile(content);
    String program = loadResource("prog10.lua");

    // When:
    Object[] actual = run(program, path.toString(), "set", 10);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789b123456789c\n123456789d123456789e123456789f");
  }

  @Test
  public void test_File_read_next_chunk__One_line_len_1() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFile(content);
    String program = loadResource("prog11.lua");

    // When:
    Object[] actual = run(program, path.toString(), 1);

    // Then:
    assertThat(actual[0]).isEqualTo("1");
  }

  @Test
  public void test_File_read_next_chunk__One_line_len_10() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFile(content);
    String program = loadResource("prog11.lua");

    // When:
    Object[] actual = run(program, path.toString(), 10);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789a");
  }

  @Test
  public void test_File_read_next_chunk__One_line_len_20() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFile(content);
    String program = loadResource("prog11.lua");

    // When:
    Object[] actual = run(program, path.toString(), 20);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789a");
  }

  @Test
  public void test_File_read_next_chunk__Two_lines_len_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c\n123456789d123456789e123456789f";
    Path path = createTempFile(content);
    String program = loadResource("prog11.lua");

    // When:
    Object[] actual = run(program, path.toString(), 10);

    // Then:
    assertThat(actual[0]).isEqualTo("123456789a");
  }

  @Test
  public void test_File_read_next_chunk__One_line_len_is_zero() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFile(content);
    String program = loadResource("prog11.lua");

    // When:
    Object[] actual = run(program, path.toString(), 0);

    // Then:
    assertThat(actual[0]).isEqualTo("");
  }

  @Test
  public void test_File_read_next_chunk__Empty_file_len_is_zero() throws Exception {
    // Given:
    String content = "";
    Path path = createTempFile(content);
    String program = loadResource("prog11.lua");

    // When:
    Object[] actual = run(program, path.toString(), 0);

    // Then:
    assertThat(actual[0]).isEqualTo(null);
  }

  @Test
  public void test_File_read_next_chunk_multiple_times__Two_lines_len_10() throws Exception {
    // Given:
    String content = "123456789a123456789b123456789c\n123456789d123456789e123456789f";
    Path path = createTempFile(content);
    String program = loadResource("prog12.lua");

    // When:
    Object[] actual = run(program, path.toString(), 10);

    // Then:
    assertThat(actual).containsExactly("123456789a", "123456789b", "123456789c", "\n123456789",
        "d123456789", "e123456789", "f");
  }

  @Test
  public void test_File_write__One_line() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFilename();
    String program = loadResource("prog13.lua");

    // When:
    run(program, path.toString(), content);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(content);
  }

  @Test
  public void test_File_write__Two_lines() throws Exception {
    // Given:
    String content = "123456789a\n123456789b";
    Path path = createTempFilename();
    String program = loadResource("prog13.lua");

    // When:
    run(program, path.toString(), content);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(content);
  }

  @Test
  public void test_File_write__Two_strings() throws Exception {
    // Given:
    String str1 = "123456789a";
    String str2 = "123456789b";
    String expected = str1 + str2;
    Path path = createTempFilename();
    String program = loadResource("prog13.lua");

    // When:
    run(program, path.toString(), str1, str2);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void test_File_write__One_number() throws Exception {
    // Given:
    int number = 1;
    String expected = String.valueOf(number);
    Path path = createTempFilename();
    String program = loadResource("prog13.lua");

    // When:
    run(program, path.toString(), number);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void test_File_write__Two_numbers() throws Exception {
    // Given:
    int number1 = 1;
    int number2 = 2;
    String expected = String.valueOf(number1) + String.valueOf(number2);
    Path path = createTempFilename();
    String program = loadResource("prog13.lua");

    // When:
    run(program, path.toString(), number1, number2);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void test_File_write_and_seek__Two_strings_set_9() throws Exception {
    // Given:
    String str1 = "123456789a123456789b";
    String str2 = "_";
    String expected = "123456789_123456789b";
    Path path = createTempFilename();
    String program = loadResource("prog14.lua");

    // When:
    run(program, path.toString(), str1, 9, str2);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void test_File_write_in_append_mode__One_line() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFile(content);
    String line = "123456789b";
    String expected = content + line;
    String program = loadResource("prog15.lua");

    // When:
    run(program, path.toString(), line);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void test_File_seek_and_write_in_append_mode__One_line() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFile(content);
    String line = "123456789b";
    // expect that seek will be ignored!
    String expected = content + line;
    String program = loadResource("prog16.lua");

    // When:
    run(program, path.toString(), line);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void test_File_close__can_not_read_from_closed_file() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFile(content);
    String program = loadResource("prog17.lua");

    // When:
    Exception actual = null;
    try {
      run(program, path.toString());
    } catch (Exception ex) {
      actual = ex;
    }

    // Then:
    assertThat(actual).isNotNull();
  }

  @Test
  public void test_File_close__can_not_write_to_closed_file() throws Exception {
    // Given:
    String content = "123456789a";
    Path path = createTempFilename();
    String program = loadResource("prog18.lua");
    
    // When:
    Exception actual = null;
    try {
      run(program, path.toString(), content);
    } catch (Exception ex) {
      actual = ex;
    }

    // Then:
    assertThat(actual).isNotNull();
  }

  @Test 
  public void test_File_write__bytes() throws Exception {
    // Given:
    String input = "0,1,2,3";
    Path path = createTempFilename();
    String program = loadResource("prog20.lua");
    
    // When:
    run(program, path.toString(), input);
    
    // Then:
    byte[] actual = readBinaryFile(path);
    assertThat(actual).isEqualTo(new byte[] {0,1,2,3});
  }
  
  @Test 
  public void test_File_write__bytes_with_utf8_a_umlaut() throws Exception {
    // Given:
    String input = "195,164";
    Path path = createTempFilename();
    String program = loadResource("prog20.lua");
    
    // When:
    run(program, path.toString(), input);
    
    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo("ä");
  }
  
  @Test 
  public void test_File_write__bytes_with_chinese_symbol() throws Exception {
    // Given:
    String input = "231,154,132";
    Path path = createTempFilename();
    String program = loadResource("prog20.lua");
    
    // When:
    run(program, path.toString(), input);
    
    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo("\u7684");
  }
  
  @Test 
  public void test_File_read__bytes() throws Exception {
    // Given:
    byte[] content = new byte[] {0,1,2,3};
    Path path = createTempBinaryFile(content);
    String program = loadResource("prog21.lua");
    
    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("0,1,2,3");
  }
  
  @Test 
  public void test_File_read__bytes_with_utf8_a_umlaut() throws Exception {
    // Given:
    String content = "ä";
    Path path = createTempFile(content);
    String program = loadResource("prog21.lua");
    
    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("195,164");
  }
  
  @Test 
  public void test_File_read__bytes_with_utf8_chinese_symbol() throws Exception {
    // Given:
    String content = "\u7684";
    Path path = createTempFile(content);
    String program = loadResource("prog21.lua");
    
    // When:
    Object[] actual = run(program, path.toString());
    
    // Then:
    assertThat(actual[0]).isEqualTo("231,154,132");
  }
  
  @Test 
  public void test_File_seek_and_read__bytes_with_utf8_chinese_symbol() throws Exception {
    // Given:
    String content = "\u7684";
    Path path = createTempFile(content);
    String program = loadResource("prog22.lua");
    
    // When:
    Object[] actual = run(program, path.toString(), 1);
    
    // Then:
    assertThat(actual[0]).isEqualTo("154,132");
  }

}
