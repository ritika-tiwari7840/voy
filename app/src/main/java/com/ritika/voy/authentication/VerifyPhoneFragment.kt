package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ritika.voy.BaseFragment
import com.ritika.voy.KeyboardUtils
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.EmailVerifyResponse
import com.ritika.voy.api.dataclasses.GetUserResponse
import com.ritika.voy.api.dataclasses.PhoneVerifyRequest
import com.ritika.voy.api.dataclasses.phoneVerifyResponseX
import com.ritika.voy.api.dataclasses.resendPhoneRequest
import com.ritika.voy.databinding.FragmentVerifyPhoneBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.http.Tag
import java.io.IOException
import kotlin.math.log

class VerifyPhoneFragment : BaseFragment() {
    private var _binding: FragmentVerifyPhoneBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var resendTimer: CountDownTimer
    lateinit var keyboardUtils: KeyboardUtils
    private var TAG: String = "VerifyPhoneFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentVerifyPhoneBinding.inflate(inflater, container, false)

        val scrollView: ScrollView = binding.scrollView
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

        val resendTextview = view.findViewById<TextView>(R.id.resendTextView)
        val resendText = "Didn’t receive any code? Resend Code"
        startResendTimer()
        val spannable = SpannableString(resendText)
        spannable.setSpan(
            ForegroundColorSpan(Color.WHITE), 0, 24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#7e60bf")),
            25,
            resendText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            UnderlineSpan(), 25, resendText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        resendTextview.text = spannable

        val otpBox1: EditText = view.findViewById(R.id.otpBox1)
        val otpBox2: EditText = view.findViewById(R.id.otpBox2)
        val otpBox3: EditText = view.findViewById(R.id.otpBox3)
        val otpBox4: EditText = view.findViewById(R.id.otpBox4)
        val otpBox5: EditText = view.findViewById(R.id.otpBox5)
        val otpBox6: EditText = view.findViewById(R.id.otpBox6)

        setupOtpInput(otpBox1, otpBox2)
        setupOtpInput(otpBox2, otpBox3)
        setupOtpInput(otpBox3, otpBox4)
        setupOtpInput(otpBox4, otpBox5)
        setupOtpInput(otpBox5, otpBox6)

        setupBackspace(otpBox2, otpBox1)
        setupBackspace(otpBox3, otpBox2)
        setupBackspace(otpBox4, otpBox3)
        setupBackspace(otpBox5, otpBox4)
        setupBackspace(otpBox6, otpBox5)

        val otpErrorTextView = view.findViewById<TextView>(R.id.tvOtpError)
        val otpErrorIcon = view.findViewById<ImageView>(R.id.ivOtpErrorIcon)

        val otpFields = listOf(otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isAnyFieldEmpty = otpFields.any { it.text.isEmpty() }
                val areAllFieldsEmpty = otpFields.all { it.text.isEmpty() }
                if (isAnyFieldEmpty && !areAllFieldsEmpty) {
                    otpErrorTextView.visibility = View.VISIBLE
                    otpErrorIcon.visibility = View.VISIBLE
                } else {
                    otpErrorTextView.visibility = View.GONE
                    otpErrorIcon.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        otpFields.forEach { it.addTextChangedListener(textWatcher) }

        navController = Navigation.findNavController(view)

        binding.btnVerify.setOnClickListener {
            val phone_otp =
                otpBox1.text.toString() + otpBox2.text.toString() + otpBox3.text.toString() + otpBox4.text.toString() + otpBox5.text.toString() + otpBox6.text.toString()

            val user_id = arguments?.getString("user_id")
            val email = arguments?.getString("email")
            val phone = arguments?.getString("phone")

            if (phone_otp.length != 6) {
                view?.let {
                    Snackbar.make(it, "Please enter a valid OTP", Snackbar.LENGTH_LONG).show()
                }
                return@setOnClickListener
            } else {
                phoneVerify(user_id ?: "", phone_otp)
            }
        }

        binding.btnBack.setOnClickListener {
            val user_id = arguments?.getString("user_id")
            val email = arguments?.getString("email")
            val phone = arguments?.getString("phone")
            val bundle = bundleOf("user_id" to user_id, "email" to email, "phone" to phone)
            navController.navigate(R.id.action_verifyPhoneFragment_to_createAccount, bundle)
        }

        binding.resendTextView.setOnClickListener {
            val phone_number = arguments?.getString("phoneNumber") ?: ""
            resendPhone(phone_number)
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        binding.resendTextView.isEnabled = false
        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.resendTextView.text =
                    "Didn’t receive any code? Resend Code ($secondsRemaining)"
                val resendText = "Didn’t receive any code? Resend Code ($secondsRemaining)"
                val spannable = SpannableString(resendText)
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE), 0, 24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#7e60bf")),
                    25,
                    resendText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    UnderlineSpan(), 25, resendText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.resendTextView.text = spannable
            }

            override fun onFinish() {
                binding.resendTextView.isEnabled = true
                binding.resendTextView.text = "Didn’t receive any code? Resend Code"
                val resendText = "Didn’t receive any code? Resend Code"
                val spannable = SpannableString(resendText)
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE), 0, 24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#7e60bf")),
                    25,
                    resendText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    UnderlineSpan(), 25, resendText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.resendTextView.text = spannable
            }
        }.start()
    }

    private fun resendPhone(phone_number: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                Log.e(tag, "Phone number: $phone_number")
                val response = RetrofitInstance.api.resendPhoneOtp(resendPhoneRequest(phone_number))
                if (response.success) {
                    view?.let {
                        Snackbar.make(it, "OTP sent successfully", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error: ${e.message}")
                view?.let {
                    Snackbar.make(it, "Failed to send OTP", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun clearFields() {
        binding.otpBox1.text?.clear()
        binding.otpBox2.text?.clear()
        binding.otpBox3.text?.clear()
        binding.otpBox4.text?.clear()
        binding.otpBox5.text?.clear()
        binding.otpBox6.text?.clear()
    }

    private fun phoneVerify(user_id: String, phone_otp: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response =
                    RetrofitInstance.api.PhoneVerify(PhoneVerifyRequest(user_id, phone_otp))
                if (response.success) {
                    response.tokens.let {
                        DataStoreManager.saveTokens(requireContext(), it.access, it.refresh)
                    }
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG).show()
                    }
                    Log.d("PhoneVerification", "phoneVerify: ${response}")
                    try {
                        val accessToken = response.tokens?.access
                        DataStoreManager.saveTokens(
                            requireContext(), response.tokens?.access!!, response.tokens?.refresh!!
                        )
                        if (accessToken != null) {
                            val userResponse = getUserData(accessToken)
                            if (userResponse.success) {
                                Log.d(tag, "User Details: ${userResponse.user}")
                                val bundle = Bundle().apply {
                                    putParcelable("user_details", userResponse.user)
                                }
                                navController.navigate(
                                    R.id.action_verifyPhoneFragment_to_homeActivity, bundle
                                )
                            }
                        } else {
                            navController.navigate(R.id.action_verifyPhoneFragment_to_loginFragment)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error: ${e.message}")
                    }
                } else {
                    Log.e(tag, "Error: ${response.message}")
                    if (response.message == "Invalid OTP") {
                        binding.tvOtpError.visibility = View.VISIBLE
                        binding.ivOtpErrorIcon.visibility = View.VISIBLE
                        binding.tvOtpError.text = response.message
                    }
                    view?.let {
                        Snackbar.make(it, response.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    400 -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val gson = Gson()
                            val errorResponse = gson.fromJson(
                                errorBody,
                                phoneVerifyResponseX::class.java
                            ) // Replace with your response class
                            if (errorResponse.errors.containsKey("user_id")) {
                                view?.let {
                                    Snackbar.make(
                                        it,
                                        errorResponse.errors["user_id"]!![0],
                                        Snackbar.LENGTH_LONG
                                    )
                                        .show()
                                }
                            } else if (errorResponse.errors.containsKey("phone_otp")) {
                                view?.let {
                                    Snackbar.make(
                                        it,
                                        "${errorResponse.errors["phone_otp"]?.get(0)}",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .show()
                                }
                            } else {
                                view?.let {
                                    Snackbar.make(
                                        it,
                                        "Invalid input, please check all fields",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                            Log.e("CreateAccount", "Bad Request: ${errorResponse.message}")
                        } catch (parseException: Exception) {
                            Log.e(
                                "CreateAccount",
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
                        Log.e("LoginFragment", "Unauthorized: ${e.message()}")
                        view?.let {
                            Snackbar.make(
                                it,
                                "Unauthorized access please check your credentials",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    else -> {
                        Log.e("LoginFragment", "HTTP Error: ${e.code()} - ${e.message()}")
                        view?.let {
                            Snackbar.make(it, "${e.message}", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("LoginFragment", "Error: ${e.message}")
                view?.let {
                    Snackbar.make(it, "Network error", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("LoginFragment", "Error: ${e.message}")
                view?.let {
                    Snackbar.make(it, "${e.message}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                progressDialog.dismiss()
                clearFields()
            }
        }
    }

    private fun setupOtpInput(currentBox: EditText, nextBox: EditText) {
        currentBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length == 1) {
                    nextBox.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBackspace(currentBox: EditText, previousBox: EditText) {
        currentBox.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (currentBox.text.isNotEmpty()) {
                    currentBox.text.clear()
                } else {
                    previousBox.requestFocus()
                    previousBox.text.clear()
                }
                true
            } else {
                false
            }
        }
    }

    override fun onBackPressed() {
        val user_id = arguments?.getString("user_id")
        val email = arguments?.getString("email")
        val phone = arguments?.getString("phone")
        val bundle = bundleOf("user_id" to user_id, "email" to email, "phone" to phone)
        navController.navigate(R.id.action_verifyPhoneFragment_to_createAccount, bundle)
    }

    private suspend fun getUserData(accessToken: String): GetUserResponse {
        return RetrofitInstance.api.getUserData("Bearer $accessToken")
    }
}