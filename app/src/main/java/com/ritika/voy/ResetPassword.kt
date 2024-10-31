package com.ritika.voy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ritika.voy.databinding.FragmentResetPasswordBinding

class ResetPassword : Fragment() {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)

        setupValidation()

        return binding.root
    }

    private fun setupValidation() {
        // Password field validation
        val passwordEditText = binding.password
        val passwordInputLayout = binding.newPasswordLabel

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (!isValidPassword(password)) {
                    passwordInputLayout.error =
                        "Password must contain 8 characters, including uppercase, lowercase, number, and special character"
                    passwordInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    passwordInputLayout.error = null
                    passwordInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.theme_color)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    passwordInputLayout.hint = ""
                }

            }
        })

        // Confirm Password field validation
        val confirmPasswordEditText = binding.confirmPassword
        val confirmPasswordInputLayout = binding.confirmPasswordLabel

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = passwordEditText.text.toString()
                if (confirmPassword != password) {
                    confirmPasswordInputLayout.error = "Passwords do not match"
                    confirmPasswordInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    confirmPasswordInputLayout.error = null
                    confirmPasswordInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.theme_color)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    confirmPasswordInputLayout.hint = ""
                }
            }
        })
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
}
