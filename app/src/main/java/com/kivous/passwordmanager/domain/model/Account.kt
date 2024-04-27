package com.kivous.passwordmanager.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts"
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var accountName: String? = null,
    var username: String? = null,
    var password: ByteArray? = null,
    var timestamp: Long? = null
)
