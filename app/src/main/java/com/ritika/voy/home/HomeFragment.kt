package com.ritika.voy.home

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.semantics.Role
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.databinding.FragmentHomeBinding
import com.ritika.voy.databinding.FragmentLoginBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


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
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        role = "passenger"

        lifecycleScope.launch {
            DataStoreManager.getUserData(requireContext(), "fullName").first().let {
                val fullName = it.toString()
                if (fullName.isNotEmpty() && fullName != null) {
                    binding.homeGreetingText.text = "hello, $fullName"
                } else {
                    binding.homeGreetingText.text = "hello, User"
                }
            }
            DataStoreManager.getUserData(requireContext(), "isDriverVerified").first().let {
                isDiverVerified = it.toBoolean()
                Toast.makeText(requireContext(), "$isDiverVerified", Toast.LENGTH_SHORT).show()
            }
            DataStoreManager.getUserData(requireContext(), "firstName").first().let {
                firstName = it.toString()
            }

        }

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