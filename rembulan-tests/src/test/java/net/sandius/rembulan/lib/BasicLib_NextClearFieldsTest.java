package net.sandius.rembulan.lib;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * Lua 5.3 manual, {@code next}: "The behavior of next is undefined if, during the traversal, you
 * assign any value to a non-existent field in the table. You may however modify existing fields.
 * In particular, you may clear existing fields."
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicLib_NextClearFieldsTest extends TestBase {

  @Test
  public void test_clear_every_field_during_pairs() throws Exception {
    String program = "local t = {a=1, b=2, c=3, d=4}\n" //
        + "local visited = 0\n" //
        + "for k in pairs(t) do t[k] = nil; visited = visited + 1 end\n" //
        + "return visited, next(t) == nil";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {4L, true});
  }

  @Test
  public void test_clear_some_fields_during_pairs() throws Exception {
    String program = "local t = {a=1, b=2, c=3, d=4, e=5, f=6}\n" //
        + "local visited = 0\n" //
        + "for k, v in pairs(t) do\n" //
        + "  visited = visited + 1\n" //
        + "  if v % 2 == 0 then t[k] = nil end\n" //
        + "end\n" //
        + "local sum, left = 0, 0\n" //
        + "for _, v in pairs(t) do sum = sum + v; left = left + 1 end\n" //
        + "return visited, left, sum";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {6L, 3L, 9L});
  }

  @Test
  public void test_clear_array_part_during_pairs() throws Exception {
    String program = "local t = {10, 20, 30, 40}\n" //
        + "local visited = 0\n" //
        + "for k in pairs(t) do t[k] = nil; visited = visited + 1 end\n" //
        + "return visited, next(t) == nil";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {4L, true});
  }

  @Test
  public void test_clear_fields_not_yet_visited() throws Exception {
    // clearing every field at the first step: the traversal ends without visiting the others
    String program = "local t = {a=1, b=2, c=3, d=4}\n" //
        + "local visited = 0\n" //
        + "for k in pairs(t) do\n" //
        + "  visited = visited + 1\n" //
        + "  for k2 in pairs({a=1, b=2, c=3, d=4}) do t[k2] = nil end\n" //
        + "end\n" //
        + "return visited, next(t) == nil";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {1L, true});
  }

  @Test
  public void test_clear_current_and_next_field() throws Exception {
    // clears the current field and the one that follows it; every field is still seen exactly once
    String program = "local t = {}\n" //
        + "for i = 1, 10 do t['k' .. i] = i end\n" //
        + "local seen, visited = {}, 0\n" //
        + "for k in pairs(t) do\n" //
        + "  if seen[k] then error('visited twice: ' .. k) end\n" //
        + "  seen[k] = true\n" //
        + "  visited = visited + 1\n" //
        + "  local k2 = next(t, k)\n" //
        + "  t[k] = nil\n" //
        + "  if k2 ~= nil then t[k2] = nil end\n" //
        + "end\n" //
        + "return visited, next(t) == nil";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {5L, true});
  }

  @Test
  public void test_next_with_a_cleared_key() throws Exception {
    // next continues after a key that has just been cleared
    String program = "local t = {}\n" //
        + "t.a = 1; t.b = 2; t.c = 3\n" //
        + "local k1 = next(t)\n" //
        + "local k2 = next(t, k1)\n" //
        + "t[k1] = nil\n" //
        + "t[k2] = nil\n" //
        + "local k3 = next(t, k1)\n" //
        + "return k3 ~= nil, k3 ~= k1 and k3 ~= k2, next(t, k3) == nil";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {true, true, true});
  }

  @Test
  public void test_next_with_an_unknown_key_is_still_an_error() throws Exception {
    String program = "local t = {a=1}\n" //
        + "local ok, err = pcall(next, t, 'nope')\n" //
        + "return ok, err";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {false, "invalid key to 'next'"});
  }

  @Test
  public void test_modify_existing_fields_during_pairs() throws Exception {
    String program = "local t = {a=1, b=2, c=3}\n" //
        + "for k, v in pairs(t) do t[k] = v * 10 end\n" //
        + "return t.a + t.b + t.c";
    Object[] actual = run(program);
    assertThat(actual).isEqualTo(new Object[] {60L});
  }
}
