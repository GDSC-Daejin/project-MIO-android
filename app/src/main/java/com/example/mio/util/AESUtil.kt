package com.example.mio.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AESUtil {
    // AES 비밀키 생성 (보통은 저장해두어야 함)
    fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256) // 256-bit AES 키 생성
        return keyGen.generateKey()
    }

    // 비밀키로 문자열 암호화 (AES/GCM/NoPadding)
    fun encryptAES(secretKey: SecretKey, plainText: String): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        // IV(Initial Vector) 생성 (GCM 모드에서는 12바이트 권장)
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv) // 랜덤 IV 생성

        // GCMParameterSpec에 IV와 인증 태그 크기(128비트) 설정
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        // 암호화 수행
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 암호화된 결과와 IV를 Base64로 인코딩하여 반환
        val encryptedText = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        val encodedIV = Base64.encodeToString(iv, Base64.DEFAULT)

        return Pair(encryptedText, encodedIV)
    }

    // 비밀키로 문자열 복호화 (AES/GCM/NoPadding)
    fun decryptAES(secretKey: SecretKey, encryptedText: String, encodedIV: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        // Base64로 인코딩된 IV를 복호화
        val iv = Base64.decode(encodedIV, Base64.DEFAULT)

        // GCMParameterSpec을 사용해 IV와 인증 태그 크기(128비트)를 설정
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        // 암호화된 텍스트 복호화
        val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }
    /**사용법**/
    /*
    val secretKey = generateAESKey() // AES 키 생성
    val password = "1234" // 암호화할 비밀번호

    // 비밀번호 암호화
    val encryptedPassword = encryptAES(secretKey, password)
    println("암호화된 비밀번호: $encryptedPassword")

    // 암호화된 비밀번호 복호화
    val decryptedPassword = decryptAES(secretKey, encryptedPassword)
    println("복호화된 비밀번호: $decryptedPassword")


    val secretKey = AESKeyStoreUtil.getSecretKeyFromKeystore()

    // 암호화
    val (encryptedText, iv) = encryptAES(secretKey, "Hello, world!")

    // 복호화
    val decryptedText = decryptAES(secretKey, encryptedText, iv)

    println(decryptedText)  // "Hello, world!"

    * */
}