package com.xinecraft.utils;

import com.google.gson.Gson;
import com.xinecraft.Minetrax;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;

public class CryptoUtil {
    public static String encrypt(byte[] keyValue, String plaintext) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        //serialize
        String serializedPlaintext = "s:" + plaintext.getBytes().length + ":\"" + plaintext + "\";";
        byte[] plaintextBytes = serializedPlaintext.getBytes(StandardCharsets.UTF_8);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = c.getIV();
        byte[] encVal = c.doFinal(plaintextBytes);
        String encryptedData = Base64.getEncoder().encodeToString(encVal);

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(Base64.getEncoder().encode(iv));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes(StandardCharsets.UTF_8));
        String mac = new String(Hex.encodeHex(calcMac));
        // System.out.println("MAC: "+mac);

        AesEncryptionData aesData = new AesEncryptionData(
                Base64.getEncoder().encodeToString(iv),
                encryptedData,
                mac);

        String aesDataJson = new Gson().toJson(aesData);

        return Base64.getEncoder().encodeToString(aesDataJson.getBytes(StandardCharsets.UTF_8));
    }

    public static String decrypt(byte[] keyValue, String ivValue, String encryptedData, String macValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        byte[] iv = Base64.getDecoder().decode(ivValue.getBytes(StandardCharsets.UTF_8));
        byte[] decodedValue = Base64.getDecoder().decode(encryptedData.getBytes(StandardCharsets.UTF_8));

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(ivValue.getBytes(StandardCharsets.UTF_8));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes(StandardCharsets.UTF_8));
        byte[] mac = Hex.decodeHex(macValue.toCharArray());
        if (!Arrays.equals(calcMac, mac)) {
            Minetrax.getPlugin().getLogger().warning("Mac Mismatch while decrypting data. Please check your API Key and Secret.");
            return null;
        }

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decValue = c.doFinal(decodedValue);

        int firstQuoteIndex = 0;
        while (decValue[firstQuoteIndex] != (byte) '"') firstQuoteIndex++;
        return new String(Arrays.copyOfRange(decValue, firstQuoteIndex + 1, decValue.length - 2));
    }

    public static String getDecryptedString(String secretKey, String encryptedString) {
        String aesDataString = new String(Base64.getDecoder().decode(encryptedString.trim().getBytes(StandardCharsets.UTF_8)));

        AesEncryptionData aesEncryptedData = new Gson().fromJson(aesDataString, AesEncryptionData.class);

        String decrypted = null;
        try {
            decrypted = decrypt(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    aesEncryptedData.iv,
                    aesEncryptedData.value,
                    aesEncryptedData.mac
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public static String getEncryptedString(String secretKey, String plainString)
    {
        String encryptedString = null;
        try {
            encryptedString = CryptoUtil.encrypt(secretKey.getBytes(StandardCharsets.UTF_8), plainString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }
}
