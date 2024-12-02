package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentLoginBinding
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.LoginRequest
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ritika.voy.KeyboardUtils
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.dataclasses.GetUserResponse
import com.ritika.voy.api.dataclasses.LoginResponse

class LoginFragment : BaseFragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var bundle = Bundle()
    lateinit var keyboardUtils: KeyboardUtils
    private val TAG: String = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val scrollView = binding.scrollView
        keyboardUtils = KeyboardUtils(scrollView.id)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val screenHeight = resources.displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.304).toInt()


        val bottomSection: View = view.findViewById(R.id.bottomSection)
        val params = bottomSection.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = topMargin
        bottomSection.layoutParams = params

        val registerTextview = view.findViewById<TextView>(R.id.registerTextView)
        val registerText = "Don’t  have an account? Register instead"
        val spannable = SpannableString(registerText)
        spannable.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            22,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#7e60bf")),
            23,
            registerText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        registerTextview.text = spannable

        val passwordEditText = view.findViewById<EditText>(R.id.etPassword)
        val togglePasswordButton = view.findViewById<ImageButton>(R.id.btnTogglePassword)
        var isPasswordVisible = false

        togglePasswordButton.setOnClickListener {
            if (isPasswordVisible) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordButton.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordButton.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            isPasswordVisible = !isPasswordVisible
            passwordEditText.setSelection(passwordEditText.text.length)
        }


        val emailEditText = view.findViewById<EditText>(R.id.etEmail)
        val emailErrorTextView = view.findViewById<TextView>(R.id.tvEmailError)

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString()
                if (isValidEmail(email)) {
                    emailEditText.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)
                    emailErrorTextView.visibility = View.GONE
                } else if (email.isEmpty()) {
                    emailEditText.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)
                    emailErrorTextView.visibility = View.GONE
                } else {
                    emailEditText.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.edit_text_background_error
                    )
                    emailErrorTextView.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        navController = Navigation.findNavController(view)

        registerTextview.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_createAccount)
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_continueWithEmail)
        }

        binding.btnLogin.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty() || !isValidEmail(email)) {
                view?.let {
                    Snackbar.make(it, "Please fill all the fields correctly", Snackbar.LENGTH_LONG)
                        .show()
                }
            } else {
                login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        emailEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                emailEditText.hint = ""
            } else {
                emailEditText.hint = getString(R.string.enter_your_email)
            }
        }

        passwordEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordEditText.hint = ""
            } else {
                passwordEditText.hint = getString(R.string.enter_your_password)
            }
        }
    }

    private fun clearFields() {
        binding.etEmail.text?.clear()
        binding.etPassword.text?.clear()
    }

    private fun login(email: String, password: String) {
        Log.d(TAG, "Email: $email, Password: $password")
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {

                val response = RetrofitInstance.api.login(LoginRequest(email, password))
                if (response.success) {
                    response.tokens?.let {
                        DataStoreManager.saveTokens(requireContext(), it.access!!, it.refresh!!)
                        val userResponse = getUserData(it.access)
                        if (userResponse.success) {
                            Log.d(TAG, "User Details: ${userResponse.user}")
                            bundle = Bundle().apply {
                                putParcelable("user_details", userResponse.user)
                            }
                        }
                    }
                    view?.let {
                        Snackbar.make(it, "${response.message}", Snackbar.LENGTH_LONG).show()
                    }
                    navController.navigate(R.id.action_loginFragment_to_homeActivity, bundle)
                } else {
                    Log.e(TAG, "Error: ${response.message} ")
                    view?.let {
                        Snackbar.make(it, "${response.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val gson = Gson()
                            val errorResponse = gson.fromJson(
                                errorBody,
                                LoginResponse::class.java
                            ) // Replace with your response class
                            Log.e(TAG, "Bad Request: ${errorResponse.message}")

                            view?.let {
                                Snackbar.make(it, "${errorResponse.message}", Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        } catch (parseException: Exception) {
                            Log.e(
                                TAG,
                                "Error parsing response: ${parseException.message}",
                                parseException
                            )
                            view?.let {
                                Snackbar.make(
                                    it,
                                    "Invalid input, please check all fields",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    401 -> {
                        Log.e(TAG, "Unauthorized: ${e.message()}")
                        view?.let {
                            Snackbar.make(
                                it,
                                "Unauthorized access please check your credentials",
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
                    Snackbar.make(it, "Network error", Snackbar.LENGTH_LONG).show()
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
        navController.navigate(R.id.action_loginFragment_to_continueWithEmail)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun getUserData(accessToken: String): GetUserResponse {
        return RetrofitInstance.api.getUserData("Bearer $accessToken")
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

}
