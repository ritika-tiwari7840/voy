package com.ritika.voy.authentication

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentCreateAccountBinding

class CreateAccount : BaseFragment() {
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)


        setupSpannableText()
        setupValidation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val screenHeight = resources.displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.203).toInt()


        val bottomSection: View = view.findViewById(R.id.bottomSection)
        val params = bottomSection.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = topMargin
        bottomSection.layoutParams = params

        navController = Navigation.findNavController(view)

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.loginFragment)
        }

        binding.signUpButton.setOnClickListener {
            navController.navigate(R.id.verifyEmailFragment)
        }

        binding.signedUp.setOnClickListener {
            navController.navigate(R.id.loginFragment)
        }
    }

    override fun onBackPressed() {
        navController.navigate(R.id.loginFragment)
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
        setupPasswordValidation(passwordEditText, passwordInputLayout)


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

    private fun setupPasswordValidation(editText: TextInputEditText, inputLayout: TextInputLayout) {
        val popupView = layoutInflater.inflate(R.layout.password_criteria_popup, null)
        val passwordPopup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
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
                        editText,
                        Gravity.NO_GRAVITY,
                        location[0],
                        editTextY - popupHeight
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
                    setValidState(inputLayout)
                    resetToDefault(inputLayout, editText)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) inputLayout.hint = ""
            }
        })
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
