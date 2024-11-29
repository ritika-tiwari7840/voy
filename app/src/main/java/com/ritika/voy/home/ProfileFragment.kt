package com.ritika.voy.home

import com.ritika.voy.api.datamodels.SharedViewModel
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentProfileBinding
import com.bumptech.glide.request.transition.Transition

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
        val sharedViewModel: SharedViewModel by activityViewModels()
        val user = sharedViewModel.user
        if (user != null) {
            binding.userName.text = user.full_name
            setSpannableText(binding.ratingAsHost, "${user.rating_as_driver} \nAs Host")
            setSpannableText(binding.ratingAsGuest, "${user.rating_as_passenger} \nAs Host")
            setSpannableText(
                binding.totalRatings,
                "${(user.rating_as_driver + user.rating_as_passenger ?: 0.0).div(2)} \nRatings"
            )
            val imageUrl = user.profile_photo.toString()
            val textView = binding.userName
            Log.d("Image", "onLoadCleared:  $imageUrl")
            Glide.with(requireContext())
                .load(imageUrl)
                .apply(RequestOptions().circleCrop()) // Apply circular crop transformation
                .placeholder(R.drawable.profile_image)
                .error(R.drawable.profile_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .override(250, 250)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        resource.setBounds(0, 0, resource.intrinsicWidth, resource.intrinsicHeight)
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            resource,
                            null,
                            null
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle clearing the resource (optional)
                    }
                })

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
