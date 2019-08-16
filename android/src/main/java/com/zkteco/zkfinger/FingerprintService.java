package com.zkteco.zkfinger;
/**
 * the algorithm of fingerprint 10.0
 * @author Bruce
 *
 */
public class FingerprintService {

	static public native int init(int[] limit);

	static public native int close();

	static public native int verify(byte[] temp1, byte[] temp2);

	static public native int verifyId(byte[] temp, String id);

	static public native int setParameter(int optCode, int value);

	static public native int identify(byte[] temp, byte[] idstr, int threshold,
									  int count);

	static public native int merge(byte[] temp1, byte[] temp2, byte[] temp3,
								   byte[] temp);

	static public native int save(byte[] temp, String id);

	static public native int get(byte[] temp, String id);

	static public native int del(String id);

	static public native int clear();

	static public native int count();

	static public native int version(byte[] version);

	static {
		System.loadLibrary("zkfinger10");
	}
}
