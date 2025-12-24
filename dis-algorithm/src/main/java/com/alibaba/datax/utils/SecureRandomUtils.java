package com.alibaba.datax.utils;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

public class SecureRandomUtils {
    public static final SecureRandom getInstance() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static UUID randomUUID() {
        SecureRandom secureRandom = getInstance();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        randomBytes[6]  &= 0x0f;  /* clear version        */
        randomBytes[6]  |= 0x40;  /* set to version 4     */
        randomBytes[8]  &= 0x3f;  /* clear variant        */
        randomBytes[8]  |= 0x80;  /* set to IETF variant  */
        return new UUID(ByteBuffer.wrap(randomBytes).getLong(), ByteBuffer.wrap(randomBytes, 8, 8).getLong());
    }

    /**
     * 获取n位随机字符串
     *
     * @return
     */
    public static String getSecureRandomString(int length) {
        SecureRandom secureRandom = getInstance();
        int len = length / 2;
        byte[] values = new byte[len];
        secureRandom.nextBytes(values);
        StringBuilder sb = new StringBuilder();
        for (byte b : values) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 获取随机UUID字符串
     * @param upperCase 是否大写
     * @param withHyphen 是否包含连接符
     * @return
     */
    public static String randomUUIDString(boolean upperCase, boolean withHyphen) {
        UUID uuid = randomUUID();
        String uuidStr = uuid.toString();
        if (upperCase) {
            uuidStr = uuidStr.toUpperCase();
        }
        if (!withHyphen) {
            uuidStr = uuidStr.replace("-", "");
        }
        return uuidStr;
    }
}
