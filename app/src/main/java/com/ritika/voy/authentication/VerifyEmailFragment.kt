package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.EmailVerifyRequest
import com.ritika.voy.databinding.FragmentVerifyEmailBinding
import kotlinx.coroutines.launch

class VerifyEmailFragment : BaseFragment() {

    private var _binding: FragmentVerifyEmailBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private var UserId: String? = null

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireView())
        UserId = arguments?.getString("user_id")

        if (UserId == null) {
            showToast("User ID is missing")
            navController.navigateUp()
            return
        }

        val enteredOtp = otpFields.joinToString("") { it.text.toString() }

        setupLayoutMargins(view)
        setupResendTextView()
        setupOtpInputs()
        setupTextWatchers()
        setupButtonListeners()
    }

    override fun onBackPressed() {
        navController.navigate(R.id.createAccount)
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
                val user_id = arguments?.getString("user_id") ?: ""
                emailVerify(user_id, enteredOtp)
            } else {
                showToast("Please fill all fields")
            }
        }
    }

    private fun emailVerify(user_id: String, otp: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.EmailVerify(EmailVerifyRequest(user_id, otp))
                if (response.success) {
                    val userBundle = Bundle().apply {
                        putString("user_id", user_id)
                    }
                    Toast.makeText(requireContext(), "Email verified Successfully, Please Verify Phone Number.", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.verifyPhoneFragment, userBundle)
                } else {
                    showToast(response.message)
                }
            } catch (e: Exception) {
                showToast("An unexpected error occurred")
            } finally {
                progressDialog.dismiss()
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
}