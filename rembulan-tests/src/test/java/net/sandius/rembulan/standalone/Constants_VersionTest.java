package net.sandius.rembulan.standalone;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * The console reports the version of the build (#59), not a constant that gets out of date.
 */
public class Constants_VersionTest {

  @Test
  public void test_version_is_the_build_s() {
    // the test task passes the project version
    String expected = System.getProperty("rembulan.version");
    assertThat(expected).isNotEmpty();
    assertThat(Constants.VERSION).isEqualTo(expected);
  }

}
