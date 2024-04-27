package com.kivous.passwordmanager.domain.use_case

import com.kivous.passwordmanager.domain.model.Account
import com.kivous.passwordmanager.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccount @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(account: Account) = repository.deleteAccount(account)
}