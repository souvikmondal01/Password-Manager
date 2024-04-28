package com.kivous.passwordmanager.data.database

import androidx.room.TypeConverter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

class Converters {
    @TypeConverter
    fun privateKeyToString(privateKey: PrivateKey?): String? =
        privateKey?.let {
            val privateKeyBytes = it.encoded
            Base64.getEncoder().encodeToString(privateKeyBytes)
        }

    @TypeConverter
    fun stringToPrivateKey(value: String?): PrivateKey? =
        value?.let {
            val privateKeyBytes = Base64.getDecoder().decode(value)
            val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePrivate(keySpec)
        }

}