package com.ritika.voy

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.databinding.FragmentContinueWithEmailBinding
import com.ritika.voy.authentication.CreateAccount

class ContinueWithEmail : BaseFragment() {
    private var _binding: FragmentContinueWithEmailBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentContinueWithEmailBinding.inflate(inflater, container, false)

        val continueWithTermsText: TextView = binding.continueWithTermsText
        val fullText = "By Signing up you agree to our Terms of Use"
        val termsText = "Terms of Use"


        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(termsText)
        val end = start + termsText.length

        val termsColor = ContextCompat.getColor(requireContext(), R.color.theme_color)
        spannableString.setSpan(
            UnderlineSpan(),start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(termsColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        continueWithTermsText.text = spannableString



        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.continueWithEmailButton.setOnClickListener {
            navController.navigate(R.id.loginFragment)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }

    override fun onBackPressed() {
        requireActivity().finish()
    }

}

