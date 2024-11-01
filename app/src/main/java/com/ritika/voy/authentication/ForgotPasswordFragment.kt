package com.ritika.voy.authentication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.BaseFragment
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentForgotPasswordBinding

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
    ): View{
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

        navController = Navigation.findNavController(view)

        binding.btnContinue.setOnClickListener {
            navController.navigate(R.id.otpFragment)
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.loginFragment)
        }
    }

    override fun onBackPressed() {
        navController.navigate(R.id.loginFragment)
    }

}
