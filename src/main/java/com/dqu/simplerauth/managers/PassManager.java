package com.dqu.simplerauth.managers;

import com.dqu.simplerauth.AuthMod;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class PassManager {
    public static String encrypt(String password) {
        try {
            byte[] salt = generateSalt();
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return toHex(salt) + ":" + toHex(hash);
        } catch (Exception e) {
            AuthMod.LOGGER.error(e);
        }
        return null;
    }

    public static String encrypt(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return toHex(salt) + ":" + toHex(hash);
        } catch (Exception e) {
            AuthMod.LOGGER.error(e);
        }
        return null;
    }

    public static boolean verify(String password, String hash) {
        String salt = hash.split(":")[0];
        String newHash = encrypt(password, fromHex(salt));
        return newHash.equals(hash);
    }

    private static byte[] generateSalt() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            AuthMod.LOGGER.error(e);
        }
        return null;
    }

    private static String toHex(byte[] bytes) {
        return String.format("%x", new BigInteger(1, bytes));
    }

    private static byte[] fromHex(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
