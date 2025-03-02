package com.billcorea.googleai0521.utils

import android.annotation.SuppressLint
import com.billcorea.googleai0521.BuildConfig
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

object AesCryptor {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"
    private const val SECRET_KEY = BuildConfig.SECRET_KEY // Replace with your own secret key : 16byte, 32byte
    private const val IV = "YourInitializationVector" // Replace with your own initialization vector

    @SuppressLint("GetInstance")
    fun encrypt(plainText: String): String {
        val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        val ivParameterSpec = IvParameterSpec(IV.toByteArray())

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    @SuppressLint("GetInstance")
    fun decrypt(encryptedText: String): String {
        val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        val ivParameterSpec = IvParameterSpec(IV.toByteArray())

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText))
        return String(decryptedBytes)
    }
}
