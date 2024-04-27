package com.kivous.passwordmanager.domain.use_case

data class UseCases(
    val insertAccount: InsertAccount,
    val getAccounts: GetAccounts,
    val deleteAccount: DeleteAccount,
    val updateAccount: UpdateAccount
)