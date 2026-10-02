package net.sandius.rembulan.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import net.sandius.rembulan.ByteString;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PackedConstantsTest extends Assertions {

  private static ByteString bytes(int... values) {
    byte[] result = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = (byte) values[i];
    }
    return ByteString.copyOf(result);
  }

  private static ByteString repeat(int value, int count) {
    byte[] result = new byte[count];
    Arrays.fill(result, (byte) value);
    return ByteString.copyOf(result);
  }

  // throws if a chunk does not fit into a string constant of a class file
  private static void assertFitsIntoClassFile(List<String> chunks) throws IOException {
    for (String chunk : chunks) {
      new DataOutputStream(new ByteArrayOutputStream()).writeUTF(chunk);
    }
  }

  private static Object[] roundTrip(List<Object> constants) throws IOException {
    List<String> chunks = PackedConstants.pack(constants);
    assertFitsIntoClassFile(chunks);
    return PackedConstants.unpack(chunks.toArray(new String[0]));
  }

  @Test
  public void test_integers() throws Exception {
    List<Object> constants = Arrays.<Object>asList(0L, 1L, -1L, 65535L, 65536L,
        Long.MIN_VALUE, Long.MAX_VALUE, 0x0001000200030004L);
    assertThat(roundTrip(constants)).containsExactly(constants.toArray());
  }

  @Test
  public void test_floats() throws Exception {
    List<Object> constants = Arrays.<Object>asList(0.0, -0.0, 0.5, -1e300, Double.MIN_VALUE,
        Double.MAX_VALUE, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    Object[] actual = roundTrip(constants);
    assertThat(actual).containsExactly(constants.toArray());
    assertThat(Double.doubleToRawLongBits((Double) actual[1]))
        .isEqualTo(Double.doubleToRawLongBits(-0.0));
  }

  @Test
  public void test_strings() throws Exception {
    int[] all = new int[256];
    for (int i = 0; i < all.length; i++) {
      all[i] = i;
    }
    List<Object> constants = Arrays.<Object>asList(ByteString.empty(), ByteString.of("abc"),
        bytes(0), bytes(all));
    assertThat(roundTrip(constants)).containsExactly(constants.toArray());
  }

  @Test
  public void test_integer_and_string_are_different_constants() throws Exception {
    List<Object> constants = Arrays.<Object>asList(1L, ByteString.of("1"), 1.0);
    Object[] actual = roundTrip(constants);
    assertThat(actual).containsExactly(constants.toArray());
    assertThat(actual[0]).isInstanceOf(Long.class);
    assertThat(actual[2]).isInstanceOf(Double.class);
  }

  @Test
  public void test_string_longer_than_a_chunk() throws Exception {
    // zero bytes take 2 bytes each in a class file, so this needs at least 4 chunks
    List<Object> constants = Arrays.<Object>asList(1L, repeat(0, 100000), 2L, repeat(255, 70000));
    List<String> chunks = PackedConstants.pack(constants);
    assertThat(chunks.size()).isGreaterThanOrEqualTo(4);
    assertThat(roundTrip(constants)).containsExactly(constants.toArray());
  }

  @Test
  public void test_many_constants() throws Exception {
    List<Object> constants = new ArrayList<>();
    for (long i = 0; i < 20000; i++) {
      constants.add(i * 1000003);
      constants.add(i + 0.25);
      constants.add(ByteString.of("s" + i));
    }
    List<String> chunks = PackedConstants.pack(constants);
    assertThat(chunks.size()).isGreaterThan(1);
    // not containsExactly, which takes minutes for that many elements
    assertThat(Arrays.asList(roundTrip(constants))).isEqualTo(constants);
  }

  @Test
  public void test_no_constants() throws Exception {
    assertThat(PackedConstants.pack(new ArrayList<Object>())).isEmpty();
    assertThat(PackedConstants.unpack(new String[0])).isEmpty();
  }

}
