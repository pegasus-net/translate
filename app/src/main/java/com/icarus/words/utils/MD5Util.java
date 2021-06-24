package com.icarus.words.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String encode(String string, boolean upperCase) {
        StringBuilder encode = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] arr = digest.digest(string.getBytes(StandardCharsets.UTF_8));
            for (byte b : arr) {
                String s = Integer.toHexString(b & 0xFF);
                if (s.length() < 2) {
                    encode.append("0");
                }
                encode.append(s);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (upperCase) {
            return encode.toString().toUpperCase();
        } else {
            return encode.toString().toLowerCase();
        }
    }

    public static String encode(String string) {
        return encode(string, true);
    }
}
