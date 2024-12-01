package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.ritika.voy.BaseFragment
import com.ritika.voy.KeyboardUtils
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.ForgotResponse
import com.ritika.voy.api.dataclasses.ResetRequest
import com.ritika.voy.api.dataclasses.ResetResponse
import com.ritika.voy.databinding.FragmentResetPasswordBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class ResetPassword : BaseFragment() {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    lateinit var keyboardUtils: KeyboardUtils
    private lateinit var navController: NavController
    private lateinit var passwordPopup: PopupWindow
    private val TAG: String = "ResetPassword"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        setupValidation()
        val scrollView = binding.scrollView
        keyboardUtils = KeyboardUtils(scrollView.id)
        return binding.root
    }

    private fun setupValidation() {
        setupPasswordValidation(binding.newPassword, binding.newPasswordLabel)

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
                if (::passwordPopup.isInitialized && passwordPopup.isShowing) {
                    passwordPopup.dismiss()
                }
            } else if (confirmPasswordEditText.text.isNullOrEmpty()) {
                confirmPasswordInputLayout.hint = getString(R.string.confirm_password)
            }
        }
    }

    private fun setupPasswordValidation(
        passwordEditText: TextInputEditText,
        passwordInputLayout: TextInputLayout,
    ) {
        val passwordCriteriaLayout = binding.passwordCriteriaLayout


        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordInputLayout.hint = ""
                passwordCriteriaLayout.visibility = View.VISIBLE
            } else {
                passwordCriteriaLayout.visibility = View.GONE
                if (passwordEditText.text.isNullOrEmpty()) {
                    passwordInputLayout.hint = getString(R.string.enter_your_password)
                }
            }
        }


        val unmetColor = ContextCompat.getColor(requireContext(), R.color.white)
        val metColor = ContextCompat.getColor(requireContext(), R.color.green)

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                binding.criteriaLength.setTextColor(if (password.length >= 8) metColor else unmetColor)
                binding.criteriaUpper.setTextColor(if (password.any { it.isUpperCase() }) metColor else unmetColor)
                binding.criteriaLower.setTextColor(if (password.any { it.isLowerCase() }) metColor else unmetColor)
                binding.criteriaDigit.setTextColor(if (password.any { it.isDigit() }) metColor else unmetColor)
                binding.criteriaSpecial.setTextColor(if (password.any { !it.isLetterOrDigit() }) metColor else unmetColor)

                val isPasswordValid = isValidPassword(password)
                if (isPasswordValid) {
                    passwordCriteriaLayout.visibility = View.GONE
                    setValidState(passwordInputLayout, passwordEditText)
                    resetToDefault(passwordInputLayout, passwordEditText)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    passwordInputLayout.hint = ""
                }
                if (s.isNullOrEmpty() && !passwordEditText.hasFocus()) {
                    passwordInputLayout.hint = getString(R.string.enter_your_password)
                }
            }
        })
    }

    private fun validateConfirmPassword() {
        val password = binding.newPassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val confirmPasswordInputLayout = binding.confirmPasswordLabel
        val confirmPasswordEditText = binding.confirmPassword
        val newPasswordInputLayout = binding.newPasswordLabel

        val errorColor = ContextCompat.getColor(requireContext(), R.color.red)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)
        val newPasswordValidColor = ContextCompat.getColor(requireContext(), R.color.theme_color)

        when {
            confirmPassword.isEmpty() -> {
                resetToDefault(confirmPasswordInputLayout, confirmPasswordEditText)
                confirmPasswordInputLayout.boxStrokeColor = defaultColor
                newPasswordInputLayout.boxStrokeColor = defaultColor
            }

            confirmPassword != password -> {
                setErrorState(
                    confirmPasswordInputLayout,
                    "Passwords do not match",
                    confirmPasswordEditText
                )
                confirmPasswordInputLayout.boxStrokeColor = errorColor
                newPasswordInputLayout.boxStrokeColor = defaultColor
            }

            !isValidPassword(confirmPassword) -> {
                setErrorState(
                    confirmPasswordInputLayout,
                    "Password must contain 8 characters, including uppercase, lowercase, number, and special character",
                    confirmPasswordEditText
                )
                confirmPasswordInputLayout.boxStrokeColor = errorColor
                newPasswordInputLayout.boxStrokeColor = defaultColor
            }

            else -> {
                setValidState(confirmPasswordInputLayout, confirmPasswordEditText)
                confirmPasswordInputLayout.boxStrokeColor = newPasswordValidColor
                newPasswordInputLayout.boxStrokeColor = newPasswordValidColor
            }
        }
    }

    private fun setErrorState(
        inputLayout: TextInputLayout,
        errorMessage: String,
        editText: TextInputEditText,
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

    private fun setValidState(inputLayout: TextInputLayout, editText: TextInputEditText) {
        inputLayout.helperText = null
        inputLayout.setHelperTextColor(null)
        editText.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)

    }

    private fun resetToDefault(inputLayout: TextInputLayout, editText: TextInputEditText) {
        inputLayout.helperText = null
        inputLayout.setHelperTextColor(null)
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::passwordPopup.isInitialized && passwordPopup.isShowing) {
            passwordPopup.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.doneButton.setOnClickListener {

            val newPassword = binding.newPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()
            val otp = arguments?.getString("otp").toString()
            val email = arguments?.getString("email").toString()
            resetPassword(email, otp, newPassword, confirmPassword)
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_resetPassword_to_forgotPasswordFragment)
        }
    }

    private fun clearFields() {
        binding.newPassword.text?.clear()
        binding.confirmPassword.text?.clear()
    }

    private fun resetPassword(
        email: String,
        otp: String,
        new_password: String,
        confirm_password: String,
    ) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.resetPassword(
                    ResetRequest(
                        email,
                        otp,
                        new_password,
                        confirm_password
                    )
                )
                if (response.success) {
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG)
                            .show()
                    }
                    navController.navigate(R.id.action_resetPassword_to_loginFragment)
                } else {
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val gson = Gson()
                            val errorResponse = gson.fromJson(
                                errorBody, ResetResponse::class.java
                            ) // Replace with your response class
                            view?.let {
                                Snackbar.make(it, "${errorResponse.message}", Snackbar.LENGTH_LONG)
                                    .show()
                            }
                            Log.e(TAG, "${errorResponse.errors}")
                        } catch (parseException: Exception) {
                            Log.e(
                                TAG,
                                "Error parsing response: ${parseException.message}",
                                parseException
                            )
                            view?.let {
                                Snackbar.make(it, "${parseException.message}", Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }

                    401 -> {
                        Log.e(TAG, "Unauthorized: ${e.message()}")
                        view?.let {
                            Snackbar.make(
                                it,
                                "Unauthorized access, Sign Up please",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    else -> {
                        Log.e(TAG, "HTTP Error: ${e.code()} - ${e.message()}")
                        view?.let {
                            Snackbar.make(it, "${e.message}", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error: ${e.message}")
                view?.let {
                    Snackbar.make(it, "Check your network connection please", Snackbar.LENGTH_LONG)
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                view?.let {
                    Snackbar.make(it, "${e.message}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                progressDialog.dismiss()
                clearFields()
            }
        }
    }

    override fun onBackPressed() {
        navController.navigate(R.id.action_resetPassword_to_forgotPasswordFragment)
    }
}
