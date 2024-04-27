package com.kivous.passwordmanager.di

import android.content.Context
import androidx.room.Room
import com.kivous.passwordmanager.data.database.AccountDatabase
import com.kivous.passwordmanager.data.repository.AccountRepositoryImpl
import com.kivous.passwordmanager.domain.repository.AccountRepository
import com.kivous.passwordmanager.domain.use_case.DeleteAccount
import com.kivous.passwordmanager.domain.use_case.GetAccounts
import com.kivous.passwordmanager.domain.use_case.InsertAccount
import com.kivous.passwordmanager.domain.use_case.UpdateAccount
import com.kivous.passwordmanager.domain.use_case.UseCases
import com.kivous.passwordmanager.util.RSA
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyPair
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideArticleDB(@ApplicationContext context: Context): AccountDatabase =
        Room.databaseBuilder(
            context, AccountDatabase::class.java, "account_db.db"
        ).build()

    @Provides
    @Singleton
    fun provideAccountRepository(db: AccountDatabase): AccountRepository = AccountRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideUseCases(repository: AccountRepository) =
        UseCases(
            insertAccount = InsertAccount(repository),
            updateAccount = UpdateAccount(repository),
            deleteAccount = DeleteAccount(repository),
            getAccounts = GetAccounts(repository),
        )


}