package com.kivous.passwordmanager.presentation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivous.passwordmanager.R
import com.kivous.passwordmanager.databinding.FragmentAddNewBottomSheetBinding
import com.kivous.passwordmanager.databinding.FragmentHomeBinding
import com.kivous.passwordmanager.domain.model.Account
import com.kivous.passwordmanager.presentation.adapter.AccountAdapter
import com.kivous.passwordmanager.presentation.fragment.ShowPasswordBottomSheet.Companion.TAG
import com.kivous.passwordmanager.presentation.viewmodel.AccountViewModel
import com.kivous.passwordmanager.util.Extensions.getColor
import com.kivous.passwordmanager.util.Extensions.gone
import com.kivous.passwordmanager.util.Extensions.timeStamp
import com.kivous.passwordmanager.util.Extensions.toast
import com.kivous.passwordmanager.util.Extensions.visible
import com.kivous.passwordmanager.util.Password
import com.kivous.passwordmanager.util.Password.checkPasswordStrength
import com.kivous.passwordmanager.util.Password.generateStrongPassword
import com.kivous.passwordmanager.util.RSA.decrypt
import com.kivous.passwordmanager.util.RSA.encrypt
import com.kivous.passwordmanager.util.RSA.generateKeyPair
import com.kivous.passwordmanager.util.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.security.KeyPair
import java.util.concurrent.Executor

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
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        biometricStatusCheck()
        setUpBiometricAuthenticate()

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

                // Generate password and set to password edittext
                tvGeneratePassword.setOnClickListener {
                    val password = generateStrongPassword()
                    etPassword.setText(password)
                }

                // Check password strength and update UI accordingly
                tfPassword.editText?.doOnTextChanged { text: CharSequence?, _, _, _ ->

                    when (checkPasswordStrength(text.toString())) {

                        Password.PasswordStrength.EMPTY -> {
                            tvPasswordStrength.text = ""
                        }

                        Password.PasswordStrength.WEAK -> {
                            tvPasswordStrength.text = "Weak"
                            tvPasswordStrength.setTextColor(getColor(R.color.weak))
                        }

                        Password.PasswordStrength.MEDIUM -> {
                            tvPasswordStrength.text = "Medium"
                            tvPasswordStrength.setTextColor(getColor(R.color.medium))
                        }

                        Password.PasswordStrength.STRONG -> {
                            tvPasswordStrength.text = "Strong"
                            tvPasswordStrength.setTextColor(getColor(R.color.strong))
                        }

                    }
                }

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

                        val encryptedPassword =
                            encrypt(password.toByteArray(), keyPair.public) // Encrypt password

                        val account = Account(
                            accountName = accountName,
                            username = username,
                            password = encryptedPassword,
                            timestamp = timeStamp(),
                            privateKey = keyPair.private
                        )
                        // Add new account to database
                        viewModel.addAccount(account)

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

                // Decrypt password
                val decryptedPassword =
                    account.privateKey?.let { it1 -> decrypt(account.password!!, it1) }
                val password = java.lang.String(decryptedPassword)

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
                            val password = etPassword.text.toString().trim()

                            // Check that no fields are empty
                            if (accountName.isEmpty()) {
                                etAccountName.error = "Enter Account Name"
                            } else if (username.isEmpty()) {
                                etUsername.error = "Enter Username/Email"
                            } else if (password.isEmpty()) {
                                etPassword.error = "Enter Password"
                            } else {

                                // Encrypt password
                                val encryptedPassword =
                                    encrypt(password.toByteArray(), keyPair.public)

                                val newAccount = Account(
                                    id = account.id,
                                    accountName = accountName,
                                    username = username,
                                    password = encryptedPassword,
                                    timestamp = account.timestamp,
                                    privateKey = keyPair.private
                                )

                                // Update account data
                                viewModel.updateAccount(newAccount)

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
                        binding.recyclerView.gone()
                    }

                    is Response.Success -> {
                        binding.progressBar.gone()
                        binding.recyclerView.visible()
                        it.data?.collect { list ->
                            adapter.differ.submitList(list) // Set data to adapter
                        }
                    }

                    is Response.Error -> {
                        it.message?.let { e ->
                            binding.progressBar.gone()
                            binding.recyclerView.visible()
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

    @SuppressLint("SwitchIntDef")
    private fun biometricStatusCheck() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                toast("No biometric features available on this device.")
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                toast("Biometric features are currently unavailable.")
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                toast("No fingerprint assigned")
            }
        }
    }

    private fun setUpBiometricAuthenticate() {
        // Initialize Executor
        executor = ContextCompat.getMainExecutor(requireContext())

        // Initialize BiometricPrompt
        biometricPrompt = BiometricPrompt(
            this@HomeFragment,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    binding.cvRefresh.visible() // Refresh icon  will be visible on failure
                    binding.mainLayout.gone()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    binding.cvRefresh.gone()
                    binding.mainLayout.visible() // HomeScreen will be visible on success
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    binding.cvRefresh.visible()
                    binding.mainLayout.gone()
                }
            })

        // Build biometric authentication dialog
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify biometric")
            .setAllowedAuthenticators(
                BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
            )
            .build()
        /**
         *  BIOMETRIC_STRONG -> Fingerprint
         *  BIOMETRIC_WEAK -> Face unlock
         *  DEVICE_CREDENTIAL -> Pin, Password, Pattern etc
         */


        // Show biometric authentication dialog
        biometricPrompt.authenticate(promptInfo)

        binding.cvRefresh.setOnClickListener { // <- refresh icon onclick
            // Show biometric authentication dialog
            biometricPrompt.authenticate(promptInfo)
        }

    }

}

