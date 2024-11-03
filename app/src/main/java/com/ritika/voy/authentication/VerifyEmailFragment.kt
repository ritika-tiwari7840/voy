package com.ritika.voy.authentication

import android.graphics.Color
import android.os.Bundle
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
import android.widget.Toast
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.api.AuthService
import com.ritika.voy.api.dataclasses.VerifyRequest
import com.ritika.voy.api.dataclasses.VerifyResponse
import com.ritika.voy.api.datamodels.UserData
import com.ritika.voy.api.datamodels.UserPreferences
import com.ritika.voy.databinding.FragmentVerifyEmailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class VerifyEmailFragment : BaseFragment() {

    private var _binding: FragmentVerifyEmailBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var userData: UserData
    private lateinit var navController: NavController

    private val otpFields: List<EditText> by lazy {
        listOf(
            binding.otpBox1,
            binding.otpBox2,
            binding.otpBox3,
            binding.otpBox4,
            binding.otpBox5,
            binding.otpBox6
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences.getInstance(requireContext())
        userData = UserData.getInstance(requireContext())
        navController = Navigation.findNavController(requireView())


        setupLayoutMargins(view)
        setupResendTextView()
        setupOtpInputs()
        setupTextWatchers()
        setupButtonListeners()
    }

    override fun onBackPressed() {
        navController.navigate(R.id.createAccount)
    }

    private fun verifyEmailOtp(otp: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val tempUserId = userPreferences.getUserToken()

                if (tempUserId == null) {
                    showToast("User token is missing")
                    return@launch
                }

                val request = VerifyRequest(
                    email_otp = otp,
                    phone_otp = "012637", // This should be dynamic as per your requirements
                    temp_user_id = tempUserId
                )

                // Make the API call using Retrofit
                val response = Retrofit.api.create(AuthService::class.java).verifyOTP(request).awaitResponse()
                handleApiResponse(response)

            } catch (e: Exception) {
                Log.e("VerifyEmailFragment", "Error during email verification", e)
                showToast("An error occurred: ${e.message}")
            }
        }
    }

    private suspend fun handleApiResponse(response: retrofit2.Response<VerifyResponse>) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful && response.body() != null) {
                val verifyResponse = response.body()!!
                if (verifyResponse.success) {
                    userData.saveVerificationResponse(
                        verifyResponse.tokens.access,
                        verifyResponse.tokens.refresh,
                        verifyResponse.user.id.toString(),
                        verifyResponse.user.email,
                        verifyResponse.user.full_name,
                        verifyResponse.user.created_at
                    )
                    showToast("Email Verified")
                    navController.navigate(R.id.verifyPhoneFragment)
                } else {
                    showToast(verifyResponse.message)
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e("VerifyEmailFragment", "Verification failed: $errorMessage")
                showToast("Verification failed: $errorMessage")
                binding.tvForgotSubtitle.text = errorMessage
            }
        }
    }

    private fun setupLayoutMargins(view: View) {
        val screenHeight = resources.displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.304).toInt()
        val bottomSection: View = view.findViewById(R.id.bottomSection)
        val params = bottomSection.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = topMargin
        bottomSection.layoutParams = params
    }

    private fun setupResendTextView() {
        val resendTextView = binding.resendTextView
        val resendText = "Didn’t receive any code? Resend Code"
        val spannable = SpannableString(resendText).apply {
            setSpan(ForegroundColorSpan(Color.WHITE), 0, 24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(Color.parseColor("#7e60bf")), 25, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(UnderlineSpan(), 25, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        resendTextView.text = spannable
        resendTextView.setOnClickListener {
            showToast("Resend OTP functionality to be implemented")
            // Call your resend OTP API here
        }
    }

    private fun setupOtpInputs() {
        for (i in 0 until otpFields.size - 1) {
            setupOtpInput(otpFields[i], otpFields[i + 1])
            setupBackspace(otpFields[i + 1], otpFields[i])
        }
    }

    private fun setupTextWatchers() {
        val otpErrorTextView = binding.tvOtpError
        val otpErrorIcon = binding.ivOtpErrorIcon

        otpFields.forEach { field ->
            field.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val isAnyFieldEmpty = otpFields.any { it.text.isEmpty() }
                    otpErrorTextView.visibility = if (isAnyFieldEmpty) View.VISIBLE else View.GONE
                    otpErrorIcon.visibility = if (isAnyFieldEmpty) View.VISIBLE else View.GONE
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun setupButtonListeners() {
        binding.btnBack.setOnClickListener {
            navController.popBackStack()
        }

        binding.btnVerify.setOnClickListener {
            if (areAllFieldsFilled()) {
                val enteredOtp = otpFields.joinToString("") { it.text.toString() }
                verifyEmailOtp(enteredOtp)
            } else {
                showToast("Please fill all fields")
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

    private fun areAllFieldsFilled(): Boolean {
        return otpFields.all { it.text.isNotEmpty() }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
