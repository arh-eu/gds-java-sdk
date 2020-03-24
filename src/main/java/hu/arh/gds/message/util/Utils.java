package hu.arh.gds.message.util;

import java.nio.charset.StandardCharsets;

public class Utils {

    public static String stringToUTF8Hex(String string) {
        byte[] bytes=string.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb=new StringBuilder(2*bytes.length);
        for (byte bb: bytes) {
            sb.append(String.format("%1$02x", 0xff&bb));
        }
        return sb.toString();
    }
}
