package net.sandius.rembulan.compiler;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * Chunks with many constants, or with a string constant over 64 KB, compile and run, although a
 * class file limits its static initialiser to 64 KB of bytecode, its constant pool to 65535 entries
 * and each string constant to 65535 bytes.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LargeChunkTest extends TestBase {

  @Test
  public void test_table_literal_with_6000_records() throws Exception {
    int n = 6000;
    StringBuilder program = new StringBuilder("local t = {\n");
    for (int i = 0; i < n; i++) {
      program.append("  {x=").append(i).append(", y=").append(i * 2).append(", f=").append(i)
          .append(".5, name=\"block_").append(i).append("\"},\n");
    }
    program.append("}\n");
    program.append("for i = 1, #t do\n");
    program.append("  local r = t[i]\n");
    program.append("  if r.x ~= i - 1 or r.y ~= (i - 1) * 2 or r.f ~= i - 0.5\n");
    program.append("      or math.type(r.y) ~= 'integer' or math.type(r.f) ~= 'float'\n");
    program.append("      or r.name ~= 'block_' .. (i - 1) then\n");
    program.append("    return i\n");
    program.append("  end\n");
    program.append("end\n");
    program.append("return #t\n");

    Object[] actual = run(program.toString());

    assertThat(actual).isEqualTo(new Object[] {(long) n});
  }

  @Test
  public void test_more_than_32768_constants_beyond_the_fields() throws Exception {
    // each value is a constant of its own, and the table positions exceed 32767 too
    int n = 34000;
    StringBuilder program = new StringBuilder("local t = {");
    for (int i = 0; i < n; i++) {
      program.append(i * 1000003L).append(", ");
    }
    program.append("}\n");
    program.append("for i = 1, #t do\n");
    program.append("  if t[i] ~= (i - 1) * 1000003 then\n");
    program.append("    return i\n");
    program.append("  end\n");
    program.append("end\n");
    program.append("return #t\n");

    Object[] actual = run(program.toString());

    assertThat(actual).isEqualTo(new Object[] {(long) n});
  }

  @Test
  public void test_string_literal_over_64KB() throws Exception {
    // decimal escapes keep the source ASCII; 5 bytes per repetition, 150000 bytes in total
    String unit = "xy\\0\\128\\255";
    StringBuilder literal = new StringBuilder();
    for (int i = 0; i < 30000; i++) {
      literal.append(unit);
    }
    String program = "local s = \"" + literal + "\"\n"
        + "return #s, s == string.rep(\"" + unit + "\", 30000), s:byte(3), s:byte(4), s:byte(-1)";

    Object[] actual = run(program);

    assertThat(actual).isEqualTo(new Object[] {150000L, true, 0L, 128L, 255L});
  }

  @Test
  public void test_special_numbers_beyond_the_fields() throws Exception {
    StringBuilder program = new StringBuilder("local filler = {");
    for (int i = 0; i < 2000; i++) {
      program.append("'f").append(i).append("', ");
    }
    program.append("}\n");
    program.append("local z = -0.0\n");
    program.append("local min = math.mininteger\n");
    program.append("return 1 / z == -math.huge, -9223372036854775807 - 1 == min, 0x7fffffffffffffff,"
        + " 1e308, #filler\n");

    Object[] actual = run(program.toString());

    assertThat(actual).isEqualTo(new Object[] {true, true, Long.MAX_VALUE, 1e308, 2000L});
  }

  @Test
  public void test_many_constants_in_a_nested_function() throws Exception {
    int n = 5000;
    StringBuilder program = new StringBuilder("local function make()\n  return {");
    for (int i = 0; i < n; i++) {
      program.append("'s").append(i).append("', ");
    }
    program.append("}\nend\n");
    program.append("local t = make()\n");
    program.append("return #t, t[1], t[" + n + "], make()[4321]\n");

    Object[] actual = run(program.toString());

    assertThat(actual).isEqualTo(new Object[] {(long) n, "s0", "s" + (n - 1), "s4320"});
  }

}
