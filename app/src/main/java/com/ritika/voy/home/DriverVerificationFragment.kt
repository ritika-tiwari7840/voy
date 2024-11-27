package com.ritika.voy.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.databinding.FragmentDriverVerificationBinding
import com.ritika.voy.databinding.FragmentVerifyBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DriverVerificationFragment : Fragment() {
    private var _binding: FragmentDriverVerificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private var isDiverVerified: Boolean? = null ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDriverVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        lifecycleScope.launch {
            DataStoreManager.getUserData(requireContext(), "isDriverVerified").first().let {
                isDiverVerified = it.toBoolean()
                if (isDiverVerified == true) {
                    binding.uploadStatusButton.text = "uploaded"
                    binding.uploadStatusButton.setBackground(resources.getDrawable(R.drawable.button_background))
                } else {
                    binding.uploadStatusButton.text = "Not uploaded"
                    binding.uploadStatusButton.setBackground(resources.getDrawable(R.drawable.upload_button_status_background))
                }
            }
        }

        binding.uploadStatusButton.setOnClickListener {
            navController.navigate(R.id.action_driverVerificationFragment_to_uploadLicenseFragment)
        }

        binding.driverVerificationStatus.setOnClickListener {
            navController.navigate(R.id.action_driverVerificationFragment_to_uploadLicenseFragment)
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_driverVerificationFragment_to_verifyFragment)
        }

    }

}