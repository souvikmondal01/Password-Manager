package com.kivous.passwordmanager.presentation.fragment

import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivous.passwordmanager.databinding.FragmentAddNewBottomSheetBinding
import com.kivous.passwordmanager.databinding.FragmentHomeBinding
import com.kivous.passwordmanager.domain.model.Account
import com.kivous.passwordmanager.presentation.adapter.AccountAdapter
import com.kivous.passwordmanager.presentation.fragment.ShowPasswordBottomSheet.Companion.TAG
import com.kivous.passwordmanager.presentation.viewmodel.AccountViewModel
import com.kivous.passwordmanager.util.Extensions.gone
import com.kivous.passwordmanager.util.Extensions.logD
import com.kivous.passwordmanager.util.Extensions.timeStamp
import com.kivous.passwordmanager.util.Extensions.toast
import com.kivous.passwordmanager.util.Extensions.visible
import com.kivous.passwordmanager.util.RSA.decrypt
import com.kivous.passwordmanager.util.RSA.encrypt
import com.kivous.passwordmanager.util.RSA.generateKeyPair
import com.kivous.passwordmanager.util.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.security.KeyPair
import kotlin.math.log

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by viewModels()
    private lateinit var adapter: AccountAdapter
    private lateinit var addNewBottomSheet: AddNewBottomSheet
    private lateinit var showPasswordBottomSheet: ShowPasswordBottomSheet
    private lateinit var addNewBottomSheetBinding: FragmentAddNewBottomSheetBinding
    private lateinit var keyPair: KeyPair

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize KeyPair
        keyPair = generateKeyPair()

        binding.apply {
            fab.setOnClickListener {
                if (!addNewBottomSheet.isAdded) { // <- Repeated clicks can cause the app to crash.
                    addNewBottomSheet.show(
                        childFragmentManager, AddNewBottomSheet.TAG
                    ) // show AddNew BottomSheet
                }
            }
        }

        // Initialize AddNew BottomSheet
        addNewBottomSheet = AddNewBottomSheet {
            addNewBottomSheetBinding = it
            it.apply {
                btnAdd.setOnClickListener {
                    // get data from edittext
                    val accountName = etAccountName.text.toString().trim()
                    val username = etUsername.text.toString().trim()
                    val password = etPassword.text.toString().trim()

                    // Check that no fields are empty
                    if (accountName.isEmpty()) {
                        etAccountName.error = "Enter Account Name"
                    } else if (username.isEmpty()) {
                        etUsername.error = "Enter Username/Email"
                    } else if (password.isEmpty()) {
                        etPassword.error = "Enter Password"
                    } else {

//                        try {
                        val encryptedPassword = encrypt(password.toByteArray(), keyPair.public)

                        val account = Account(
                            accountName = accountName,
                            username = username,
                            password = encryptedPassword,
                            timestamp = timeStamp()
                        )
                        // Add new account to database
                        viewModel.addAccount(account)
//                        } catch (e: Exception) {
//                            logD(e.message)
//                        }

                    }

                }
            }
        }

        // Initialize PasswordShow BottomSheet
        showPasswordBottomSheet = ShowPasswordBottomSheet { }

        //Initialize Account Adapter
        adapter = AccountAdapter(::accountAdapterViewController)

        // SetUp RecyclerView
        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        // Get all accounts
        viewModel.getAccounts()
        collectAccounts()

        collectAddAccountStatus()
        collectUpdateAccountStatus()
        collectDeleteAccountStatus()


    }

    override fun onPause() {
        super.onPause()
        if (addNewBottomSheet.isVisible) {
            addNewBottomSheet.dismissNow()
        }
        if (showPasswordBottomSheet.isVisible) {
            showPasswordBottomSheet.dismissNow()
        }

        /** Because changing the app theme while
         * the bottom sheet is open can cause the app to crash. **/
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun accountAdapterViewController(holder: AccountAdapter.ViewHolder, account: Account) {
        holder.apply {
            binding.apply {
                // Set data to RecyclerView items
                tvAccountName.text = account.accountName
            }

            itemView.setOnClickListener {
//                try {
                    val decryptedPassword = decrypt(account.password!!, keyPair.private)
                    val password = String(decryptedPassword)
                    logD(password)
//                } catch (e: Exception) {
//                    logD(e.message)
//                    logD(e.cause)
//                    logD(e.stackTrace)
//                }

                // Initialize PasswordShow BottomSheet on every click of RecyclerView item
                showPasswordBottomSheet = ShowPasswordBottomSheet { binding ->
                    binding.apply {
                        // Set data to edittext of ShowPassword BottomSheet
                        etAccountName.setText(account.accountName)
                        etUsername.setText(account.username)
                        etPassword.setText(password)

                        btnDelete.setOnClickListener {
                            // Delete account
                            viewModel.deleteAccount(account)
                        }

                        btnEdit.setOnClickListener {
                            // get data from edittext of ShowPassword BottomSheet
                            val accountName = etAccountName.text.toString().trim()
                            val username = etUsername.text.toString().trim()
                            val password2 = etPassword.text.toString().trim()

                            // Check that no fields are empty
                            if (accountName.isEmpty()) {
                                etAccountName.error = "Enter Account Name"
                            } else if (username.isEmpty()) {
                                etUsername.error = "Enter Username/Email"
                            } else if (password2.isEmpty()) {
                                etPassword.error = "Enter Password"
                            } else {

                                try {
                                    val encryptedPassword =
                                        encrypt(password2.toByteArray(), keyPair.public)

                                    val newAccount = Account(
                                        id = account.id,
                                        accountName = accountName,
                                        username = username,
                                        password = encryptedPassword,
                                        timestamp = account.timestamp
                                    )

                                    // Update account data
                                    viewModel.updateAccount(newAccount)
                                } catch (e: Exception) {
                                    logD(e.message)
                                }

                            }
                        }
                    }
                }

                if (!showPasswordBottomSheet.isAdded) { // <- Repeated clicks can cause the app to crash.
                    showPasswordBottomSheet.show(
                        childFragmentManager, TAG
                    ) // show PasswordShow BottomSheet
                }


            }
        }
    }

    private fun collectAccounts() {
        lifecycleScope.launch {
            viewModel.accounts.collectLatest {
                when (it) {
                    is Response.Loading -> {
                        binding.progressBar.visible()
                    }

                    is Response.Success -> {
                        binding.progressBar.gone()
                        it.data?.collect { list ->
                            adapter.differ.submitList(list) // Set data to adapter
                        }
                    }

                    is Response.Error -> {
                        it.message?.let { e ->
                            binding.progressBar.gone()
                            toast(e)
                        }
                    }
                }
            }
        }
    }

    private fun collectAddAccountStatus() {
        lifecycleScope.launch {
            viewModel.addAccountStatus.collectLatest {
                when (it) {
                    is Response.Loading -> {

                    }

                    is Response.Success -> {
                        if (it.data == true) {
                            // Clear edittext
                            addNewBottomSheetBinding.apply {
                                etAccountName.text?.clear()
                                etUsername.text?.clear()
                                etPassword.text?.clear()
                            }
                            addNewBottomSheet.dismissNow() // Hide Bottom Sheet
                        }
                    }

                    is Response.Error -> {
                        it.message?.let { e ->
                            toast(e)
                        }
                    }
                }
            }
        }
    }

    private fun collectUpdateAccountStatus() {
        lifecycleScope.launch {
            viewModel.updateAccountStatus.collectLatest {
                when (it) {
                    is Response.Loading -> {

                    }

                    is Response.Success -> {
                        if (it.data == true) {
                            showPasswordBottomSheet.dismissNow()
                        }
                    }

                    is Response.Error -> {
                        it.message?.let { e ->
                            toast(e)
                        }
                    }
                }
            }
        }
    }

    private fun collectDeleteAccountStatus() {
        lifecycleScope.launch {
            viewModel.deleteAccountStatus.collectLatest {
                when (it) {
                    is Response.Loading -> {

                    }

                    is Response.Success -> {
                        if (it.data == true) {
                            showPasswordBottomSheet.dismissNow()
                        }
                    }

                    is Response.Error -> {
                        it.message?.let { e ->
                            toast(e)
                        }
                    }
                }
            }
        }
    }

}

