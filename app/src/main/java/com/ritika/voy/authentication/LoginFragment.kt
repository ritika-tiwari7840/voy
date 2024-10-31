package com.ritika.voy.authentication

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ritika.voy.R

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
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
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordButton.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
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
                    emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)
                    emailErrorTextView.visibility = View.GONE
                } else if(email.isEmpty()){
                    emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)
                    emailErrorTextView.visibility = View.GONE
                }
                else{
                    emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background_error)
                    emailErrorTextView.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
