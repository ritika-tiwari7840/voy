package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ritika.voy.BaseFragment
import com.ritika.voy.KeyboardUtils
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.ForgotRequest
import com.ritika.voy.api.dataclasses.ForgotResponse
import com.ritika.voy.api.dataclasses.LoginResponse
import com.ritika.voy.databinding.FragmentForgotPasswordBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ForgotPasswordFragment : BaseFragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val TAG: String = "ForgotPasswordFragment"
    lateinit var keyboardUtils: KeyboardUtils
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
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

        val emailEditText = view.findViewById<EditText>(R.id.etEmail)
        val emailErrorTextView = view.findViewById<TextView>(R.id.tvEmailError)

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString()
                if (email.contains("@") && isValidEmail(email)) {
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

        binding.btnContinue.setOnClickListener {
            val email = emailEditText.text.toString()
            val emailBundle = Bundle().apply {
                putString("email", email)
            }
            if (email.isNotEmpty() && isValidEmail(email)) {
                forgotpassword(email)
            } else {
                emailEditText.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.edit_text_background_error
                )
                emailErrorTextView.visibility = View.VISIBLE
            }
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }
    }

    private fun clearFields() {
        binding.etEmail.text?.clear()
    }


    private fun forgotpassword(email: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.forgotPassword(ForgotRequest(email))
                if (response.success) {
                    val emailBundle = Bundle().apply {
                        putString("email", email)
                    }
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG).show()
                    }
                    navController.navigate(
                        R.id.action_forgotPasswordFragment_to_otpFragment,
                        emailBundle
                    )
                } else {
                    view?.let {
                        Snackbar.make(it, "${response.errors}", Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val gson = Gson()
                            val errorResponse = gson.fromJson(
                                errorBody, ForgotResponse::class.java
                            ) // Replace with your response class
                            view?.let {
                                Snackbar.make(
                                    it,
                                    errorResponse.errors["email"]!!.get(0),
                                    Snackbar.LENGTH_LONG
                                )
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
        navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}