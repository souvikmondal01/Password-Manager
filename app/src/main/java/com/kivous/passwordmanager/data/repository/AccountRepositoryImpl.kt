package com.kivous.passwordmanager.data.repository

import com.kivous.passwordmanager.data.database.AccountDatabase
import com.kivous.passwordmanager.domain.model.Account
import com.kivous.passwordmanager.domain.repository.AccountRepository
import com.kivous.passwordmanager.util.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val db: AccountDatabase
) : AccountRepository {
    override suspend fun insertAccount(account: Account): Flow<Response<Boolean>> = flow {
        try {
            emit(Response.Loading())
            CoroutineScope(Dispatchers.IO).async {
                db.getAccountDao().insertAccount(account)
            }.await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Error(e.message))
        }
    }

    override suspend fun updateAccount(account: Account): Flow<Response<Boolean>> = flow {
        try {
            emit(Response.Loading())
            CoroutineScope(Dispatchers.IO).async {
                db.getAccountDao().updateAccount(account)
            }.await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Error(e.message))
        }
    }

    override suspend fun deleteAccount(account: Account): Flow<Response<Boolean>> = flow {
        try {
            emit(Response.Loading())
            CoroutineScope(Dispatchers.IO).async {
                db.getAccountDao().deleteAccount(account)
            }.await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Error(e.message))
        }
    }

    override suspend fun getAccounts(): Flow<Response<Flow<List<Account>>>> = flow {
        try {
            emit(Response.Loading())
            val list = CoroutineScope(Dispatchers.IO).async {
                db.getAccountDao().getAccounts()
            }.await()
            emit(Response.Success(list))
        } catch (e: Exception) {
            emit(Response.Error(e.message))
        }
    }

}