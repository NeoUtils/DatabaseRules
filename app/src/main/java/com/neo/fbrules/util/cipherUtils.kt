package com.neo.fbrules.util

import android.util.Base64
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
fun getKeyFromPassword(password: String, salt: String, length: Int = 256): SecretKey {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 65536, length)
    return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
}

fun getIvParameterSpec(): IvParameterSpec {
    val iv = ByteArray(16) { 0 }
    SecureRandom().nextBytes(iv)
    return IvParameterSpec(iv)
}

//encrypt and decrypt

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun encrypt(
    input: ByteArray,
    algorithm: String,
    key: SecretKey?,
    iv: IvParameterSpec = getIvParameterSpec()
): ByteArray {
    val cipher: Cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)
    return cipher.doFinal(input)
}

fun String.encrypt(
    key: SecretKey?,
    algorithm: String = "AES/CBC/PKCS5Padding",
    iv: IvParameterSpec = getIvParameterSpec()
): String {
    return Base64.encodeToString(
        encrypt(this.toByteArray(Charsets.UTF_8), algorithm, key, iv),
        Base64.DEFAULT
    )
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun decrypt(
    cipherText: ByteArray,
    algorithm: String,
    key: SecretKey,
    iv: IvParameterSpec = getIvParameterSpec()
): ByteArray {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, key, iv)
    return cipherText
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun String.decrypt(
    key: SecretKey,
    algorithm: String = "AES/CBC/PKCS5Padding",
    iv: IvParameterSpec = getIvParameterSpec()
): String {

    return Base64.encodeToString(
        decrypt(this.toByteArray(Charsets.US_ASCII), algorithm, key, iv),
        Base64.DEFAULT
    )
}

object AES {

    //const val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    const val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    const val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX

    fun encrypt(strToEncrypt: String, secretKey: String): String {

        val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec =
            PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
        val tmp = factory.generateSecret(spec)

        val mSecretKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, mSecretKey, ivParameterSpec)

        return Base64.encodeToString(
            cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)),
            Base64.DEFAULT
        )
    }

    fun decrypt(strToDecrypt: String, secretKey: String): String? {
        try {

            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =
                PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec);
            val mSecretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, mSecretKey, ivParameterSpec);
            return String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        } catch (e: Exception) {
            println("Error while decrypting: $e");
        }
        return null
    }
}