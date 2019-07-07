package com.myco.util.crypto;

import com.myco.util.values.ObfuscatedToStringProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * An application service
 *
 * @author troyh
 *
 */
@Component
public class CryptoService {

  private final static String ALGORITHM_NAME = "AES/GCM/NoPadding";
  private final static int ALGORITHM_NONCE_SIZE = 12;
  private final static int ALGORITHM_TAG_SIZE = 128;
  private final static int ALGORITHM_KEY_SIZE = 128;
  private final static String PBKDF2_NAME = "PBKDF2WithHmacSHA256";
  private final static int PBKDF2_SALT_SIZE = 16;
  private final static int PBKDF2_ITERATIONS = 32767;


  public String encryptString(ObfuscatedToStringProperty<String> secret, ObfuscatedToStringProperty<String> plaintext) {

    byte[] salt = secureSalt();
    byte[] key;
    try {
      key = generateSecretKey(secret, salt);
    }
    catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException("Bug Alert! Unable to generate secret key bytes", e);
    }

    // Encrypt and prepend salt.
    byte[] ciphertextAndNonce;
    try {
      ciphertextAndNonce =
          doEncryption(new ObfuscatedToStringProperty<>(plaintext.getValue().getBytes(StandardCharsets.UTF_8)), key);
    }
    catch (InvalidKeyException
        | InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchPaddingException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new RuntimeException("Bug Alert! Unable to encrypt data", e);
    }

    byte[] ciphertextAndNonceAndSalt = new byte[salt.length + ciphertextAndNonce.length];
    System.arraycopy(salt, 0, ciphertextAndNonceAndSalt, 0, salt.length);
    System.arraycopy(ciphertextAndNonce, 0, ciphertextAndNonceAndSalt, salt.length, ciphertextAndNonce.length);

    // Return as base64 string.
    return Base64.getEncoder().encodeToString(ciphertextAndNonceAndSalt);
  }


  public ObfuscatedToStringProperty<String> decryptString(ObfuscatedToStringProperty<String> secret,
      String base64CiphertextAndNonceAndSalt) {
    Assert.state(StringUtils.hasText(secret.getValue()), "null/blank secure data secret");
    Assert.hasText(base64CiphertextAndNonceAndSalt, "null/blank value to decrypt");

    // Decode the base64.
    byte[] ciphertextAndNonceAndSalt = Base64.getDecoder().decode(base64CiphertextAndNonceAndSalt);

    // Retrieve the salt and ciphertextAndNonce.
    byte[] salt = new byte[PBKDF2_SALT_SIZE];
    byte[] ciphertextAndNonce = new byte[ciphertextAndNonceAndSalt.length - PBKDF2_SALT_SIZE];
    System.arraycopy(ciphertextAndNonceAndSalt, 0, salt, 0, salt.length);
    System.arraycopy(ciphertextAndNonceAndSalt, salt.length, ciphertextAndNonce, 0, ciphertextAndNonce.length);

    byte[] key;
    try {
      key = generateSecretKey(secret, salt);
    }
    catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException("Unable to generate secret key bytes", e);
    }

    // Decrypt and return result.
    try {
      return new ObfuscatedToStringProperty<>(
          new String(doDecryption(ciphertextAndNonce, key).getValue(), StandardCharsets.UTF_8));
    }
    catch (InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException
        | NoSuchAlgorithmException
        | NoSuchPaddingException e) {
      throw new RuntimeException("Unable to decrypt data", e);
    }
  }


  private byte[] secureSalt() {
    // Generate a 128-bit salt using a CSPRNG.
    SecureRandom rand = new SecureRandom();
    byte[] salt = new byte[PBKDF2_SALT_SIZE];
    rand.nextBytes(salt);
    return salt;
  }


  private byte[] generateSecretKey(ObfuscatedToStringProperty<String> secret, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Create an instance of PBKDF2 and derive a key.
    PBEKeySpec pwSpec = new PBEKeySpec(secret.getValue().toCharArray(), salt, PBKDF2_ITERATIONS, ALGORITHM_KEY_SIZE);
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_NAME);
    return keyFactory.generateSecret(pwSpec).getEncoded();
  }


  private byte[] doEncryption(ObfuscatedToStringProperty<byte[]> plaintext, byte[] key)
      throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException,
      IllegalBlockSizeException, BadPaddingException {

    // Generate a 96-bit nonce using a CSPRNG.
    SecureRandom rand = new SecureRandom();
    byte[] nonce = new byte[ALGORITHM_NONCE_SIZE];
    rand.nextBytes(nonce);

    // Create the cipher instance and initialize.
    Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(ALGORITHM_TAG_SIZE, nonce));

    // Encrypt and prepend nonce.
    byte[] ciphertext = cipher.doFinal(plaintext.getValue());
    byte[] ciphertextAndNonce = new byte[nonce.length + ciphertext.length];
    System.arraycopy(nonce, 0, ciphertextAndNonce, 0, nonce.length);
    System.arraycopy(ciphertext, 0, ciphertextAndNonce, nonce.length, ciphertext.length);

    return ciphertextAndNonce;
  }

  private ObfuscatedToStringProperty<byte[]> doDecryption(byte[] ciphertextAndNonce, byte[] key)
      throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
      NoSuchAlgorithmException, NoSuchPaddingException {
    // Retrieve the nonce and ciphertext.
    byte[] nonce = new byte[ALGORITHM_NONCE_SIZE];
    byte[] ciphertext = new byte[ciphertextAndNonce.length - ALGORITHM_NONCE_SIZE];
    System.arraycopy(ciphertextAndNonce, 0, nonce, 0, nonce.length);
    System.arraycopy(ciphertextAndNonce, nonce.length, ciphertext, 0, ciphertext.length);

    // Create the cipher instance and initialize.
    Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(ALGORITHM_TAG_SIZE, nonce));

    // Decrypt and return result.
    return new ObfuscatedToStringProperty<>(cipher.doFinal(ciphertext));
  }
}
