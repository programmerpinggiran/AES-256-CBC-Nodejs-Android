package app.sample.aes256cbc.helper;


import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Helper {

    public static String secret_key = "YOUR_SECRET_KEY";

    public static String md5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }


    public static String createRandomString(int len) {
        Random RANDOM = new Random();
        String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }



    public static String encrypt(String value) {
        try {
            String iv = createRandomString(16);
            String key = md5(secret_key);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] crypted = cipher.doFinal(value.getBytes());
            String base64 = Base64.encodeToString(crypted, Base64.NO_WRAP);
            return iv + base64;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static String decrypt(String input) {
        if (input == null) {
            return null;
        }
        input = input.replace("\"", "");
        String in = input;
        String iv = input.substring(0, 16);
        String crypted = input.substring(16, in.length());
        String key = md5(secret_key);
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
            byte[] base64 = Base64.decode(crypted, Base64.NO_WRAP);
            byte[] original = cipher.doFinal(base64);
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }


}

