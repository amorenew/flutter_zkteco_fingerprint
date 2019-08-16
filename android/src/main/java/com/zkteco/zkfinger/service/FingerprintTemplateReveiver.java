package com.zkteco.zkfinger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.zkteco.zkfinger.util.CRC16;

import java.io.UnsupportedEncodingException;

/**
 * receive fingerprint template data from Wiot App
 */
public class FingerprintTemplateReveiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        byte[] bytes = intent.getByteArrayExtra("data");
        if(checkCrc(bytes)){
            if(bytes[4] == 0x01 && bytes[5] == 0x0D){
                byte[] idBytes = new byte[20];
                System.arraycopy(bytes,6,idBytes,0,20);
                byte[] templateByte = new byte[bytes.length - 29];
                System.arraycopy(templateByte,26,idBytes,0,bytes.length - 29);
                String idStr = "";
                String templateStr = "";
                try {
                    idStr = new String(idBytes,"UTF-8");
                    templateStr = new String(templateByte,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    public boolean checkCrc(byte[] message) {
        int crc = ((message[2] << 8) & 0xFF00) | (message[3] & 0xFF);
        message[2] = 0x00;
        message[3] = 0x00;
        int crc16r = CRC16.CRC16_Check(message);
        if (crc == crc16r)
            return true;
        else
            return false;
    }
}
