package com.zkteco.zkfinger.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtil {
    /**
     * At server side, use ZipOutputStream to zip text to byte array, then convert
     * byte array to base64 string, so it can be trasnfered via http request.
     */
    public static String compressString(byte[] textBytes)
            throws IOException {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(rstBao);
        zos.write(textBytes);
        zos.flush();
        zos.close();

        byte[] bytes = rstBao.toByteArray();
        // In my solr project, I use org.apache.solr.co mmon.util.Base64.
        // return = org.apache.solr.common.util.Base64.byteArrayToBase64(bytes, 0,
        // bytes.length);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * When client receives the zipped base64 string, it first decode base64
     * String to byte array, then use ZipInputStream to revert the byte array to a
     * string.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static byte[] uncompressString(String zippedBase64Str)
            throws IOException {
        byte[] result;

        // In my solr project, I use org.apache.solr.common.util.Base64.
        // byte[] bytes =
        // org.apache.solr.common.util.Base64.base64ToByteArray(zippedBase64Str);
        byte[] bytes = Base64.decode(zippedBase64Str, Base64.DEFAULT);
//        String charset = "UTF-8"; // You should determine it based on response header.

        try (GZIPInputStream zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            byte[] buffer = new byte[1024];

            int len;
            while ((len = zi.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            zi.close();
            out.close();
            result = out.toByteArray();
        }
        return result;
    }
}
