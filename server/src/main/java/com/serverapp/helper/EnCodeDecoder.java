package com.serverapp.helper;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.Getter;

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
}
