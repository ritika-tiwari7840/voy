package com.ritika.voy.home

import SharedViewModel
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
import androidx.lifecycle.ViewModelProvider
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

        var sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        // Access the user object
        val user = sharedViewModel.user
        if (user != null) {
            binding.userName.text = user.full_name
            setSpannableText(binding.ratingAsHost, "${user.rating_as_driver} \nAs Host")
            setSpannableText(binding.ratingAsGuest, "${user.rating_as_passenger} \nAs Host")
            setSpannableText(
                binding.totalRatings,
                "${(user.rating_as_driver + user.rating_as_passenger ?: 0.0).div(2)} \nRatings"
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.editProfile.setOnClickListener {
            navController.navigate(R.id.action_profile_to_editInfo)
        }
        binding.contributionText.setOnClickListener {
            navController.navigate(R.id.action_profile_to_eco)

        }
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_profile_to_home)
        }
        binding.settings.setOnClickListener {
            navController.navigate(R.id.action_profile_to_settingsFragment)
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
