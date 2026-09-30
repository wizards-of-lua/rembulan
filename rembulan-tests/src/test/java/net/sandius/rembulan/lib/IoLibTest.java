package net.sandius.rembulan.lib;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
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
  public void test_File_lines__Read_file_with_multiple_line_having_each_two_numbers_and_some_text()
      throws Exception {
    // Given:
    Path path = createTempFile("1 2 a\n3 4 b\n100 200 end");
    String program = loadResource("prog24.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("1 2  a\n3 4  b\n100 200  end");
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
  public void test_File_read_next_line__Read_file_with_three_lines_and_eol() throws Exception {
    // Given:
    String content = "one\ntwo\nthree";
    Path path = createTempFile(content);
    String program = loadResource("prog23.lua");

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
  public void test_File_read_next_number__Read_file_with_newline_and_number() throws Exception {
    // Given:
    String content = "\n12";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(12L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_newlines_and_numbers() throws Exception {
    // Given:
    String content = "\n12\n5";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(17L);
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
  public void test_File_read_next_number__Read_file_with_one_negative_number() throws Exception {
    // Given:
    String content = "-123";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(-123L);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_one_number_having_an_exponent()
      throws Exception {
    // Given:
    String content = "12e2";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1200D);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_one_negative_number_having_an_exponent()
      throws Exception {
    // Given:
    String content = "-12e2";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(-1200D);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_one_number_having_an_negative_exponent()
      throws Exception {
    // Given:
    String content = "12e-2";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(0.12D);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_one_negative_number_having_an_negative_exponent()
      throws Exception {
    // Given:
    String content = "-12e-2";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(-0.12D);
  }

  @Test
  public void test_File_read_next_number__Read_file_with_two_consecutive_negative_numbers()
      throws Exception {
    // Given:
    String content = "-1-12";
    Path path = createTempFile(content);
    String program = loadResource("prog5.lua");

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(-13L);
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
    assertThat(actual).isEqualTo(new byte[] {0, 1, 2, 3});
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
    byte[] content = new byte[] {0, 1, 2, 3};
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

  @Test
  public void test_File_write__replaces_old_content() throws Exception {
    // Given:
    String oldContent = "123456789a123456789b";
    String newContent = "123456789c";
    Path path = createTempFile(oldContent);
    String program = loadResource("prog13.lua");

    // When:
    run(program, path.toString(), newContent);

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo(newContent);
  }

  // -------------------------------------------------------------------------
  // r+ (update-read) mode
  // -------------------------------------------------------------------------

  @Test
  public void test_File_open_in_r_plus_mode__write_overwrites_in_place() throws Exception {
    // Given:
    String content = "hello world";
    Path path = createTempFile(content);
    String program = loadResource("prog25.lua");

    // When:
    run(program, path.toString(), "HELLO");

    // Then: first 5 bytes overwritten, tail preserved, file not truncated
    String actual = readFile(path);
    assertThat(actual).isEqualTo("HELLO world");
  }

  @Test
  public void test_File_open_in_r_plus_mode__does_not_create_missing_file() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = loadResource("prog25.lua");

    // When:
    run(program, path.toString(), "any");

    // Then: r+ has no CREATE flag; file must still not exist
    assertThat(Files.exists(path)).isFalse();
  }

  // -------------------------------------------------------------------------
  // w+ (update-write) mode
  // -------------------------------------------------------------------------

  @Test
  public void test_File_open_in_w_plus_mode__truncates_existing_content() throws Exception {
    // Given:
    String oldContent = "old content";
    Path path = createTempFile(oldContent);
    String program = loadResource("prog26.lua");

    // When:
    run(program, path.toString(), "new");

    // Then: file is truncated; only the newly written text is present
    String actual = readFile(path);
    assertThat(actual).isEqualTo("new");
  }

  @Test
  public void test_File_open_in_w_plus_mode__creates_new_file() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = loadResource("prog26.lua");

    // When:
    run(program, path.toString(), "hello");

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo("hello");
  }

  // -------------------------------------------------------------------------
  // a+ (update-append) mode
  // -------------------------------------------------------------------------

  @Test
  public void test_File_open_in_a_plus_mode__write_appends_to_end_regardless_of_seek_position()
      throws Exception {
    // Given:
    String content = "hello";
    Path path = createTempFile(content);
    String program = loadResource("prog27.lua");

    // When: Lua seeks to position 0, then writes — write must still go to EOF
    run(program, path.toString(), " world");

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo("hello world");
  }

  @Test
  public void test_File_open_in_a_plus_mode__creates_new_file() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = loadResource("prog27.lua");

    // When:
    run(program, path.toString(), "hello");

    // Then:
    String actual = readFile(path);
    assertThat(actual).isEqualTo("hello");
  }

  // -------------------------------------------------------------------------
  // Read formats without '*' (Lua 5.3 makes the '*' optional)
  // -------------------------------------------------------------------------

  private static final String READ_FORMATS_PROGRAM = "" //
      + "local filename, f1, f2, f3, f4 = ...\n" //
      + "local file = io.open(filename, 'r')\n" //
      + "local a, b, c, d = file:read(f1), file:read(f2), file:read(f3), file:read(f4)\n" //
      + "file:close()\n" //
      + "return a, b, c, d\n";

  @Test
  public void test_File_read__formats_without_star() throws Exception {
    // Given:
    Path path = createTempFile("12 34\nsecond line\nrest");

    // When:
    Object[] actual = run(READ_FORMATS_PROGRAM, path.toString(), "n", "l", "L", "a");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {12L, " 34", "second line\n", "rest"});
  }

  @Test
  public void test_File_read__formats_with_star() throws Exception {
    // Given:
    Path path = createTempFile("12 34\nsecond line\nrest");

    // When:
    Object[] actual = run(READ_FORMATS_PROGRAM, path.toString(), "*n", "*l", "*L", "*a");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {12L, " 34", "second line\n", "rest"});
  }

  @Test
  public void test_File_read__only_the_first_letter_of_a_format_counts() throws Exception {
    // Given:
    Path path = createTempFile("12 34\nsecond line\nrest");

    // When:
    Object[] actual =
        run(READ_FORMATS_PROGRAM, path.toString(), "number", "*line", "Line", "*all");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {12L, " 34", "second line\n", "rest"});
  }

  @Test
  public void test_File_read__whole_file_at_end_of_file_returns_empty_string() throws Exception {
    // Given:
    Path path = createTempFile("hello");

    // When: "a" never fails, it returns "" at the end of the file (unlike "l" and "n")
    Object[] actual = run(READ_FORMATS_PROGRAM, path.toString(), "a", "a", "*a", "l");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"hello", "", "", null});
  }

  @Test
  public void test_File_read__whole_file_of_empty_file_returns_empty_string() throws Exception {
    // Given:
    Path path = createTempFile("");

    // When:
    Object[] actual = run(READ_FORMATS_PROGRAM, path.toString(), "a", "l", "a", "n");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"", null, "", null});
  }

  @Test
  public void test_File_lines__format_without_star() throws Exception {
    // Given:
    Path path = createTempFile("one\ntwo\n");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local result = ''\n" //
        + "for line in file:lines('L') do result = result .. '[' .. line .. ']' end\n" //
        + "file:close()\n" //
        + "return result\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("[one\n][two\n]");
  }

  @Test
  public void test_File_read__unknown_format_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local ok = pcall(file.read, file, 'x')\n" //
        + "local ok2 = pcall(file.read, file, '*')\n" //
        + "file:close()\n" //
        + "return ok, ok2\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {false, false});
  }

  // -------------------------------------------------------------------------
  // io.type, io.open
  // -------------------------------------------------------------------------

  @Test
  public void test_io_type() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local open = io.type(file)\n" //
        + "file:close()\n" //
        + "return io.type(42), io.type({}), open, io.type(file)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {null, null, "file", "closed file"});
  }

  @Test
  public void test_io_open__missing_file_returns_nil_and_message() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file, msg = io.open(..., 'r')\n" //
        + "return file, type(msg)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isNull();
    assertThat(actual[1]).isEqualTo("string");
  }

  @Test
  public void test_io_open__invalid_mode_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "return pcall(io.open, ..., 'rw')\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

  // -------------------------------------------------------------------------
  // Default files: io.input, io.read, io.output, io.write, io.close, io.flush
  // -------------------------------------------------------------------------

  @Test
  public void test_io_input_and_io_read() throws Exception {
    // Given:
    Path path = createTempFile("one\ntwo\nrest");
    String program = "" //
        + "local file = io.input(...)\n" //
        + "local same = io.input() == file\n" //
        + "local a, b = io.read(), io.read('L')\n" //
        + "local rest = io.read('a')\n" //
        + "io.close(file)\n" //
        + "return io.type(file), same, a, b, rest\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"closed file", true, "one", "two\n", "rest"});
  }

  @Test
  public void test_io_output_io_write_and_io_close() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file = io.output(...)\n" //
        + "io.write('a', 1, 'b')\n" //
        + "local flushed = io.flush()\n" //
        + "local closed = io.close()\n" //
        + "return io.type(file), flushed, closed\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"closed file", true, true});
    assertThat(readFile(path)).isEqualTo("a1b");
  }

  @Test
  public void test_File_flush() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file = io.open(..., 'w')\n" //
        + "file:write('hello')\n" //
        + "local flushed = file:flush()\n" //
        + "file:close()\n" //
        + "return flushed\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(true);
    assertThat(readFile(path)).isEqualTo("hello");
  }

  // -------------------------------------------------------------------------
  // Edge cases of read, lines, seek and write
  // -------------------------------------------------------------------------

  @Test
  public void test_File_read__zero_characters() throws Exception {
    // Given:
    Path path = createTempFile("x");
    Path empty = createTempFile("");

    // When: read(0) tests for end of file: "" before it, nil at it
    Object[] actual = run(READ_FORMATS_PROGRAM, path.toString(), 0, 1, 0, "a");
    Object[] actualEmpty = run(READ_FORMATS_PROGRAM, empty.toString(), 0, "a", "a", "a");

    // Then:
    assertThat(actual[0]).isEqualTo("");
    assertThat(actual[2]).isNull();
    assertThat(actualEmpty[0]).isNull();
  }

  @Test
  public void test_File_read__number_fails_on_text() throws Exception {
    // Given:
    Path path = createTempFile("x");

    // When:
    Object[] actual = run(READ_FORMATS_PROGRAM, path.toString(), "n", "a", "a", "a");

    // Then:
    assertThat(actual[0]).isNull();
  }

  @Test
  public void test_File_lines__numbers() throws Exception {
    // Given:
    Path path = createTempFile("1 2.5 3");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local result = {}\n" //
        + "for n in file:lines('n') do result[#result + 1] = n end\n" //
        + "file:close()\n" //
        + "return table.unpack(result)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {1L, 2.5, 3L});
  }

  @Test
  public void test_File_lines__several_formats() throws Exception {
    // Given:
    Path path = createTempFile("a\nb\nc\nd\n");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local result = ''\n" //
        + "for x, y in file:lines('l', 'l') do result = result .. x .. y .. ',' end\n" //
        + "file:close()\n" //
        + "return result\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("ab,cd,");
  }

  @Test
  public void test_File_seek__defaults_to_current_position() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local before = file:seek()\n" //
        + "file:read(2)\n" //
        + "local after = file:seek()\n" //
        + "local fromCur = file:seek('cur')\n" //
        + "file:close()\n" //
        + "return before, after, fromCur\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {0L, 2L, 2L});
  }

  @Test
  public void test_File_seek__invalid_option_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local ok = pcall(file.seek, file, 'foo')\n" //
        + "file:close()\n" //
        + "return ok\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

  @Test
  public void test_File_write__table_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file = io.open(..., 'w')\n" //
        + "local ok = pcall(file.write, file, {})\n" //
        + "file:close()\n" //
        + "return ok\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

  @Test
  public void test_File_write__returns_the_file() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file = io.open(..., 'w')\n" //
        + "local same = file:write('a'):write('b') == file\n" //
        + "file:close()\n" //
        + "return same\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(true);
    assertThat(readFile(path)).isEqualTo("ab");
  }

  // -------------------------------------------------------------------------
  // Deviations from Lua 5.3, fixed (#17 - #24)
  // -------------------------------------------------------------------------

  @Test
  public void test_io_output__without_argument_returns_the_default_output_file()
      throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file = io.output(...)\n" //
        + "local same = io.output() == file\n" //
        + "io.close()\n" //
        + "return same\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(true);
  }

  @Test
  public void test_File_read__zero_characters_consumes_nothing() throws Exception {
    // Given:
    Path path = createTempFile("xy");

    // When:
    Object[] actual = run(READ_FORMATS_PROGRAM, path.toString(), 0, 1, 0, "a");

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"", "x", "", "y"});
  }

  @Test
  public void test_File_read__stops_at_the_first_format_that_fails() throws Exception {
    // Given:
    Path path = createTempFile("x\n");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local n = select('#', file:read('l', 'l', 'l'))\n" //
        + "file:close()\n" //
        + "file = io.open(..., 'r')\n" //
        + "local a, b = file:read('l', 'l', 'l')\n" //
        + "file:close()\n" //
        + "return n, a, b\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {2L, "x", null});
  }

  @Test
  public void test_File_close__closing_a_closed_file_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "file:close()\n" //
        + "return pcall(file.close, file)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

  @Test
  public void test_File_tostring() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local open = tostring(file)\n" //
        + "file:close()\n" //
        + "return open:match('^file %(.+%)$') ~= nil, tostring(file)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {true, "file (closed)"});
  }

  @Test
  public void test_File_read__write_only_file_returns_nil_and_message() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'a')\n" //
        + "local ok, value, msg = pcall(file.read, file, 'a')\n" //
        + "file:close()\n" //
        + "return ok, value, type(msg)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {true, null, "string"});
  }

  @Test
  public void test_File_setvbuf() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file = io.open(..., 'w')\n" //
        + "local a, b, c = file:setvbuf('no'), file:setvbuf('full', 1024), file:setvbuf('line')\n" //
        + "file:close()\n" //
        + "return a, b, c\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {true, true, true});
  }

  @Test
  public void test_io_lines() throws Exception {
    // Given:
    Path path = createTempFile("one\ntwo\n");
    String program = "" //
        + "local result = ''\n" //
        + "for line in io.lines(...) do result = result .. '[' .. line .. ']' end\n" //
        + "for a, b in io.lines(..., 1, 'l') do result = result .. a .. '|' .. b end\n" //
        + "return result\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("[one][two]o|net|wo");
  }

  @Test
  public void test_io_lines__closes_the_file_at_its_end() throws Exception {
    // Given:
    Path path = createTempFile("one\n");
    String program = "" //
        + "local iterator = io.lines(...)\n" //
        + "for line in iterator do end\n" //
        + "return pcall(iterator)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {false, "file is already closed"});
  }

  @Test
  public void test_io_lines__without_file_name_reads_the_default_input_and_keeps_it_open()
      throws Exception {
    // Given:
    Path path = createTempFile("one\ntwo\n");
    String program = "" //
        + "local file = io.input(...)\n" //
        + "local result = ''\n" //
        + "for line in io.lines() do result = result .. '[' .. line .. ']' end\n" //
        + "for n in io.lines(nil, 'n') do result = result .. n end\n" //
        + "return result, io.type(file)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual).isEqualTo(new Object[] {"[one][two]", "file"});
  }

  @Test
  public void test_io_lines__missing_file_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "return pcall(io.lines, ...)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

  @Test
  public void test_File_lines__closed_file_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFile("one\ntwo\n");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local iterator = file:lines()\n" //
        + "local first = iterator()\n" //
        + "file:close()\n" //
        + "local ok = pcall(iterator)\n" //
        + "return first, ok, pcall(file.lines, file)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo("one");
    assertThat(actual[1]).isEqualTo(false);
    assertThat(actual[2]).isEqualTo(false);
  }

  @Test
  public void test_File_lines__failed_read_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'a')\n" //
        + "local ok = pcall(file:lines())\n" //
        + "file:close()\n" //
        + "return ok\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

  @Test
  public void test_File_read__stops_at_a_number_that_fails() throws Exception {
    // Given:
    Path path = createTempFile("x\n");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "local n = select('#', file:read('n', 'l'))\n" //
        + "file:close()\n" //
        + "return n\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(1L);
  }

  @Test
  public void test_File_read__closed_file_raises_an_error_also_for_write_only_files()
      throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'a')\n" //
        + "file:close()\n" //
        + "return pcall(file.read, file, 'a')\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

  @Test
  public void test_File_setvbuf__invalid_option_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFilename(); // path that does not exist
    String program = "" //
        + "local file = io.open(..., 'w')\n" //
        + "local ok = pcall(file.setvbuf, file, 'always')\n" //
        + "file:close()\n" //
        + "return ok, pcall(file.setvbuf, file, 'no')\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
    assertThat(actual[1]).isEqualTo(false);
  }

  @Test
  public void test_io_close__closing_a_closed_file_raises_an_error() throws Exception {
    // Given:
    Path path = createTempFile("hello");
    String program = "" //
        + "local file = io.open(..., 'r')\n" //
        + "io.close(file)\n" //
        + "return pcall(io.close, file)\n";

    // When:
    Object[] actual = run(program, path.toString());

    // Then:
    assertThat(actual[0]).isEqualTo(false);
  }

}
