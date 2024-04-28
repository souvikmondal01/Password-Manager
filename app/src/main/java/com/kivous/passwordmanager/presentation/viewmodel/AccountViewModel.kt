package com.kivous.passwordmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivous.passwordmanager.domain.model.Account
import com.kivous.passwordmanager.domain.use_case.UseCases
import com.kivous.passwordmanager.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private var _addAccountStatus = MutableStateFlow<Response<Boolean>>(Response.Error(null))
    val addAccountStatus: StateFlow<Response<Boolean>> = _addAccountStatus

    private var _updateAccountStatus = MutableStateFlow<Response<Boolean>>(Response.Error(null))
    val updateAccountStatus: StateFlow<Response<Boolean>> = _updateAccountStatus

    private var _deleteAccountStatus = MutableStateFlow<Response<Boolean>>(Response.Error(null))
    val deleteAccountStatus: StateFlow<Response<Boolean>> = _deleteAccountStatus

    private var _accounts = MutableStateFlow<Response<Flow<List<Account>>>>(Response.Error(null))
    val accounts: StateFlow<Response<Flow<List<Account>>>> = _accounts

    fun addAccount(account: Account) = viewModelScope.launch {
        useCases.insertAccount.invoke(account).collect {
            _addAccountStatus.value = it
        }
    }

    fun updateAccount(account: Account) = viewModelScope.launch {
        useCases.updateAccount.invoke(account).collect {
            _updateAccountStatus.value = it
        }
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        useCases.deleteAccount.invoke(account).collect {
            _deleteAccountStatus.value = it
        }
    }

    fun getAccounts() = viewModelScope.launch {
        useCases.getAccounts.invoke().collect {
            _accounts.value = it
        }
    }

}