package nl.sedit.encoders;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to encode en decode BCD numbers
 */
public class BCD {
    private BCD () {}

    private static Pattern BCD_PATTERN = Pattern.compile("[0-9]+");

    /**
     * Encode string with numbers (decimal) to BCD encoded bytes (big endian)
     * @param value Number that needs to be converted
     * @return BCD encoded number
     * @throws IllegalArgumentException if input is not a number
     */
    public static byte[] encode(String value) {
        if (!BCD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Can only encode numerical strings");
        }

        final byte[] bcd = new byte[(value.length() + 1) / 2];
        int i, j;
        if (value.length() % 2 == 1) {
            bcd[0] = (byte) (value.codePointAt(0) & 0xf);
            i = 1;
            j = 1;
        } else {
            i = 0;
            j = 0;
        }
        for ( ; i < bcd.length; i++, j+= 2) {
            bcd[i] = (byte) ( ((value.codePointAt(j) & 0xf) << 4) | (value.codePointAt(j + 1) & 0xf) );
        }
        return bcd;
    }

    /**
     * Encode value to BCD encoded bytes (big endian)
     * @param value number
     * @return BCD encoded number
     * @throws IllegalArgumentException if input is negative
     */
    public static byte[] encode(BigInteger value) {
        if (value.signum() == -1) {
            throw new IllegalArgumentException("Only non-negative values are supported");
        }
        if (value.bitLength() > 63) {
            return encode(value.toString());
        } else {
            return encode(value.longValue());
        }
    }

    /**
     * Encode value to BCD encoded bytes (big endian) in a byte array of a specific length
     * @param value number
     * @param length length of the byte array
     * @return BCD encoded number
     * @throws IllegalArgumentException if input is negative or does not fit in byte array
     */
    public static byte[] encode(BigInteger value, int length) {
        if (value.signum() == -1) {
            throw new IllegalArgumentException("Only non-negative values are supported");
        } else if (value.bitLength() > length * 8) {
            throw new IllegalArgumentException("Value does not fit in byte array of length" + length);
        }
        if (value.bitLength() > 63) {
            return encode(String.format("%0" + (length * 2) + "d", value));
        } else {
            return encode(value.longValue(), length);
        }
    }

    /**
     * Encode value to BCD encoded bytes (big endian) in a byte array of a specific length
     * @param value number
     * @param length length of the byte array
     * @return BCD encoded number
     * @throws IllegalArgumentException if input is negative or does not fit in byte array
     */
    public static byte[] encode(long value, int length) {
        if (value < 0) {
            throw new IllegalArgumentException("Only non-negative values are supported");
        } else if (value == 0) {
            return new byte[length];
        }
        final byte[] bcd = new byte[length];

        for (int i = bcd.length - 1; i >= 0; i--) {
            int b = (int) (value % 10);
            value /= 10;
            b |= (value % 10) << 4;
            value /= 10;
            bcd[i] = (byte) b;
        }
        if (value != 0) {
            throw new IllegalArgumentException("Value does not fit in byte array of length " + length);
        }

        return bcd;
    }

    /**
     * Encode value to BCD encoded bytes (big endian)
     * @param value number
     * @return BCD encoded number
     * @throws IllegalArgumentException if input is negative
     */
    public static byte[] encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Only non-negative values are supported");
        } else if (value == 0) {
            return new byte[1];
        }
        final int length = (int) Math.log10(value) + 1;
        return encode(value, (length + 1) / 2);
    }

    /**
     * Decodes BCD encoded bytes to BigInteger
     * @param bcd BCD encoded bytes
     * @return encoded number
     * @throws IllegalArgumentException if an illegal byte is detected
     */
    public static BigInteger decode(byte[] bcd) {
        BigInteger value = BigInteger.ZERO;
        for (int i =  0; i < bcd.length; i++) {
            final int high = ((int) bcd[i] & 0xff) >> 4;
            final int low = (int) bcd[i] & 0xf;

            if (high > 10 || low > 10)
                throw new IllegalArgumentException(String.format("Illegal byte %x%x at %d", high, low, i));

            value = value.multiply((BigInteger.TEN)).add(BigInteger.valueOf(high));
            value = value.multiply((BigInteger.TEN)).add(BigInteger.valueOf(low));
        }

        return value;
    }

    /**
     * Decodes BCD encoded bytes directly to a decimal string
     * @param bcd BCD encoded bytes
     * @return encoded number as String
     * @throws IllegalArgumentException if an illegal byte is detected
     */
    public static String decodeAsString(byte[] bcd, boolean stripLeadingZero) {
        final StringBuilder buf = new StringBuilder(bcd.length * 2);
        for (int i = 0; i < bcd.length; i++) {
            final int high = ((int) bcd[i] & 0xff) >> 4;
            final int low = (int) bcd[i] & 0xf;

            if (high > 10 || low > 10)
                throw new IllegalArgumentException(String.format("Illegal byte %x%x at %d", high, low, i));
            buf.append((char) (0x30 | high));
            buf.append((char) (0x30 | low));
        }
        return stripLeadingZero && buf.charAt(0) == '0' ? buf.substring(1) : buf.toString();
    }
}
