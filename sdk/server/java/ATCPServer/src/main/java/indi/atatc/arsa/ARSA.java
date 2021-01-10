package indi.atatc.arsa;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ARSA {
    public static class AKeyPair {
        private final APublicKey publicKey;
        private final APrivateKey privateKey;
        private final int keyLength;

        public AKeyPair(APublicKey publicKey, APrivateKey privateKey, int keyLength) throws InvalidKeySpecException, NoSuchAlgorithmException {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.keyLength = keyLength;
        }

        public APublicKey getPublicKey() {
            return publicKey;
        }

        public APrivateKey getPrivateKey() {
            return privateKey;
        }

        public int getKeyLength() {
            return keyLength;
        }
    }

    public static class APublicKey {
        private final String publicKey;
        private final int keyLength;
        private final PublicKey n;

        private APublicKey(String publicKeyString, PublicKey publicKeyObject, int keyLength) {
            this.publicKey = publicKeyString;
            this.keyLength = keyLength;
            n = publicKeyObject;
        }

        public int getKeyLength() {
            return keyLength;
        }

        public PublicKey getPublicKey() {
            return n;
        }

        public static APublicKey importPublicKey(String publicKey, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
            byte[] buffer = Base64.getDecoder().decode(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return new APublicKey(publicKey, keyFactory.generatePublic(keySpec), keyLength);
        }

        public static APublicKey importPublicKey(PublicKey publicKey, int keyLength) {
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            return new APublicKey(publicKeyString, publicKey, keyLength);
        }

        @Override
        public String toString() {
            return publicKey;
        }
    }

    public static class APrivateKey {
        private final String privateKey;
        private final int keyLength;
        private final PrivateKey n;

        private APrivateKey(String privateKeyString, PrivateKey privateKeyObject, int keyLength) {
            this.privateKey = privateKeyString;
            this.keyLength = keyLength;
            n = privateKeyObject;
        }

        public int getKeyLength() {
            return keyLength;
        }

        public PrivateKey getPrivateKey() {
            return n;
        }

        public static APrivateKey importPrivateKey(String privateKey, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
            byte[] buffer = Base64.getDecoder().decode(privateKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return new APrivateKey(privateKey, keyFactory.generatePrivate(keySpec), keyLength);
        }

        public static APrivateKey importPrivateKey(PrivateKey privateKey, int keyLength) {
            String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            return new APrivateKey(privateKeyString, privateKey, keyLength);
        }

        @Override
        public String toString() {
            return privateKey;
        }

    }

    public static AKeyPair newKeys(int keyLength) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keyLength);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            return new AKeyPair(APublicKey.importPublicKey(keyPair.getPublic(), keyLength), APrivateKey.importPrivateKey(keyPair.getPrivate(), keyLength), keyLength);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String sign(String content, APrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey.getPrivateKey());
        signer.update(content.getBytes());
        return Base64.getEncoder().encodeToString(signer.sign());
    }

    public static boolean verify(String content, String signature, APublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] signature_bytes = Base64.getDecoder().decode(signature);
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initVerify(publicKey.getPublicKey());
        signer.update(content.getBytes());
        return signer.verify(signature_bytes);
    }

    public static String encrypt(String content, APublicKey publicKey) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        byte[] content_bytes = content.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey.getPublicKey());
        int para_len = publicKey.getKeyLength() / 8 - 11;
        int content_len = content_bytes.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        while (content_len - offset > 0) {
            if (content_len - offset > para_len) {
                cache = cipher.doFinal(content_bytes, offset, para_len);
            } else {
                cache = cipher.doFinal(content_bytes, offset, content_len - offset);
            }
            byteArrayOutputStream.write(cache, 0, cache.length);
            offset += para_len;
        }
        String res = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.close();
        return res;
    }

    public static String decrypt(String content, APrivateKey privateKey) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        byte[] content_bytes = Base64.getDecoder().decode(content);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey.getPrivateKey());
        int para_len = privateKey.getKeyLength() / 8;
        int content_len = content_bytes.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        while (content_len - offset > 0) {
            if (content_len - offset > para_len) {
                cache = cipher.doFinal(content_bytes, offset, para_len);
            } else {
                cache = cipher.doFinal(content_bytes, offset, content_len - offset);
            }
            byteArrayOutputStream.write(cache, 0, cache.length);
            offset += para_len;
        }
        String res = byteArrayOutputStream.toString();
        byteArrayOutputStream.close();
        return res;
    }

}
