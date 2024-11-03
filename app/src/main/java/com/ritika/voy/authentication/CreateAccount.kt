package com.ritika.voy.authentication

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.api.AuthService
import com.ritika.voy.api.dataclasses.SignUpRequest
import com.ritika.voy.api.dataclasses.SignUpResponse
import com.ritika.voy.api.datamodels.UserPreferences
import com.ritika.voy.databinding.FragmentCreateAccountBinding
import kotlinx.coroutines.launch
import retrofit2.Call


class CreateAccount : BaseFragment() {
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var userPreferences: UserPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        setupSpannableText()
        setupValidation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences.getInstance(requireContext())

        val screenHeight = resources.displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.203).toInt()

        val bottomSection: View = view.findViewById(R.id.bottomSection)
        val params = bottomSection.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = topMargin
        bottomSection.layoutParams = params

        navController = Navigation.findNavController(view)

        binding.signUpButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val existingToken = userPreferences.getUserToken()
                    if (existingToken != null) {
                        Toast.makeText(
                            requireContext(),
                            "You are already registered. Redirecting to verification of email",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate(R.id.verifyEmailFragment)
                    } else if (areAllFieldsValid()) {
                        val email = binding.enterEmail.text.toString()
                        val password = binding.password.text.toString()
                        val confirmPassword = binding.confirmPassword.text.toString()
                        val phone = binding.phone.text.toString()
                        apiCall(confirmPassword, email, password, phone)

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please correct the errors in the form",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    // Handle any potential issues with fetching token or other errors
                    Toast.makeText(
                        requireContext(),
                        "$e An error occurred. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("CreateAccount", "Error: ${e.message}", e)
                }
            }
        }
    }

    override fun onBackPressed() {
        navController.navigate(R.id.loginFragment)
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
        val authService = Retrofit.api.create(AuthService::class.java)
        authService.signUp(requestBody).enqueue(object : retrofit2.Callback<SignUpResponse> {
            override fun onResponse(
                call: Call<SignUpResponse>,
                response: retrofit2.Response<SignUpResponse>,
            ) {
                if (response.isSuccessful) {
                    val signUpResponse = response.body()
                    if (signUpResponse != null && signUpResponse.success) {
                        // User signed up successfully
                        Toast.makeText(
                            context,
                            "User profile created successfully! Token: ${signUpResponse.temp_user_id}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Save the token using UserPreferences
                        lifecycleScope.launch {
                            userPreferences.saveUserToken(signUpResponse.temp_user_id ?: "")
                            // Navigate only after saving the token
                            navController.navigate(R.id.verifyEmailFragment)
                        }

                    } else {
                        // Handle error message returned from the server
                        Toast.makeText(
                            context,
                            signUpResponse?.message ?: "Sign-up failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Handle failure to get a successful response
                    Toast.makeText(
                        context,
                        "Sign-up failed with error code: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                // Handle failure to execute the call
                Log.e("API Error", "Network error: ${t.message}", t)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun areAllFieldsValid(): Boolean {
        val email = binding.enterEmail.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val phone = binding.phone.text.toString()

        val emailValid = isValidEmail(email)
        val passwordValid = isValidPassword(password)
        val confirmPasswordValid = confirmPassword == password // Confirm password must match
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
    }

    private fun setupValidation() {
        // Email TextWatcher
        val emailEditText = binding.enterEmail
        val emailInputLayout = binding.emailLayout
        setupEmailValidation(emailEditText, emailInputLayout)

        // Password TextWatcher
        val passwordEditText = binding.password
        val passwordInputLayout = binding.passwordLayout
        setupPasswordValidation(passwordEditText, passwordInputLayout)


        // Confirm Password TextWatcher
        val confirmPasswordEditText = binding.confirmPassword
        val confirmPasswordInputLayout = binding.confirmPasswordLayout
        setupConfirmPasswordValidation(confirmPasswordEditText, confirmPasswordInputLayout)

        // Phone TextWatcher
        val phoneEditText = binding.phone
        val phoneInputLayout = binding.phoneLayout
        setupPhoneValidation(phoneEditText, phoneInputLayout)
    }

    private fun setupPhoneValidation(
        phoneEditText: TextInputEditText,
        phoneInputLayout: TextInputLayout,
    ) {
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString()
                when {
                    phone.isEmpty() -> resetToDefault(phoneInputLayout, phoneEditText)
                    !isValidPhone(phone) -> setErrorState(
                        phoneInputLayout, "Invalid phone number", phoneEditText
                    )

                    else -> setValidState(phoneInputLayout, phoneEditText)

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

    private fun setupEmailValidation(
        emailEditText: TextInputEditText,
        emailInputLayout: TextInputLayout,
    ) {
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                when {
                    email.isEmpty() -> resetToDefault(emailInputLayout, emailEditText)
                    !isValidEmail(email) -> setErrorState(
                        emailInputLayout, "Invalid email format", emailEditText
                    )

                    else -> setValidState(emailInputLayout, emailEditText)
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
            } else {
                if (emailEditText.text.isNullOrEmpty()) {
                    emailInputLayout.hint = getString(R.string.enter_your_email)
                }
                emailInputLayout.helperText = null
            }
        }
    }

    private fun setupPasswordValidation(editText: TextInputEditText, inputLayout: TextInputLayout) {
        val popupView = layoutInflater.inflate(R.layout.password_criteria_popup, null)
        val passwordPopup = PopupWindow(
            popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        passwordPopup.isOutsideTouchable = false
        passwordPopup.isFocusable = false

        val criteriaLength = popupView.findViewById<TextView>(R.id.criteria_length)
        val criteriaUpper = popupView.findViewById<TextView>(R.id.criteria_upper)
        val criteriaLower = popupView.findViewById<TextView>(R.id.criteria_lower)
        val criteriaDigit = popupView.findViewById<TextView>(R.id.criteria_digit)
        val criteriaSpecial = popupView.findViewById<TextView>(R.id.criteria_special)

        val unmetColor = ContextCompat.getColor(requireContext(), R.color.black)
        val metColor = ContextCompat.getColor(requireContext(), R.color.green)

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editText.post {
                    // Calculate the position of the EditText
                    val location = IntArray(2)
                    editText.getLocationOnScreen(location)
                    val editTextY = location[1]

                    // Calculate the height of the popup
                    popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    val popupHeight = popupView.measuredHeight

                    // Show the popup above the EditText
                    passwordPopup.width = editText.width
                    passwordPopup.showAtLocation(
                        editText, Gravity.NO_GRAVITY, location[0], editTextY - popupHeight
                    )
                }
            } else {
                passwordPopup.dismiss()
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()

                criteriaLength.setTextColor(if (password.length >= 8) metColor else unmetColor)
                criteriaUpper.setTextColor(if (password.any { it.isUpperCase() }) metColor else unmetColor)
                criteriaLower.setTextColor(if (password.any { it.isLowerCase() }) metColor else unmetColor)
                criteriaDigit.setTextColor(if (password.any { it.isDigit() }) metColor else unmetColor)
                criteriaSpecial.setTextColor(if (password.any { !it.isLetterOrDigit() }) metColor else unmetColor)

                val isPasswordValid = isValidPassword(password)
                if (isPasswordValid) {
                    passwordPopup.dismiss()
                    setValidState(inputLayout, editText)
                    resetToDefault(inputLayout, editText)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) inputLayout.hint = ""
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
        val confirmPasswordEditText = binding.confirmPassword

        when {
            confirmPassword.isEmpty() -> resetToDefault(
                confirmPasswordInputLayout, confirmPasswordEditText
            )

            confirmPassword != password -> setErrorState(
                confirmPasswordInputLayout, "Passwords do not match", confirmPasswordEditText
            )

            else -> setValidState(confirmPasswordInputLayout, confirmPasswordEditText)
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

    private fun setValidState(inputLayout: TextInputLayout, editText: TextInputEditText) {
        inputLayout.helperText = null
        inputLayout.setHelperTextColor(null)
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner)
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
        val passwordRegex =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
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
