package com.icarus.words.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import a.icarus.utils.Recycle;

@SuppressWarnings("unused")
public class StreamUtil {
    public static void copy(InputStream is, OutputStream os) {
        copy(is, os, new byte[1024]);
    }

    public static boolean copy(InputStream is, OutputStream os, byte[] buffer) {
        int len;
        try {
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            Recycle.close(is);
            Recycle.close(os);
        }
        return true;
    }

    public static String toString(InputStream is) {
        return toString(is, new byte[1024]);
    }

    public static String toString(InputStream is, byte[] buffer) {
        int len;
        StringBuilder builder = new StringBuilder();
        try {
            while ((len = is.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, len));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            Recycle.close(is);
        }
        return builder.toString();
    }
}
