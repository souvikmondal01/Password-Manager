package com.kivous.passwordmanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kivous.passwordmanager.domain.model.Account

@Database(
    entities = [Account::class],
    version = 1
)

abstract class AccountDatabase : RoomDatabase() {
    abstract fun getAccountDao(): AccountDao
}