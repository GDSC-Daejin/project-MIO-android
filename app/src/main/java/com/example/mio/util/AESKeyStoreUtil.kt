package com.example.mio.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object AESKeyStoreUtil {

    // Android Keystore에 AES 키 생성 및 저장
    fun generateAESKeyInKeystore(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "MyKeyAlias",  // 키의 별칭
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)  // AES GCM 모드
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)  // 패딩 없음
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    // Android Keystore에서 AES 키 로드
    fun getSecretKeyFromKeystore(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getKey("MyKeyAlias", null) as SecretKey
    }
}