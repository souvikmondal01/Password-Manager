package com.kivous.passwordmanager.domain.use_case

data class UseCases(
    val insertAccount: InsertAccount,
    val updateAccount: UpdateAccount,
    val deleteAccount: DeleteAccount,
    val getAccounts: GetAccounts,
)