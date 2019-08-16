package com.zkteco.zkfinger.util;


public class Base64Util {

    private static String base64Code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String getBinaryStrFromByteArr(byte[] bArr) {
        String result = "";
        for (byte b : bArr) {
            result += getBinaryStrFromByte(b);
        }
        return result;
    }

    public static String getBinaryStrFromByte(byte b) {
        boolean isFu = false;
        if (b < 0) {
            b = (byte) (128 + b);
            isFu = true;
        }
        String result = "";
        byte a = b;
        for (int i = 0; i < 8; i++) {
            result = (a % 2) + result;
            a = (byte) (a / 2);
        }
        if (isFu) {
            StringBuilder str = new StringBuilder(result);
            str.setCharAt(0, '1');

            result = str.toString();
        }
        return result;
    }

    public static int getIntFromBinaryStr(String str) {
        int result = 0;
        if (str.charAt(0) == '1') {
            result = result + 32;
        }
        if (str.charAt(1) == '1') {
            result = result + 16;
        }
        if (str.charAt(2) == '1') {
            result = result + 8;
        }
        if (str.charAt(3) == '1') {
            result = result + 4;
        }
        if (str.charAt(4) == '1') {
            result = result + 2;
        }
        if (str.charAt(5) == '1') {
            result = result + 1;
        }
        return result;
    }

    public static byte getByteFromStr(String str) {
        boolean isFu = false;
        byte result = 0;
        if (str.charAt(0) == '1') {
            isFu = true;
        }
        if (str.charAt(1) == '1') {
            result += 64;
        }
        if (str.charAt(2) == '1') {
            result += 32;
        }
        if (str.charAt(3) == '1') {
            result += 16;
        }
        if (str.charAt(4) == '1') {
            result += 8;
        }
        if (str.charAt(5) == '1') {
            result += 4;
        }
        if (str.charAt(6) == '1') {
            result += 2;
        }
        if (str.charAt(7) == '1') {
            result += 1;
        }
        if (isFu) {
            return (byte) (result - 128);
        }
        return result;
    }

    public static String getBinaryFromInt(int num) {
        int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0;
        for (a = 0; a < 2; a++) {
            for (b = 0; b < 2; b++) {
                for (c = 0; c < 2; c++) {
                    for (d = 0; d < 2; d++) {
                        for (e = 0; e < 2; e++) {
                            for (f = 0; f < 2; f++) {
                                if ((f * 1 + e * 2 + d * 4 + c * 8 + b * 16 + a * 32) == num) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append(a);
                                    stringBuilder.append(b);
                                    stringBuilder.append(c);
                                    stringBuilder.append(d);
                                    stringBuilder.append(e);
                                    stringBuilder.append(f);
                                    String str = stringBuilder.toString();
                                    return str;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String encode(byte[] binaryData) {
        String srcStr = getBinaryStrFromByteArr(binaryData);
        int length = 0, biaoji = 0;//biaoji 指的是加等号数//Referring to the equal number
        if (srcStr.length() % 3 == 0) {
            length = srcStr.length();
            biaoji = 0;
        } else if (srcStr.length() % 3 == 2) {
            length = srcStr.length() + 4;
            srcStr = srcStr + '0' + '0' + '0' + '0';
            biaoji = 2;
        } else if (srcStr.length() % 3 == 1) {
            length = srcStr.length() + 2;
            srcStr = srcStr + '0' + '0';
            biaoji = 1;
        }
        int[] intTmp = new int[length / 6];
        char[] charTmp = new char[length / 6];
        for (int i = 0; i < length / 6; i++) {
            intTmp[i] = getIntFromBinaryStr(srcStr.substring(6 * i, 6 * i + 6));
            charTmp[i] = base64Code.charAt(intTmp[i]);
        }
        String dest = String.valueOf(charTmp);
        while (biaoji > 0) {
            dest = dest + '=';
            biaoji--;
        }
        StringBuilder strTmp = new StringBuilder(dest);
        int i = 76;
        while (i < strTmp.length()) {
            strTmp.insert(i, "\r\n");
            i += 76;
        }
        dest = strTmp.toString();
        return dest;
    }

    public static byte[] decode(String srcStr) {
        int eqCounter = 0;//等号数量用来删0//The number of equal signs is used to delete 0
        if (srcStr.endsWith("==")) {
            eqCounter = 2;
        } else if (srcStr.endsWith("=")) {
            eqCounter = 1;
        }
        srcStr = srcStr.replaceAll("=", "");
        srcStr = srcStr.replaceAll("\r\n", "");

        int[] intTmp = new int[srcStr.length()];
        for (int i = 0; i < srcStr.length(); i++) {
            for (int j = 0; j < base64Code.length(); j++) {
                if (srcStr.charAt(i) == base64Code.charAt(j)) {
                    intTmp[i] = j;
                    break;
                }
            }
        }

        StringBuilder strTmp = new StringBuilder();
        for (int i = 0; i < intTmp.length; i++) {
            strTmp.append(getBinaryFromInt(intTmp[i]));
        }

        String str = strTmp.toString();
        if (eqCounter == 1) {
            int i = strTmp.length() - 2;
            str = str.substring(0, i);
        } else if (eqCounter == 2) {
            int i = strTmp.length() - 4;
            str = str.substring(0, i);
        }

        int length = str.length() / 8;
        String[] strArray = new String[length];
        for (int i = 0; i < length; i++) {
            strArray[i] = str.substring(i * 8, i * 8 + 8);
        }

        byte[] byteTmp = new byte[length];
        for (int i = 0; i < length; i++) {
            byteTmp[i] = getByteFromStr(strArray[i]);
        }

        return byteTmp;
    }

    public static void main(String[] args) {
        byte[] a = {1, 2, 3, -7, -9, 110};
        String s = encode(a);
        System.out.println(s);
        byte[] b = decode(s);
        for (int i = 0; i < b.length; i++) {
            System.out.print(b[i] + " ");
        }
        System.out.println();

    }

}

