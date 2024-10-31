package com.ritika.voy.signup

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ritika.voy.R
import com.ritika.voy.ResetPassword
import com.ritika.voy.databinding.FragmentCreateAccountBinding

class CreateAccount : Fragment() {
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)

        setupSpannableText()
        setupValidation()

        binding.createAccountButton.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.main, ResetPassword())
                .addToBackStack(null).commit()
        }

        return binding.root
    }

    private fun setupSpannableText() {
        val signedUp: TextView = binding.signedUp
        val fullText = "Already a member? Log in"
        val loginText = "Log in"

        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(loginText)
        val end = start + loginText.length

        val termsColor = ContextCompat.getColor(requireContext(), R.color.theme_color)
        spannableString.setSpan(
            ForegroundColorSpan(termsColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        signedUp.text = spannableString
    }

    private fun setupValidation() {
        // Email field validation
        val emailEditText = binding.enterEmail
        val emailInputLayout = binding.email

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (!isValidEmail(email)) {
                    emailInputLayout.error = "Invalid email format"
                    emailInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    emailInputLayout.error = null
                    emailInputLayout.boxStrokeColor =
                        ContextCompat.getColor(requireContext(), R.color.theme_color)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    emailInputLayout.hint = ""
                }
            }
        })

        emailEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                emailInputLayout.hint = ""
            } else if (emailEditText.text.isNullOrEmpty()) {
                emailInputLayout.hint = getString(R.string.enter_your_email)
            }
        }

        // Password field validation
        val passwordEditText = binding.password
        val passwordInputLayout = binding.passwordLayout

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
                // Validate confirm password if it's already entered
                validateConfirmPassword()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    passwordInputLayout.hint = ""
                }
            }
        })

        passwordEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordInputLayout.hint = ""
            } else if (passwordEditText.text.isNullOrEmpty()) {
                passwordInputLayout.hint = getString(R.string.enter_your_password)
            }
        }

        // Confirm Password field validation
        val confirmPasswordEditText = binding.confirmPassword
        val confirmPasswordInputLayout = binding.confirmPasswordLayout

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

    private fun validateConfirmPassword() {
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val confirmPasswordInputLayout = binding.confirmPasswordLayout

        if (confirmPassword.isNotEmpty() && confirmPassword != password) {
            confirmPasswordInputLayout.error = "Passwords do not match"
            confirmPasswordInputLayout.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
        } else {
            confirmPasswordInputLayout.error = null
            confirmPasswordInputLayout.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.theme_color)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
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
