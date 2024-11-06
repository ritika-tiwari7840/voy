package com.ritika.voy.authentication

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.ForgotRequest
import com.ritika.voy.databinding.FragmentForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordFragment : BaseFragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
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
                if (email.contains("@")) {
                    emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)
                    emailErrorTextView.visibility = View.GONE
                } else if (email.isEmpty()) {
                    emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)
                    emailErrorTextView.visibility = View.GONE
                } else {
                    emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background_error)
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
            if (email.isNotEmpty() && email.contains("@")) {
                forgotpassword(email)
            } else {
                emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background_error)
                emailErrorTextView.visibility = View.VISIBLE
            }
            navController.navigate(R.id.otpFragment, emailBundle)
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.loginFragment)
        }
    }

    private fun forgotpassword(email : String){
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
                    Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.otpFragment, emailBundle)
                } else {
                    Toast.makeText(requireContext(), "Failed to send reset email: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show()
            } finally {
                progressDialog.dismiss()
            }
        }

    }

    override fun onBackPressed() {
        navController.navigate(R.id.loginFragment)
    }
}