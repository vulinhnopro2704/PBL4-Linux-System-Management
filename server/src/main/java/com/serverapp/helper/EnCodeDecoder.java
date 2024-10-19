package com.serverapp.helper;

import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

@Getter
public class EnCodeDecoder {
    private PublicKey rsaPublicKey;
    private PrivateKey rsaPrivateKey;

    private static EnCodeDecoder _INSTANCE;

    private EnCodeDecoder() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        rsaPublicKey = pair.getPublic();
        rsaPrivateKey = pair.getPrivate();
    }

    public static EnCodeDecoder getInstance() {
        try {
            if (_INSTANCE == null) {
                _INSTANCE = new EnCodeDecoder();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return _INSTANCE;
    }


    public String encryptCommand(String command, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(command.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptResponse(String encryptedResponse, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedResponse);
        return new String(cipher.doFinal(decodedBytes));
    }

    public SecretKey decryptAESKey(String encryptedKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] decodedKey = Base64.getDecoder().decode(encryptedKey);
        byte[] aesKey = cipher.doFinal(decodedKey);
        return new SecretKeySpec(aesKey, "AES");
    }
}
