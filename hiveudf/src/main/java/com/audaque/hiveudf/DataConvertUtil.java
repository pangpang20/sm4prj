package com.audaque.hiveudf;

/**
 * @author liudaizhong.liu
 * @date 2019年11月6日 下午8:27:47
 * @desc
 */
public class DataConvertUtil {
	/**
	 * 用于建立十六进制字符的输出的小写字符数组
	 */
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * 循环左移
	 * @param sourceByte 待左移动的值
	 * @param n          左移动的为数
	 * @return
	 */
	private static byte rotateLeft(byte sourceByte, int n) {
		// 去除高位的1
		int temp = sourceByte & 0xFF;
		return (byte) ((temp << n) | (temp >>> (8 - n)));
	}

	/**
	 * 循环右移
	 * @param sourceByte
	 * @param n
	 * @return
	 */
	private static byte rotateRight(byte sourceByte, int n) {
		// 去除高位的1
		int temp = sourceByte & 0xFF;
		return (byte) ((temp >>> n) | (temp << (8 - n)));
	}

	/**
	 * 循环左移
	 * @param sourceBytes
	 * @param n
	 * @return
	 */
	public static byte[] rotateLeft(byte[] sourceBytes, int n) {
		byte[] out = new byte[sourceBytes.length];
		for (int i = 0; i < sourceBytes.length; i++) {
			out[i] = rotateLeft(sourceBytes[i], n);
		}
		return out;
	}
	/**
	 * 循环右移
	 * @param sourceBytes
	 * @param n 
	 * @return
	 */
	public static byte[] rotateRight(byte[] sourceBytes, int n) {
		byte[] out = new byte[sourceBytes.length];
		for (int i = 0; i < sourceBytes.length; i++) {
			out[i] = rotateRight(sourceBytes[i], n);
		}
		return out;
	}
	
	/**
	 * @desc byte 数组与 int 的相互转换
	 * @param b byte数组
	 * @return
	 */
	public static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	/**
	 * @desc int 与 byte 数组 的相互转换
	 * @param a 要相互转换的值
	 * @return
	 */
	public static byte[] intToByteArray(int a) {
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}

	 /**
     *  '将16进制字符转化为byte数组
     * @param s 16进制字符
     * @return byte数组
     */
	public static byte[] fromHexString(String s) {
		char[] rawChars = s.toUpperCase().toCharArray();
		int hexChars = 0;
		for (char rawChar : rawChars) {
			if ((rawChar >= '0' && rawChar <= '9') || (rawChar >= 'A' && rawChar <= 'F')) {
				hexChars++;
			}
		}

		byte[] byteString = new byte[(hexChars + 1) >> 1];

		int pos = hexChars & 1;

		for (char rawChar : rawChars) {
			if (rawChar >= '0' && rawChar <= '9') {
				byteString[pos >> 1] <<= 4;
				byteString[pos >> 1] |= (byte) (rawChar - '0');
			} else if (rawChar >= 'A' && rawChar <= 'F') {
				byteString[pos >> 1] <<= 4;
				byteString[pos >> 1] |= (byte) (rawChar - 'A' + 10);
			} else {
				continue;
			}
			pos++;
		}

		return byteString;
	}

    /**
     * byte组转化为16进制字符
     * @param input byte数组
     * @return 16进制字符
     */
    public static String toHexString(byte[] input){
		StringBuilder result = new StringBuilder();
		for (byte b : input) {
			result.append(DIGITS_LOWER[(b >>> 4) & 0x0f]);
			result.append(DIGITS_LOWER[b & 0x0f]);
		}
		return result.toString();
	}
	
}
