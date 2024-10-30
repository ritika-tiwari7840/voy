package com.ritika.voy

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentContinueWithEmailBinding
import com.ritika.voy.signup.CreateAccount

class ContinueWithEmail : Fragment() {
    private var _binding: FragmentContinueWithEmailBinding? = null
    private val binding get() = _binding!!


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
            ForegroundColorSpan(termsColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        continueWithTermsText.text = spannableString



        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueWithEmailButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, CreateAccount())
                .addToBackStack(null)
                .commit()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }

    companion object {

    }
}

