package net.sandius.rembulan.lib;

import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.testenv.TestBase;

/**
 * {@code string.format} with {@code %f}, {@code %e} and {@code %g} must give what Lua 5.3 (C's
 * printf) gives, whatever the locale of the JVM.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringLib_FormatFloatTest extends TestBase {

  private Locale defaultLocale;

  @Before
  public void useGermanLocale() {
    // A locale with a decimal comma, like the one of the server where the bug was found.
    defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.GERMANY);
  }

  @After
  public void restoreLocale() {
    Locale.setDefault(defaultLocale);
  }

  private void assertFormat(String format, String value, String expected) throws Exception {
    Object[] actual = run("return string.format('" + format + "', " + value + ")");
    assertThat(actual).isEqualTo(new Object[] {expected});
  }

  @Test
  public void test_f_uses_a_decimal_point_in_any_locale() throws Exception {
    assertFormat("%f", "1.5", "1.500000");
    assertFormat("%.2f", "1.5", "1.50");
    assertFormat("%10.3f", "-3.14159", "    -3.142");
  }

  @Test
  public void test_e_uses_a_decimal_point_in_any_locale() throws Exception {
    assertFormat("%e", "1.5", "1.500000e+00");
    assertFormat("%.3E", "12345.678", "1.235E+04");
  }

  @Test
  public void test_f_precision_zero() throws Exception {
    assertFormat("%.0f", "2.5", "2");
    assertFormat("%.0f", "31.48", "31");
    assertFormat("%#.0f", "3", "3.");
    assertFormat("%.0e", "12345", "1e+04");
  }

  @Test
  public void test_rounds_the_exact_value_ties_to_even() throws Exception {
    // As C rounds the exact binary value: 0.15 is 0.1499999..., 2.675 is 2.67499999...
    assertFormat("%.1f", "0.15", "0.1");
    assertFormat("%.2f", "2.675", "2.67");
    // exact ties go to the even digit
    assertFormat("%.0f", "0.5", "0");
    assertFormat("%.0f", "1.5", "2");
    assertFormat("%.1e", "0.125", "1.2e-01");
    assertFormat("%f", "1e20", "100000000000000000000.000000");
    assertFormat("%08.2f", "-1.5", "-0001.50");
    assertFormat("%f", "-0.0", "-0.000000");
  }

  @Test
  public void test_g_drops_trailing_zeros() throws Exception {
    assertFormat("%g", "1.5", "1.5");
    assertFormat("%g", "1", "1");
    assertFormat("%.14g", "1", "1");
    assertFormat("%.14g", "0.1", "0.1");
    assertFormat("%g", "100000", "100000");
    assertFormat("%g", "1e20", "1e+20");
    assertFormat("%g", "1234567", "1.23457e+06");
    assertFormat("%g", "0.0001234", "0.0001234");
    assertFormat("%g", "0.00001234", "1.234e-05");
    assertFormat("%g", "0", "0");
    assertFormat("%g", "-2.5", "-2.5");
  }

  @Test
  public void test_g_precision() throws Exception {
    assertFormat("%.3g", "0.0001234", "0.000123");
    assertFormat("%.0g", "123", "1e+02");
    assertFormat("%.1g", "0.95", "0.9");
    assertFormat("%.2g", "99.5", "1e+02");
    assertFormat("%.3G", "1e-10", "1E-10");
  }

  @Test
  public void test_g_flags() throws Exception {
    assertFormat("%#g", "1", "1.00000");
    assertFormat("%#.3g", "100000", "1.00e+05");
    assertFormat("%+g", "1.5", "+1.5");
    assertFormat("% g", "1.5", " 1.5");
    assertFormat("%8g", "1.5", "     1.5");
    assertFormat("%-8g|", "1.5", "1.5     |");
    assertFormat("%08g", "-1.5", "-00001.5");
  }

  @Test
  public void test_g_in_a_german_locale() throws Exception {
    assertFormat("%.14g", "1.5", "1.5");
    assertFormat("%g", "1234567", "1.23457e+06");
  }
}
