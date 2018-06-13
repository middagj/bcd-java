package nl.sedit.encoders;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class BCDTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldEncodeStringEven() {
        assertArrayEquals(new byte[] { 0x31 }, BCD.encode("31"));
    }

    @Test
    public void shouldEncodeStringOdd() {
        assertArrayEquals(new byte[] { 0x02, 0x31 }, BCD.encode("231"));
    }

    @Test
    public void shouldEncodeStringZero() {
        assertArrayEquals(new byte[] { 0x00 }, BCD.encode("0"));
    }

    @Test
    public void shouldEncodeStringAndStripLeadingZero() {
        assertArrayEquals(new byte[] { 0x31 }, BCD.encode("031"));
    }

    @Test
    public void shouldEncodeLongEven() {
        assertArrayEquals(new byte[] { 0x31 }, BCD.encode(31));
    }

    @Test
    public void shouldEncodeLongOdd() {
        assertArrayEquals(new byte[] { 0x02, 0x31 }, BCD.encode(231));
    }

    @Test
    public void shouldEncodeLongZero() {
        assertArrayEquals(new byte[] { 0x00 }, BCD.encode(0));
    }

    @Test
    public void encodeLongShouldThrowExceptionForNegative() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Only non-negative values are supported");
        BCD.encode(-1);
    }

    @Test
    public void shouldEncodeBigIntegerSmall() {
        assertArrayEquals(new byte[] { 0x09, 0x22, 0x33, 0x72, 0x03, 0x68, 0x54, 0x77, 0x58, 0x07 },
            BCD.encode(BigInteger.valueOf(Long.MAX_VALUE)));
    }

    @Test
    public void shouldEncodeBigIntegerBig() {
        assertArrayEquals(new byte[] { 0x09, 0x22, 0x33, 0x72, 0x03, 0x68, 0x54, 0x77, 0x58, 0x08 },
            BCD.encode(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)));
    }

    @Test
    public void shouldEncodeBigIntegerZero() {
        assertArrayEquals(new byte[] { 0x00 }, BCD.encode(BigInteger.ZERO));
    }

    @Test
    public void encodeBigIntegerShouldThrowExceptionForNegative() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Only non-negative values are supported");
        BCD.encode(BigInteger.ONE.negate());
    }

    @Test
    public void shouldDecodeEven() {
        assertEquals(31, BCD.decode(new byte[] { 0x31}).intValue());
    }

    @Test
    public void shouldDecodeOdd() {
        assertEquals(231, BCD.decode(new byte[] { 0x02, 0x31}).intValue());
    }

    @Test
    public void shouldDecodeZero() {
        assertEquals(0, BCD.decode(new byte[] { 0x0 }).intValue());
    }

    @Test
    public void decodeShouldThrowExceptionOnHighNibble() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal byte d0 at 0");
        BCD.decode(new byte[] { -48 });
    }

    @Test
    public void decodeShouldThrowExceptionOnLowNibble() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal byte 0d at 0");
        BCD.decode(new byte[] { 0x0d });
    }

    @Test
    public void shouldDecodeAsStringEven() {
        assertEquals("31", BCD.decodeAsString(new byte[] { 0x31}));
    }

    @Test
    public void shouldDecodeAsStringOdd() {
        assertEquals("231", BCD.decodeAsString(new byte[] { 0x02, 0x31}));
    }

    @Test
    public void shouldDecodeAsStringZero() {
        assertEquals("0", BCD.decodeAsString(new byte[] { 0x00 }));
    }

    @Test
    public void decodeAsStringShouldThrowExceptionOnHighNibble() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal byte d0 at 0");
        BCD.decodeAsString(new byte[] { -48 });
    }

    @Test
    public void decodeAsStringShouldThrowExceptionOnLowNibble() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal byte 0d at 0");
        BCD.decodeAsString(new byte[] { 0x0d });
    }
}
