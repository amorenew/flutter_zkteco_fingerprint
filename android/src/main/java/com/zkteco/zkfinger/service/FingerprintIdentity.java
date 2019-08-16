package com.zkteco.zkfinger.service;

import android.content.Context;
import android.content.Intent;

import com.zkteco.zkfinger.util.CRC16;

import java.io.UnsupportedEncodingException;

public class FingerprintIdentity {

    public static void sendIdentityResult(Context context,int res, String Id) {
        byte[] bytes = new byte[]{(byte) 0xBF, (byte) 0xCF, 0x00, 0x00, 0x01, (byte) 0x0D, (byte) res,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xDF, (byte) 0xEF};
        try {
            if(0 == res) {
                byte[] idBytes = Id.getBytes("UTF-8");
                System.arraycopy(idBytes, 0, bytes, 7, idBytes.length);
            }
            short crc16 = (short) CRC16.CRC16_Check(bytes);
            bytes[2] = (byte)(crc16 >> 8);
            bytes[3] = (byte)crc16 ;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(BroadContanst.FINGERPRINT_IDENTIFY_ACTION);
        intent.putExtra("data",bytes);
        context.sendBroadcast(intent);
    }
}
