package nl.sedit.encoders;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BCD {
    private BCD () {}

    private static Pattern BCD_PATTERN = Pattern.compile("0*([1-9][0-9]*|0)");

    public static byte[] encode(String numbers) {
        final Matcher matcher = BCD_PATTERN.matcher(numbers);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Can only encode numerical strings");
        }

        final String value = matcher.group(1);
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

    public static byte[] encode(BigInteger value) {
        if (value.signum() == -1) {
            throw new IllegalArgumentException("Only non-negative values are supported");
        }
        if (value.bitLength() > 63)
            return encode(value.toString());
        else
            return encode(value.longValue());
    }

    public static byte[] encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Only non-negative values are supported");
        }
        if (value == 0) {
            return new byte[] { 0 };
        }
        final int length = (int) Math.log10(value) + 1;
        final byte[] bcd = new byte[(length + 1) / 2];

        for (int i = bcd.length - 1; i >= 0; i--) {
            int b = (int) (value % 10);
            value /= 10;
            b |= (value % 10) << 4;
            value /= 10;
            bcd[i] = (byte) b;
        }
        assert(value == 0);

        return bcd;
    }

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

    public static String decodeAsString(byte[] bcd) {
        final StringBuilder buf = new StringBuilder(bcd.length * 2);
        for (int i = 0; i < bcd.length; i++) {
            final int high = ((int) bcd[i] & 0xff) >> 4;
            final int low = (int) bcd[i] & 0xf;

            if (high > 10 || low > 10)
                throw new IllegalArgumentException(String.format("Illegal byte %x%x at %d", high, low, i));
            buf.append((char) (0x30 | high));
            buf.append((char) (0x30 | low));
        }
        return buf.charAt(0) == '0' ? buf.substring(1) : buf.toString();
    }
}
