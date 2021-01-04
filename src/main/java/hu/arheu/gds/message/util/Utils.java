package hu.arheu.gds.message.util;

import java.nio.charset.StandardCharsets;

public class Utils {

    /**
     * Converts a given string to hex formatted string by converting every character in it to the proper byte value.
     *
     * @param string the string to be converted
     * @return the converted, hex representation of the string
     */
    public static String stringToUTF8Hex(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte bb : bytes) {
            sb.append(String.format("%1$02x", 0xFF & bb));
        }
        return sb.toString();
    }
}
