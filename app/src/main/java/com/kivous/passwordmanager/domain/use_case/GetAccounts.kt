package com.kivous.passwordmanager.domain.use_case

import com.kivous.passwordmanager.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccounts @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke() = repository.getAccounts()
}