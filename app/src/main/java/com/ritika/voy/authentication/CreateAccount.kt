package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.ritika.voy.api.ApiService
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.SignUpErrorResponse
import com.ritika.voy.api.dataclasses.SignUpRequest
import com.ritika.voy.api.dataclasses.SignUpResponse
import com.ritika.voy.api.dataclasses.resendPhoneRequest
import com.ritika.voy.databinding.FragmentCreateAccountBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException


class CreateAccount : BaseFragment() {
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private val TAG = "CreateAccount"
    lateinit var keyboardUtils: KeyboardUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        setupSpannableText()
        setupValidation()

        val scrollView = binding.scrollView
        keyboardUtils = KeyboardUtils(scrollView.id)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val screenHeight = resources.displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.203).toInt()
        val bottomSection: View = view.findViewById(R.id.bottomSection)
        val params = bottomSection.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = topMargin
        bottomSection.layoutParams = params

        binding.signUpButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    if (areAllFieldsValid()) {
                        val email = binding.enterEmail.text.toString()
                        val password = binding.password.text.toString()
                        val confirmPassword = binding.confirmPassword.text.toString()
                        val phone = binding.phone.text.toString().removePrefix("+91 ")
                        apiCall(confirmPassword, email, password, phone)
                    } else {
                        view?.let {
                            Snackbar.make(
                                it,
                                "Please fill all fields correctly",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    view?.let {
                        Snackbar.make(it, "${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                    Log.e(TAG, "onApiCall ${e.message}:", e)
                }
            }
        }
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_createAccount_to_loginFragment)
        }
    }

    override fun onBackPressed() {
        navController.navigate(R.id.action_createAccount_to_loginFragment)
    }


    private fun clearFields() {
        binding.enterEmail.text?.clear()
        binding.password.text?.clear()
        binding.confirmPassword.text?.clear()
        binding.phone.text?.clear()
        binding.phone.setText("+91 ")
        binding.phone.setSelection(binding.phone.text?.length ?: 0)
    }

    private fun apiCall(
        confirmPassword: String,
        email: String,
        password: String,
        phoneNumber: String,
    ) {
        val requestBody = SignUpRequest(
            confirm_password = confirmPassword,
            email = email,
            password = password,
            phone_number = phoneNumber
        )
        val authService = Retrofit.api.create(ApiService::class.java)

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                authService.signUp(requestBody)
                    .enqueue(object : retrofit2.Callback<SignUpResponse> {
                        override fun onResponse(
                            call: Call<SignUpResponse>,
                            response: retrofit2.Response<SignUpResponse>,
                        ) {
                            progressDialog.dismiss()
                            if (response.isSuccessful) {
                                response.body()?.let { signUpResponse ->
                                    if (signUpResponse.success) {
                                        view?.let {
                                            Snackbar.make(
                                                it,
                                                "Registration initiated please verify your email and phone number",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                        }
                                        val bundle = Bundle().apply {
                                            putString("email", email)
                                            putString("phoneNumber", phoneNumber)
                                            putString(
                                                "user_id",
                                                signUpResponse.registration_status.user_id.toString()
                                            )
                                        }
                                        navController.navigate(
                                            R.id.action_createAccount_to_verifyEmailFragment, bundle
                                        )
                                    } else {
                                        handleUserExist(response)
                                    }
                                }
                            } else {
                                handleUserExist(response)
                            }
                        }

                        override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                            progressDialog.dismiss()
                            Log.e(TAG, "Network error: ${t.message}", t)
                            view?.let {
                                Snackbar.make(it, "Network error", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    })
            } catch (e: HttpException) {
                progressDialog.dismiss()
                when (e.code()) {
                    400 -> {
                        Log.e("CreateAccount", "Bad Request: ${e.message()}", e)
                        view?.let {
                            Snackbar.make(
                                it,
                                "Invalid inputs, please check your details",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    404 -> {
                        Log.e("CreateAccount", "Not Found: ${e.message()}", e)
                        view?.let {
                            Snackbar.make(
                                it,
                                "Please try again after sometime",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    409 -> {
                        Log.e("CreateAccount", "Conflict: ${e.message()}", e)
                        view?.let {
                            Snackbar.make(
                                it,
                                "Conflict occur, email and phone number may already be in use",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    else -> {
                        Log.e("CreateAccount", "HTTP Error: ${e.code()} - ${e.message()}", e)
                        view?.let {
                            Snackbar.make(it, "Please try again", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                progressDialog.dismiss()
                Log.e("CreateAccount", "Network Error: ${e.message}", e)
                view?.let {
                    Snackbar.make(it, "Network error", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Log.e("CreateAccount", "Unexpected Error: ${e.message}", e)
                view?.let {
                    Snackbar.make(
                        it,
                        "Something went wrong please try again later",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } finally {
//                clearFields()
            }
        }
    }

    private fun handleUserExist(response: retrofit2.Response<SignUpResponse>) {
        response.body()?.registration_status?.user_id?.let {
            val email = binding.enterEmail.text.toString()
            val phoneNumber = binding.phone.text.toString().removePrefix("+91 ")
            val bundle = Bundle().apply {
                putString("email", email)
                putString("phoneNumber", phoneNumber)
                putString("user_id", it.toString())
            }
            Log.e(TAG, "handleUserExist: ${response.code()}")
            if (response.code() == 409) {
                if (response.body()?.registration_status?.verification_status?.email_verified == false) {
                    view?.let {
                        Snackbar.make(
                            it,
                            "You have already registered please verify your email address",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    navController.navigate(R.id.action_createAccount_to_verifyEmailFragment, bundle)
                } else if (response.body()?.registration_status?.verification_status?.phone_verified == false) {
                    resendPhone(phoneNumber)
                    view?.let {
                        Snackbar.make(
                            it,
                            "You have already registered please verify your phone number",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    navController.navigate(R.id.action_createAccount_to_verifyPhoneFragment, bundle)

                } else {
                    setErrorState(
                        binding.emailLayout, "user exist with this email", binding.enterEmail
                    )
                    setErrorState(
                        binding.phoneLayout, "user exist with this phone number", binding.phone
                    )
                }
            }
        } ?: run {
            val errorResponse = response.errorBody()?.string()
            Log.e("CreateAccount", "Error Response: $errorResponse")

            val gson = Gson()
            val errorData = gson.fromJson(errorResponse, SignUpErrorResponse::class.java)

            if (errorData?.message == "Registration already in progress") {
                Log.d(TAG, "handleUserExist: Registration in progress")
                val email = binding.enterEmail.text.toString()
                val phoneNumber = binding.phone.text.toString().removePrefix("+91 ")
                val bundle = Bundle().apply {
                    putString("email", email)
                    putString("phoneNumber", phoneNumber)
                    putString("user_id", errorData.registration_status?.user_id.toString())
                }
                val registrationStatus = errorData.registration_status
                if (registrationStatus != null) {
                    if (registrationStatus.email_verified == "False") {
                        view?.let {
                            Snackbar.make(
                                it,
                                "You have already registered please verify your email address",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        Log.d(TAG, "Navigating to VerifyEmailFragment")
                        navController.navigate(
                            R.id.action_createAccount_to_verifyEmailFragment, bundle
                        )
                    } else if (registrationStatus.phone_verified == "False") {
                        Log.d(TAG, "Navigating to VerifyPhoneFragment")
                        view?.let {
                            Snackbar.make(
                                it,
                                "You have already registered please verify your phone number",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        resendPhone(phoneNumber)
                        navController.navigate(
                            R.id.action_createAccount_to_verifyPhoneFragment, bundle
                        )
                    } else {
                        view?.let {
                            Snackbar.make(
                                it,
                                "Verification steps known please try again",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    view?.let {
                        Snackbar.make(it, "Please register yourself again", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            } else {
                Log.d(TAG, "handleUserExist: User with these credentials already exists $errorData")
                if (errorData.errors.containsKey("email")) {
                    setErrorState(
                        binding.emailLayout,
                        errorData.errors["email"]?.get(0).toString(),
                        binding.enterEmail
                    )
                    view?.let {
                        Snackbar.make(
                            it,
                            errorData.errors["email"]?.get(0).toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else if (errorData.errors.containsKey("phone_number")) {
                    setErrorState(
                        binding.phoneLayout,
                        errorData.errors["phone_number"]?.get(0).toString(),
                        binding.phone
                    )
                    view?.let {
                        Snackbar.make(
                            it,
                            errorData.errors["phone_number"]?.get(0).toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else if (errorData.errors.containsKey("password")) {
                    setErrorState(
                        binding.passwordLayout,
                        errorData.errors["password"]?.get(0).toString(),
                        binding.password
                    )
                    view?.let {
                        Snackbar.make(
                            it,
                            errorData.errors["password"]?.get(0).toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    view?.let {
                        Snackbar.make(it, "Please try again", Snackbar.LENGTH_LONG).show()
                    }
                }
            }

        }
    }

    private fun areAllFieldsValid(): Boolean {
        val email = binding.enterEmail.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val phone = binding.phone.text.toString().removePrefix("+91 ")

        val emailValid = isValidEmail(email)
        val passwordValid = isValidPassword(password)
        val confirmPasswordValid = confirmPassword == password
        val phoneValid = isValidPhone(phone)

        if (!emailValid) {
            setErrorState(binding.emailLayout, "Invalid email format", binding.enterEmail)
        }
        if (!passwordValid) {
            setErrorState(
                binding.passwordLayout, "Password does not meet criteria", binding.password
            )
        }
        if (!confirmPasswordValid) {
            setErrorState(
                binding.confirmPasswordLayout, "Passwords do not match", binding.confirmPassword
            )
        }
        if (!phoneValid) {
            setErrorState(binding.phoneLayout, "Invalid phone number", binding.phone)
        }
        return emailValid && passwordValid && confirmPasswordValid && phoneValid
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
        binding.signedUp.setOnClickListener() {
            navController.navigate(R.id.action_createAccount_to_loginFragment)
        }
    }

    private fun setupValidation() {
        setupEmailValidation(binding.enterEmail, binding.emailLayout)
        setupPasswordValidation(binding.password, binding.passwordLayout)
        setupConfirmPasswordValidation(binding.confirmPassword, binding.confirmPasswordLayout)
        setupPhoneValidation(binding.phone, binding.phoneLayout)
    }

    private fun setupPhoneValidation(
        phoneEditText: TextInputEditText,
        phoneInputLayout: TextInputLayout,
    ) {
        phoneEditText.filters = arrayOf(InputFilter.LengthFilter(14))
        phoneEditText.setText("+91 ")
        phoneEditText.setSelection(phoneEditText.text?.length ?: 0)

        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().removePrefix("+91 ")
                when {
                    phone.isEmpty() -> resetToDefault(
                        phoneInputLayout, phoneEditText
                    )

                    !isValidPhone(phone) -> setErrorState(
                        phoneInputLayout, getString(R.string.invalid_phone_format), phoneEditText
                    )

                    else -> setValidState(
                        phoneInputLayout, getString(R.string.phone_helper_text), phoneEditText
                    )
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int,
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Ensure +91 prefix is always present
                if (!s.isNullOrEmpty() && !s.startsWith("+91 ")) {
                    phoneEditText.setText("+91 ${s.toString().removePrefix("+91 ")}")
                    phoneEditText.setSelection(phoneEditText.text?.length ?: 0)
                }

                // Prevent deletion of the "+91 " prefix
                if (s.isNullOrEmpty() || s.length < 4) {
                    phoneEditText.setText("+91 ")
                    phoneEditText.setSelection(phoneEditText.text?.length ?: 0)
                }
            }
        })
    }

    private fun setupEmailValidation(
        emailEditText: TextInputEditText,
        emailInputLayout: TextInputLayout,
    ) {
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                when {
                    email.isEmpty() -> resetToDefault(
                        emailInputLayout, emailEditText
                    )

                    !isValidEmail(email) -> setErrorState(
                        emailInputLayout, getString(R.string.invalid_email_format), emailEditText
                    )

                    else -> setValidState(
                        emailInputLayout, getString(R.string.email_helper_text), emailEditText
                    )
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {
            }

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
    }

    private fun setupPasswordValidation(
        passwordEditText: TextInputEditText,
        passwordInputLayout: TextInputLayout,
    ) {

        val passwordEditText = binding.password
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

                val passwordCriteria = mapOf(
                    binding.criteriaLength to (password.length >= 8),
                    binding.criteriaUpper to password.any { it.isUpperCase() },
                    binding.criteriaLower to password.any { it.isLowerCase() },
                    binding.criteriaSpecial to password.any { it in "@\$!%*?&" && !it.isDigit() },
                    binding.criteriaDigit to password.any { it.isDigit() }
                )
                passwordCriteria.forEach { (criteriaView, isMet) ->
                    criteriaView.setTextColor(if (isMet) metColor else unmetColor)
                }
                val isPasswordValid = isValidPassword(password)
                if (isPasswordValid) {
                    passwordCriteriaLayout.visibility = View.GONE
                    setValidState(passwordInputLayout, "", passwordEditText)
                    resetToDefault(passwordInputLayout, passwordEditText)
                } else {
                    passwordCriteriaLayout.visibility = View.VISIBLE
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

    private fun setupConfirmPasswordValidation(
        confirmPasswordEditText: TextInputEditText,
        confirmPasswordInputLayout: TextInputLayout,
    ) {

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPassword()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {
            }

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
        val confirmPasswordEditText = binding.confirmPassword

        when {
            confirmPassword.isEmpty() -> resetToDefault(
                confirmPasswordInputLayout, confirmPasswordEditText
            )

            confirmPassword != password -> setErrorState(
                confirmPasswordInputLayout,
                getString(R.string.password_mismatch),
                confirmPasswordEditText
            )

            else -> setValidState(confirmPasswordInputLayout, "", confirmPasswordEditText)
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

    private fun setValidState(
        inputLayout: TextInputLayout,
        helperText: String,
        editText: TextInputEditText,
    ) {
        inputLayout.helperText = null
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner)
        inputLayout.helperText = helperText
        inputLayout.setHelperTextColor(requireContext().getColorStateList(R.color.helper_text))
    }

    private fun resetToDefault(
        inputLayout: TextInputLayout,
        editText: TextInputEditText,
    ) {
        inputLayout.helperText = null
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^\\d{10}$".toRegex()
        return phone.matches(phoneRegex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }


    private fun resendPhone(phone_number: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                Log.e("VerifyPhoneFragment", "Phone number: $phone_number")
                val response = RetrofitInstance.api.resendPhoneOtp(resendPhoneRequest(phone_number))
                if (response.success) {
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("VerifyPhoneFragment", "Error: ${e.message}")
                view?.let {
                    Snackbar.make(it, e.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            } finally {
                progressDialog.dismiss()
            }
        }
    }

}
