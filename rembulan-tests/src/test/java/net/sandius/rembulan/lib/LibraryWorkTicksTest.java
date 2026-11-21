package net.sandius.rembulan.lib;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.SchedulingContext;
import net.sandius.rembulan.testenv.TestBase;

/**
 * Library functions that do long work in Java (pattern matching, building long strings, sorting)
 * charge its ticks with {@code registerTicksUnpausable} while they run, so that a scheduler can stop
 * them; before, one such call could hold the thread for minutes.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LibraryWorkTicksTest extends TestBase {

  /** Counts the ticks; throws when the unpausable ticks of one call exceed the limit. */
  static class Scheduler implements SchedulingContext {
    final long limit;
    long ticks;
    long unpausableTicks;
    long unpausableOfThisCall;

    Scheduler(long limit) {
      this.limit = limit;
    }

    @Override
    public void registerTicks(int ticks) {
      this.ticks += ticks;
      unpausableOfThisCall = 0;
    }

    @Override
    public void registerTicksUnpausable(int ticks) {
      unpausableTicks += ticks;
      unpausableOfThisCall += ticks;
      if (unpausableOfThisCall > limit) {
        throw new LuaRuntimeException("library call too long");
      }
    }

    @Override
    public boolean shouldPause() {
      return false;
    }
  }

  /** Runs the program; its arguments ({@code ...}) are strings of {@code n} times "a". */
  private Object[] run(SchedulingContext scheduler, String program, int... n) throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    RuntimeEnvironment runtimeEnvironment = RuntimeEnvironments.system();
    Table env = StandardLibrary.in(runtimeEnvironment).installInto(state);
    IoLib.installInto(state, env, runtimeEnvironment);
    LuaFunction main = CompilerChunkLoader.of("LibraryWorkTicksTest")
        .loadTextChunk(new Variable(env), "test", program);
    Object[] args = new Object[n.length];
    for (int i = 0; i < n.length; i++) {
      args[i] = ByteString.of(new String(new char[n[i]]).replace('\0', 'a'));
    }
    return DirectCallExecutor.newExecutor(() -> scheduler).call(state, main, args);
  }

  private static final String STOPPED = "library call too long";

  // --- patterns

  @Test(timeout = 10000)
  public void test_backtracking_find_is_stopped() throws Exception {
    // about 3*10^8 steps, a minute or more without the charges
    Object[] actual = run(new Scheduler(50000),
        "return pcall(string.find, string.rep('a', 300), '.-.-.-.-b')");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test(timeout = 10000)
  public void test_backtracking_match_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(50000),
        "return pcall(string.match, string.rep('a', 300), '(.-)(.-)(.-)(.-)b')");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test(timeout = 10000)
  public void test_backtracking_gmatch_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(50000),
        "local f = string.gmatch(string.rep('a', 300), '.-.-.-.-b') return pcall(f)");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test(timeout = 10000)
  public void test_backtracking_gsub_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(50000),
        "return pcall(string.gsub, string.rep('a', 300), '.-.-.-.-b', '')");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test
  public void test_stopped_call_can_be_caught_and_the_code_goes_on() throws Exception {
    Object[] actual = run(new Scheduler(50000),
        "local ok = pcall(string.find, string.rep('a', 300), '.-.-.-.-b')\n"
            + "return ok, string.find('abc', 'b')");
    assertThat(actual).isEqualTo(new Object[] {false, 2L, 2L});
  }

  @Test
  public void test_each_call_is_charged_on_its_own() throws Exception {
    // two calls of 30,000 ticks each in one piece of compiled code (no branch between them, so no
    // ordinary registerTicks): each call starts its count anew, so neither exceeds 50,000
    Scheduler scheduler = new Scheduler(50000);
    Object[] actual = run(scheduler, "local s = ... local a = s:find('b') local b = s:find('b') return a, b",
        300000);
    assertThat(actual).isEqualTo(new Object[] {null, null});
    assertThat(scheduler.unpausableTicks).isBetween(59900L, 60000L);
  }

  @Test
  public void test_a_stopped_call_does_not_stop_the_next() throws Exception {
    Object[] actual = run(new Scheduler(50000),
        "local ok = pcall(string.find, string.rep('a', 300), '.-.-.-.-b')\n"
            + "local s, n = string.gsub(string.rep('ab', 10000), 'a', 'x')\n"
            + "return ok, n");
    assertThat(actual).isEqualTo(new Object[] {false, 10000L});
  }

  @Test
  public void test_short_match_charges_nothing() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    Object[] actual = run(scheduler, "local i, j = string.find('hello world', 'o w')\n"
        + "return i, j, string.gsub('hello', 'l', 'L')");
    assertThat(actual).isEqualTo(new Object[] {5L, 7L, "heLLo", 2L});
    assertThat(scheduler.unpausableTicks).isEqualTo(0);
  }

  @Test
  public void test_long_find_charges_its_steps() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    // 100,000 start positions of 2 steps each, 20 steps per tick
    Object[] actual = run(scheduler, "return string.find(..., 'b')", 100000);
    assertThat(actual).isEqualTo(new Object[] {null});
    assertThat(scheduler.unpausableTicks).isBetween(9950L, 10000L);
  }

  @Test
  public void test_gsub_charges_matches_and_output() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    Object[] actual = run(scheduler, "local s, n = string.gsub(..., 'a', 'x') return #s, n", 100000);
    assertThat(actual).isEqualTo(new Object[] {100000L, 100000L});
    // per match: 2 steps for the position, 1 for the item, 6 for the result; an append of 1 byte
    assertThat(scheduler.unpausableTicks).isBetween(53000L, 54000L);
  }

  @Test
  public void test_gsub_with_a_function_charges_per_call() throws Exception {
    // the Lua function's own ticks start a new call: its unpausable count starts again
    Object[] actual = run(new Scheduler(1000),
        "local s, n = string.gsub(..., 'a', function() return 'x' end) return #s, n", 100000);
    assertThat(actual).isEqualTo(new Object[] {100000L, 100000L});
  }

  @Test
  public void test_results_are_unchanged() throws Exception {
    Object[] actual = run(new Scheduler(Long.MAX_VALUE),
        "local s = string.rep('ab(cd)', 10000)\n"
            + "local n = 0 for w in s:gmatch('%b()') do n = n + 1 end\n"
            + "local i, j, c = s:find('(cd)%)ab', 50000)\n"
            + "return n, i, j, c, (s:gsub('(%a)(%a)', '%2%1', 3)):sub(1, 12),"
            + " (string.rep('xy', 3, ',')), s:match('^(a)(b)')");
    assertThat(actual).isEqualTo(new Object[] {10000L, 50002L, 50006L, "cd", "ba(dc)ba(cd)", "xy,xy,xy", "a", "b"});
  }

  // --- string.rep

  @Test(timeout = 10000)
  public void test_rep_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(50000), "return pcall(string.rep, 'x', 100000000)");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test
  public void test_rep_charges_its_bytes() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    Object[] actual = run(scheduler, "return #string.rep(..., 1000)", 1000);
    assertThat(actual).isEqualTo(new Object[] {1000000L});
    // 1,000,000 bytes, 400 per tick, charged at once before the work
    assertThat(scheduler.unpausableTicks).isEqualTo(2500L);
  }

  @Test(timeout = 10000)
  public void test_rep_of_a_megabyte_is_quick_and_cheap() throws Exception {
    // a common idiom: one-byte pieces used to cost 30 times as much as their bytes
    Scheduler scheduler = new Scheduler(50000);
    Object[] actual = run(scheduler, "return #string.rep('x', 1048576), #string.rep('x', 16000000)");
    assertThat(actual).isEqualTo(new Object[] {1048576L, 16000000L});
  }

  @Test
  public void test_rep_results() throws Exception {
    Object[] actual = run(new Scheduler(Long.MAX_VALUE),
        "return string.rep('ab', 3), string.rep('ab', 3, ','), string.rep('', 3, ','),"
            + " string.rep('ab', 1, ','), string.rep('ab', 0, ','), string.rep('ab', -1),"
            + " string.rep('x', 5, ''), #string.rep('abc', 10001, '--')");
    assertThat(actual).isEqualTo(new Object[] {
        "ababab", "ab,ab,ab", ",,", "ab", "", "", "xxxxx", 3L * 10001 + 2L * 10000});
  }

  @Test(timeout = 10000)
  public void test_rep_too_large() throws Exception {
    Object[] actual = run(new Scheduler(Long.MAX_VALUE),
        "local ok, err = pcall(string.rep, 'ab', 0x40000000)\n"
            + "return ok, err, pcall(string.rep, 'x', 0x7fffffff, 'y')");
    assertThat(actual).isEqualTo(new Object[] {
        false, "resulting string too large", false, "resulting string too large"});
  }

  @Test(timeout = 10000)
  public void test_rep_of_empty_strings_is_quick() throws Exception {
    Object[] actual = run(new Scheduler(50000), "return string.rep('', 0x7fffffff)");
    assertThat(actual).isEqualTo(new Object[] {""});
  }

  // --- table.concat, table.sort

  @Test(timeout = 10000)
  public void test_concat_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(1000),
        "local t = {} for i = 1, 10000 do t[i] = 'abc' end return pcall(table.concat, t, ',')");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test
  public void test_concat_with_index_metamethod_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(1000),
        "local k = string.rep('z', 1000)\n"
            + "local t = setmetatable({}, {__index = function() return k end})\n"
            + "local raw = {} for i = 1, 10000 do raw[i] = k end\n"
            + "local v = setmetatable({}, {__index = raw})\n"
            + "return #table.concat(t, '', 1, 1000), pcall(table.concat, v, '', 1, 10000)");
    // the __index function runs Lua code between the pieces: each piece is a call of its own;
    // the __index table doesn't, so the whole concat is one call
    assertThat(actual).isEqualTo(new Object[] {1000000L, false, STOPPED});
  }

  @Test
  public void test_sort_charges_its_steps() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    Object[] actual = run(scheduler,
        "local t = {} for i = 1, 10000 do t[i] = (i * 7919) % 10007 end\n"
            + "table.sort(t) return t[1], t[10000]");
    assertThat(actual).isEqualTo(new Object[] {1L, 10006L});
    // about 2 n log2(n) sift steps, one tick each
    assertThat(scheduler.unpausableTicks).isBetween(100000L, 300000L);
  }

  @Test(timeout = 10000)
  public void test_sort_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(50000),
        "local t = {} for i = 1, 100000 do t[i] = tostring(i) end return pcall(table.sort, t)");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  // --- the .. operator

  @Test
  public void test_concat_operator_is_stopped() throws Exception {
    // 2,000,000 bytes: 5000 ticks, charged before the copy
    Object[] actual = run(new Scheduler(1000),
        "local a = ... return pcall(function() return a .. a end)", 1000000);
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test
  public void test_concat_operator_charges_its_bytes() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    Object[] actual = run(scheduler, "local a = ... return #(a .. a), #('x' .. 'y')", 200000);
    assertThat(actual).isEqualTo(new Object[] {400000L, 2L});
    assertThat(scheduler.unpausableTicks).isEqualTo(1000L);
  }

  // --- table.move, table.insert, table.remove

  @Test(timeout = 10000)
  public void test_table_move_is_stopped() throws Exception {
    // a billion moves, minutes without the charges
    Object[] actual = run(new Scheduler(50000), "return pcall(table.move, {1, 2, 3}, 1, 1000000000, 2)");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test(timeout = 10000)
  public void test_table_move_with_index_metamethod_is_stopped() throws Exception {
    Object[] actual = run(new Scheduler(50000),
        "local t = setmetatable({}, {__index = {}}) return pcall(table.move, t, 1, 1000000000, 2)");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED});
  }

  @Test
  public void test_table_insert_and_remove_charge_the_shift() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    Object[] actual = run(scheduler,
        "local t = {} for i = 1, 100000 do t[i] = i end\n"
            + "table.insert(t, 1, 0) local r = table.remove(t, 1)\n"
            + "local m = table.move(t, 1, 100000, 1, {})\n"
            + "return r, t[1], t[100000], #t, m[100000]");
    assertThat(actual).isEqualTo(new Object[] {0L, 1L, 100000L, 100000L, 100000L});
    // 3 times 100,000 elements, 4 per tick
    assertThat(scheduler.unpausableTicks).isBetween(74900L, 75000L);
  }

  // --- io

  private Path file(String content) throws Exception {
    Path dir = Files.createTempDirectory("LibraryWorkTicksTest");
    Path file = dir.resolve("data.txt");
    Files.write(file, content.getBytes(StandardCharsets.ISO_8859_1));
    file.toFile().deleteOnExit();
    dir.toFile().deleteOnExit();
    return file;
  }

  private static String repeat(String s, int n) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) {
      b.append(s);
    }
    return b.toString();
  }

  private static String lua(Path path) {
    return "'" + path.toString().replace("\\", "/") + "'";
  }

  @Test
  public void test_read_is_stopped() throws Exception {
    // 2,000,000 bytes: 13,333 ticks
    Path f = file(repeat("0123456789", 200000));
    Object[] actual = run(new Scheduler(1000),
        "local f = io.open(" + lua(f) + ")\n"
            + "local ok1, err1 = pcall(f.read, f, 'a')\n"
            + "f:seek('set') local ok2, err2 = pcall(f.read, f, 1500000)\n"
            + "f:close() return ok1, err1, ok2, err2");
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED, false, STOPPED});
  }

  @Test
  public void test_read_charges_its_bytes() throws Exception {
    Scheduler scheduler = new Scheduler(Long.MAX_VALUE);
    Path f = file(repeat("0123456789", 30000));
    Object[] actual = run(scheduler,
        "local f = io.open(" + lua(f) + ") local s = f:read('a') f:close() return #s");
    assertThat(actual).isEqualTo(new Object[] {300000L});
    // 150 bytes per tick
    assertThat(scheduler.unpausableTicks).isBetween(1950L, 2000L);
  }

  @Test
  public void test_write_is_stopped_before_it_writes() throws Exception {
    // 1,000,000 bytes: 5000 ticks, charged before the write
    Path f = file("");
    Object[] actual = run(new Scheduler(1000),
        "local s = ... local f = io.open(" + lua(f) + ", 'w')\n"
            + "local ok, err = pcall(f.write, f, s) f:close()\n"
            + "local g = io.open(" + lua(f) + ") local size = g:seek('end') g:close()\n"
            + "return ok, err, size", 1000000);
    assertThat(actual).isEqualTo(new Object[] {false, STOPPED, 0L});
  }

  // --- schedulers without registerTicksUnpausable

  @Test
  public void test_default_registers_ordinary_ticks() throws Exception {
    long[] ticks = new long[1];
    SchedulingContext plain = new SchedulingContext() {
      @Override
      public void registerTicks(int t) {
        ticks[0] += t;
      }

      @Override
      public boolean shouldPause() {
        return false;
      }
    };
    Object[] actual = run(plain, "return #string.rep(string.rep('x', 1000), 1000)");
    assertThat(actual).isEqualTo(new Object[] {1000000L});
    assertThat(ticks[0]).isGreaterThan(2500L);
  }
}
