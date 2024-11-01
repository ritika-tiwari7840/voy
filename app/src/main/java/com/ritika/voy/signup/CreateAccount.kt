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
import com.ritika.voy.authentication.ForgotPasswordFragment
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

        binding.signUpButton.setOnClickListener {
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
        // Email TextWatcher
        val emailEditText = binding.enterEmail
        val emailInputLayout = binding.emailLayout

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                when {
                    email.isEmpty() -> resetToDefault(emailInputLayout, emailEditText)
                    !isValidEmail(email) -> setErrorState(
                        emailInputLayout, "Invalid email format", emailEditText
                    )
                    else -> setValidState(emailInputLayout)
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
                emailInputLayout.helperText = "You’ll need to verify that you own this email."
            } else {
                if (emailEditText.text.isNullOrEmpty()) {
                    emailInputLayout.hint = getString(R.string.enter_your_email)
                }
                emailInputLayout.helperText = null
            }
        }

        // Password TextWatcher
        val passwordEditText = binding.password
        val passwordInputLayout = binding.passwordLayout

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                when {
                    password.isEmpty() -> resetToDefault(passwordInputLayout, passwordEditText)
                    !isValidPassword(password) -> setErrorState(
                        passwordInputLayout,
                        "Password must contain 8 characters, including uppercase, lowercase, number, and special character",
                        passwordEditText
                    )
                    else -> setValidState(passwordInputLayout)
                }
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

        // Confirm Password TextWatcher
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

        // Phone TextWatcher
        val phoneEditText = binding.phone
        val phoneInputLayout = binding.phoneLayout

        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString()
                when {
                    phone.isEmpty() -> resetToDefault(phoneInputLayout, phoneEditText)
                    !isValidPhone(phone) -> setErrorState(
                        phoneInputLayout, "Invalid phone number", phoneEditText
                    )
                    else -> setValidState(phoneInputLayout)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    phoneInputLayout.hint = ""
                }
            }
        })

        phoneEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                phoneInputLayout.hint = ""
            } else if (phoneEditText.text.isNullOrEmpty()) {
                phoneInputLayout.hint = getString(R.string.enter_your_phone)
            }
        }
    }

    private fun validateConfirmPassword() {
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val confirmPasswordInputLayout = binding.confirmPasswordLayout
        val confirmPasswordEditText = binding.confirmPassword

        when {
            confirmPassword.isEmpty() -> resetToDefault(
                confirmPasswordInputLayout, confirmPasswordEditText
            )
            confirmPassword != password -> setErrorState(
                confirmPasswordInputLayout, "Passwords do not match", confirmPasswordEditText
            )
            else -> setValidState(confirmPasswordInputLayout)
        }
    }

    private fun setErrorState(
        inputLayout: TextInputLayout,
        errorMessage: String,
        editText: TextInputEditText,
    ) {
        inputLayout.helperText = errorMessage
        inputLayout.setHelperTextColor(
            ContextCompat.getColorStateList(requireContext(), R.color.red)
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

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^\\d{10}$".toRegex() // Assuming a 10-digit phone number format
        return phone.matches(phoneRegex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
