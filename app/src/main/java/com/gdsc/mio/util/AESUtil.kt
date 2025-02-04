package com.gdsc.mio.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AESUtil {
    // AES 비밀키 생성 (보통은 저장해두어야 함)
    /*fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256) // 256-bit AES 키 생성
        return keyGen.generateKey()
    }*/


    // AES 암호화 함수 (IV는 Cipher가 자동으로 생성)
    /*fun encryptAES(secretKey: SecretKey, plainText: String): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        // 자동으로 IV가 생성됨
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // 암호화 수행
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 자동으로 생성된 IV를 가져옴
        val iv = cipher.iv

        // 암호화된 결과와 IV를 Base64로 인코딩하여 반환
        val encryptedText = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        val encodedIV = Base64.encodeToString(iv, Base64.DEFAULT)

        return Pair(encryptedText, encodedIV)
    }*/
    fun encryptAES(secretKey: SecretKey, plainText: String): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv

        val encryptedText = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        val encodedIV = Base64.encodeToString(iv, Base64.DEFAULT)

        return Pair(encryptedText, encodedIV) //원하는 text + iv
    }

    // AES 복호화 함수 (IV를 받아서 사용)
    fun decryptAES(secretKey: SecretKey, encryptedText: String, ivString: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        val iv = Base64.decode(ivString, Base64.DEFAULT)
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

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