package service.orchestrationservice.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

public class GenerateKey {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        PublicKey publicKey = pair.getPublic();
        byte[] encoded = publicKey.getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(encoded);
        String pem = "-----BEGIN PUBLIC KEY-----\n" +
                base64Key.replaceAll("(.{64})", "$1\n") +
                "\n-----END PUBLIC KEY-----";
        System.out.println(pem);
    }
}