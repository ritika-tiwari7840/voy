package com.ritika.voy.home

import com.ritika.voy.api.datamodels.SharedViewModel
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.R
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.GetUserResponse
import com.ritika.voy.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    lateinit var _binding: FragmentHomeBinding
    private val binding get() = _binding!!
    private lateinit var role: String
    lateinit var navController: NavController
    private lateinit var firstName: String
    private var isDiverVerified: Boolean? = null ?: false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedViewModel: SharedViewModel by activityViewModels()

        val user = sharedViewModel.user
        if (user != null) {
            if (user.first_name != null && user.first_name != "") {
                firstName = user.first_name
            } else {
                firstName = "User"
            }
            if (user.is_driver_verified != null) {
                isDiverVerified = user.is_driver_verified
            } else {
                isDiverVerified = false
            }
            binding.homeGreetingText.text = "hello, ${firstName}"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        role = "passenger"
        binding.findPool.setOnClickListener {
            binding.findPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.theme_color)
            binding.findPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.offerPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.offerPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            role = "passenger"
            Toast.makeText(requireContext(), "You are passenger now", Toast.LENGTH_SHORT).show()
        }
        binding.offerPool.setOnClickListener {
            if (isDiverVerified == true) {
                role = "driver"
                Toast.makeText(requireContext(), "You are Driver now", Toast.LENGTH_SHORT).show()
                binding.offerPool.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.theme_color)
                binding.offerPool.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.findPool.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
                binding.findPool.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

            } else {
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.verification)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
                val textView = dialog.findViewById<TextView>(R.id.user_name)
                val button = dialog.findViewById<Button>(R.id.get_verification)
                textView.text =
                    "$firstName, you’re only missing few steps\n from having a Verified profile."
                button.setOnClickListener {
                    navController.navigate(R.id.action_home_to_verifyFragment)
                    dialog.dismiss()
                }
            }
        }
        binding.contributionTextView.setOnClickListener {
            navController.navigate(R.id.action_home_to_eco)
        }

        binding.whereTo.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("role", role)
            Toast.makeText(requireContext(), "You are $role", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_home_to_chooseSpotFragment, bundle)
        }
    }

}