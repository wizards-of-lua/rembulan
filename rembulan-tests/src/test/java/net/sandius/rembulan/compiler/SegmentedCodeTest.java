package net.sandius.rembulan.compiler;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.lib.StandardLibrary;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.testenv.TestBase;

/**
 * A function larger than the node size limit is split into segments, one method each. Jumps
 * between segments and resumption points within them are encoded in an int, which must not
 * overflow for functions with many segments or with many blocks in a segment.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SegmentedCodeTest extends TestBase {

  private Object[] run(String program, int nodeSizeLimit) throws Exception {
    StateContext state = StateContexts.newDefaultInstance();
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);
    CompilerSettings settings = CompilerSettings.defaultSettings().withNodeSizeLimit(nodeSizeLimit);
    LuaFunction main = CompilerChunkLoader.of(settings, "SegmentedCodeTest")
        .loadTextChunk(new Variable(env), "test", program);
    return DirectCallExecutor.newExecutor().call(state, main);
  }

  @Test(timeout = 10000)
  public void test_more_than_256_segments() throws Exception {
    // with 10 nodes per segment, this table literal takes far more than 256 segments
    int n = 1000;
    StringBuilder program = new StringBuilder("local t = {\n");
    for (int i = 0; i < n; i++) {
      program.append("  {x=").append(i).append(", name=\"r").append(i).append("\"},\n");
    }
    program.append("}\n");
    program.append("local sum = 0\n");
    program.append("for i = 1, #t do sum = sum + t[i].x end\n");
    program.append("return #t, sum, t[" + n + "].name\n");

    Object[] actual = run(program.toString(), 10);

    assertThat(actual).isEqualTo(new Object[] {(long) n, (long) n * (n - 1) / 2, "r" + (n - 1)});
  }

  @Test(timeout = 10000)
  public void test_jump_to_a_block_beyond_the_256th_of_its_segment() throws Exception {
    // the ifs make hundreds of small blocks, so the loop head lies far into its segment;
    // the loop body is longer than a segment, so its back edge jumps there from another segment
    StringBuilder program = new StringBuilder("local a, b, s = true, 0, 0\n");
    for (int i = 0; i < 300; i++) {
      program.append("if a then b = b + 1 end\n");
    }
    program.append("for i = 1, 3 do\n");
    for (int i = 0; i < 1000; i++) {
      program.append("  s = s + i\n");
    }
    program.append("end\n");
    program.append("return b, s\n");

    Object[] actual = run(program.toString(), CompilerSettings.DEFAULT_NODE_SIZE_LIMIT);

    assertThat(actual).isEqualTo(new Object[] {300L, 6000L});
  }

}
