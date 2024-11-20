package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.content.IntentSender.OnFinished
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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.VerifyRequest
import com.ritika.voy.api.dataclasses.resendOTPRequest
import com.ritika.voy.databinding.FragmentOtpBinding
import kotlinx.coroutines.launch

class OtpFragment : BaseFragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private lateinit var resendTimer: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
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
        startResendTimer()
        val resendText = "Didn’t receive any code? Resend Code"
        val spannable = SpannableString(resendText)
        spannable.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            24,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#7e60bf")),
            25,
            resendText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            UnderlineSpan(),
            25,
            resendText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
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

        val email = arguments?.getString("email").toString()
        binding.tvForgotSubtitle.text = "Please enter the verification code sent to \n $email"

        //navigation

        binding.btnVerify.setOnClickListener {

            val otp = otpBox1.text.toString() +
                    otpBox2.text.toString() +
                    otpBox3.text.toString() +
                    otpBox4.text.toString() +
                    otpBox5.text.toString() +
                    otpBox6.text.toString()



            if (otp.length != 6) {
                Toast.makeText(requireContext(), "Please enter a valid otp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{

                verifyOTP(email, otp)
            }

        }
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_otpFragment_to_forgotPasswordFragment)
        }
        binding.resendTextView.setOnClickListener {
            resendOTP(email)
            startResendTimer()
        }

    }

    private fun startResendTimer() {
        binding.resendTextView.isEnabled = false
        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.resendTextView.text = "Didn’t receive any code? Resend Code ($secondsRemaining)"
                val resendText = "Didn’t receive any code? Resend Code ($secondsRemaining)"
                val spannable = SpannableString(resendText)
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    0,
                    24,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#7e60bf")),
                    25,
                    resendText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    UnderlineSpan(),
                    25,
                    resendText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.resendTextView.text = spannable
            }

            override fun onFinish() {
                binding.resendTextView.isEnabled = true
                binding.resendTextView.text = "Didn’t receive any code? Resend Code"
                val resendText = "Didn’t receive any code? Resend Code"
                val spannable = SpannableString(resendText)
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    0,
                    24,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#7e60bf")),
                    25,
                    resendText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    UnderlineSpan(),
                    25,
                    resendText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.resendTextView.text = spannable
            }
        }.start()
    }

    private fun clearFields() {
        binding.otpBox1.text?.clear()
        binding.otpBox2.text?.clear()
        binding.otpBox3.text?.clear()
        binding.otpBox4.text?.clear()
        binding.otpBox5.text?.clear()
        binding.otpBox6.text?.clear()
    }



    private fun verifyOTP(email: String, otp: String){
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.verifyOtp(VerifyRequest(email, otp))
                if (response.success){
                    val otpBundle = Bundle().apply {
                        putString("otp", otp)
                        putString("email", email)
                    }
                    Toast.makeText(requireContext(), "Otp verified", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_otpFragment_to_resetPassword, otpBundle)
                }
                else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception) {
                Toast.makeText(requireContext(), "An unexpected error occurred", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                progressDialog.dismiss()
                clearFields()
            }
        }
    }

    private fun resendOTP(email: String){
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.resendOtp(resendOTPRequest(email))
                if (response.success){
                    Toast.makeText(requireContext(), "Otp sent", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(requireContext(), "Failed to send otp: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception) {
                Log.e("API Error", "Error resending OTP: ${e.message}", e)
                Toast.makeText(requireContext(), "An unexpected error occurred", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                progressDialog.dismiss()
                clearFields()
            }
        }

    }

    fun setupOtpInput(currentBox: EditText, nextBox: EditText) {
        currentBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length == 1) {
                    nextBox.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun setupBackspace(currentBox: EditText, previousBox: EditText) {
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
        navController.navigate(R.id.action_otpFragment_to_forgotPasswordFragment)
    }
}