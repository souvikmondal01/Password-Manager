package com.kivous.passwordmanager.domain.repository

import com.kivous.passwordmanager.domain.model.Account
import com.kivous.passwordmanager.util.Response
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    suspend fun insertAccount(account: Account): Flow<Response<Boolean>>

    suspend fun updateAccount(account: Account): Flow<Response<Boolean>>

    suspend fun deleteAccount(account: Account): Flow<Response<Boolean>>

    suspend fun getAccounts(): Flow<Response<Flow<List<Account>>>>

}