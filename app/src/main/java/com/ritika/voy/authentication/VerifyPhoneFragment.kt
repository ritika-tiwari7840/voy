package com.ritika.voy.authentication

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ritika.voy.R

class VerifyPhoneFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify_phone, container, false)
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
}