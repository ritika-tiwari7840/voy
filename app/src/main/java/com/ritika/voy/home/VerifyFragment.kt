package com.ritika.voy.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentEditInfoBinding
import com.ritika.voy.databinding.FragmentVerifyBinding

class VerifyFragment : Fragment() {
    private var _binding: FragmentVerifyBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_verifyFragment_to_home)
        }
        binding.driverVerificationStatus.setOnClickListener {
            navController.navigate(R.id.action_verifyFragment_to_driverVerificationFragment)
        }
        binding.driverVerificationLayout.setOnClickListener {
            navController.navigate(R.id.action_verifyFragment_to_driverVerificationFragment)
        }
        binding.vehicleDetailsLayout.setOnClickListener {
            navController.navigate(R.id.action_verifyFragment_to_vehicleDetailsFragment)
        }
        binding.vehicleDetailsStatus.setOnClickListener {
            navController.navigate(R.id.action_verifyFragment_to_vehicleDetailsFragment)
        }
    }

}