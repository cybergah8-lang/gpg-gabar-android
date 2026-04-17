package com.kardoxi.gpg_gabar

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    data class EncryptedPrivateKey(
        val cipherText: ByteArray,
        val iv: ByteArray,
        val salt: ByteArray
    )

    fun generateRsaKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(4096)
        return kpg.generateKeyPair()
    }

    fun encryptPrivateKey(privateKeyBytes: ByteArray, password: CharArray): EncryptedPrivateKey {
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) } // 12 bytes for GCM

        val keySpec = PBEKeySpec(password, salt, 65_536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcm = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcm)
        val cipherText = cipher.doFinal(privateKeyBytes)
        return EncryptedPrivateKey(cipherText, iv, salt)
    }

    fun decodePrivateKey(encrypted: EncryptedPrivateKey, password: CharArray): ByteArray {
        val keySpec = PBEKeySpec(password, encrypted.salt, 65_536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcm = GCMParameterSpec(128, encrypted.iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcm)
        return cipher.doFinal(encrypted.cipherText)
    }
}
