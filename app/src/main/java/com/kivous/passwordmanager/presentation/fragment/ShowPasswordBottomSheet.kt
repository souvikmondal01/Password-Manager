package com.kivous.passwordmanager.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kivous.passwordmanager.databinding.FragmentPasswordShowBinding

class ShowPasswordBottomSheet(
    private val viewController: (FragmentPasswordShowBinding) -> Unit
) : BottomSheetDialogFragment() {
    private var _binding: FragmentPasswordShowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewController(binding)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "ShowPasswordBottomSheet"
    }
}