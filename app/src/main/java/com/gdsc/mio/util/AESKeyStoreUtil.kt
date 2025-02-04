package com.gdsc.mio.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object AESKeyStoreUtil {

    // Android Keystore에 AES 키 생성 및 저장
    private fun generateAESKeyInKeystore(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "MyKeyAlias",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    // Android Keystore에서 AES 키 로드
    private fun getSecretKeyFromKeystore(): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getKey("MyKeyAlias", null) as? SecretKey
    }

    fun getOrCreateAESKey(): SecretKey {
        return getSecretKeyFromKeystore() ?: generateAESKeyInKeystore()
    }

    /*fun deleteAESKeyFromKeystore() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry("MyKeyAlias")
            println("SecretKey deleted from Keystore")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error deleting SecretKey: ${e.message}")
        }
    }*/
}