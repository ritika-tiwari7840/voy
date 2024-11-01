package com.ritika.voy.authentication

import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentResetPasswordBinding
class ResetPassword : BaseFragment() {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)

        setupValidation()

        return binding.root
    }

    private fun setupValidation() {
        // Password TextWatcher
        setupPasswordValidation(binding.newPassword, binding.newPasswordLabel)

        // New Password TextWatcher
        setupPasswordValidation(binding.newPassword, binding.newPasswordLabel)

        // Confirm Password TextWatcher
        val confirmPasswordEditText = binding.confirmPassword
        val confirmPasswordInputLayout = binding.confirmPasswordLabel

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPassword()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    confirmPasswordInputLayout.hint = ""
                }
            }
        })

        confirmPasswordEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                confirmPasswordInputLayout.hint = ""
            } else if (confirmPasswordEditText.text.isNullOrEmpty()) {
                confirmPasswordInputLayout.hint = getString(R.string.confirm_password)
            }
        }
    }

    private fun setupPasswordValidation(editText: TextInputEditText, inputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                when {
                    password.isEmpty() -> resetToDefault(inputLayout, editText)
                    !isValidPassword(password) -> setErrorState(
                        inputLayout,
                        "Password must contain 8 characters, including uppercase, lowercase, number, and special character",
                        editText
                    )
                    else -> setValidState(inputLayout)
                }
                validateConfirmPassword()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    inputLayout.hint = ""
                }
            }
        })

        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputLayout.hint = ""
            } else if (editText.text.isNullOrEmpty()) {
                inputLayout.hint = getString(R.string.enter_your_password)
            }
        }
    }

    private fun validateConfirmPassword() {
        val password = binding.newPassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val confirmPasswordInputLayout = binding.confirmPasswordLabel
        val confirmPasswordEditText = binding.confirmPassword

        when {
            confirmPassword.isEmpty() -> resetToDefault(confirmPasswordInputLayout, confirmPasswordEditText)

            confirmPassword != password -> setErrorState(
                confirmPasswordInputLayout, "Passwords do not match", confirmPasswordEditText
            )

            !isValidPassword(confirmPassword) -> setErrorState(
                confirmPasswordInputLayout,
                "Password must contain 8 characters, including uppercase, lowercase, number, and special character",
                confirmPasswordEditText
            )

            else -> setValidState(confirmPasswordInputLayout)
        }
    }

    private fun setErrorState(
        inputLayout: TextInputLayout,
        errorMessage: String,
        editText: TextInputEditText
    ) {
        inputLayout.helperText = errorMessage
        inputLayout.setHelperTextColor(
            ContextCompat.getColorStateList(
                requireContext(), R.color.red
            )
        )
        editText.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background_error)
    }

    private fun setValidState(inputLayout: TextInputLayout) {
        inputLayout.helperText = null
        inputLayout.setHelperTextColor(null)
    }

    private fun resetToDefault(inputLayout: TextInputLayout, editText: TextInputEditText) {
        inputLayout.helperText = null
        inputLayout.setHelperTextColor(null)
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.doneButton.setOnClickListener {
            Toast.makeText(requireContext(), "Password Changed Successfully", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener {
            navController.popBackStack()
        }
    }

    override fun onBackPressed() {
        navController.navigate(R.id.otpFragment)
    }
}
