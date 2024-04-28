package com.kivous.passwordmanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kivous.passwordmanager.domain.model.Account

@Database(
    entities = [Account::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun getAccountDao(): AccountDao
}