package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentLoginBinding
import com.ritika.voy.api.ApiService
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.LoginRequest
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.ritika.voy.api.DataStoreManager

class LoginFragment : BaseFragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
                if (email.contains("@")) {
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

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT)
                    .show()
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
        Log.d("emailpassword", "Email: $email, Password: $password")
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
                    }
                    response.user?.let {
                        DataStoreManager.SaveUserData(
                            requireContext(),
                            it.id!!.toString(),
                            it.email!!,
                            it.first_name!!,
                            it.last_name!!,
                            it.full_name!!,
                            it.created_at!!,
                            it.phone_number!!,
                            it.gender!!.toString(),
                            it.emergency_contact_phone!!.toString(),
                            it.profile_photo!!.toString(),
                            it.rating_as_driver!!.toString(),
                            it.rating_as_passenger!!.toString(),
                            it.is_driver_verified!!.toString()
                        )
                    }

                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_loginFragment_to_homeActivity)
                } else {
                    Log.e("LoginFragment", "Error: ${response.message}")
                    Toast.makeText(
                        requireContext(),
                        "Invalid email or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: HttpException) {
                Log.e("LoginFragment", "Error: ${e.message()}")
                Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IOException) {
                Log.e("LoginFragment", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LoginFragment", "Error: ${e.message}")
                Toast.makeText(requireContext(), "An unexpected error occurred", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                progressDialog.dismiss()
                clearFields()
            }
        }
    }

    override fun onBackPressed() {
        navController.navigate(R.id.action_loginFragment_to_continueWithEmail)
    }
}
