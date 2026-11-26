package net.sandius.rembulan.lib;

import org.junit.Test;

import net.sandius.rembulan.testenv.TestBase;

/**
 * {@code package.cpath} is a string, as the Lua manual defines it; empty, as Rembulan has no C
 * loader.
 */
public class ModuleLib_CpathTest extends TestBase {

  @Test
  public void test_cpath_is_an_empty_string() throws Exception {
    Object[] actual = run("return type(package.cpath), package.cpath, package.cpath .. ';x'");

    assertThat(actual).isEqualTo(new Object[] {"string", "", ";x"});
  }

}
