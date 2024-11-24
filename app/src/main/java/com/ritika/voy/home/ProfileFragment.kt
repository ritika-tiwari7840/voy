package com.ritika.voy.home

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.databinding.FragmentChooseSpotBinding
import com.ritika.voy.databinding.FragmentProfileBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding!!
    lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.editProfile.setOnClickListener {
            navController.navigate(R.id.action_profile_to_editInfo)
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_profile_to_home)
        }

        lifecycleScope.launch {
            var totalRating: Double? = 0.0
            DataStoreManager.getUserData(requireContext(), "fullName").first().let {
                val fullName = it.toString()
                if (fullName.isNotEmpty()) {
                    binding.userName.text = "$fullName"
                } else {
                    binding.userName.text = "User Name"
                }
            }
            DataStoreManager.getUserData(requireContext(), "ratingAsDriver").first().let {
                val ratingAsDriver = it.toString()
                if (ratingAsDriver.isNotEmpty()) {
                    setSpannableText(binding.ratingAsHost, "$ratingAsDriver \nAs Host")
                    totalRating = totalRating?.plus(ratingAsDriver.toDouble())
                } else {
                    setSpannableText(binding.ratingAsHost, "5.0 \nAs Host")
                }
            }
            DataStoreManager.getUserData(requireContext(), "ratingAsPassenger").first().let {
                val ratingAsPassenger = it.toString()
                if (ratingAsPassenger.isNotEmpty()) {
                    setSpannableText(binding.ratingAsGuest, "$ratingAsPassenger \nAs Host")
                    totalRating = totalRating?.plus(ratingAsPassenger.toDouble())
                } else {
                    setSpannableText(binding.ratingAsGuest, "$5.0 \nAs Host")
                }
            }
            setSpannableText(
                binding.totalRatings, "${(totalRating ?: 0.0).div(2)} \nRatings"
            )
        }
    }

    private fun setSpannableText(textView: TextView, text: String) {
        val spannable = SpannableString(text)

        spannable.setSpan(
            StyleSpan(Typeface.BOLD), 0, text.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            RelativeSizeSpan(1.5f), 0, text.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }

}
