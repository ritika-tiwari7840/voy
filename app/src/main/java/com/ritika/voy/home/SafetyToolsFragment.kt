package com.ritika.voy.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentSafetyToolsBinding

class SafetyToolsFragment : Fragment() {
    private var _binding: FragmentSafetyToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSafetyToolsBinding.inflate(inflater, container, false)

        // Using findNavController
        val navController = findNavController()

        // Back button action
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_safetyToolsFragment_to_settingsFragment)
        }

        // Add contact action
        binding.addContact.setOnClickListener {
            navController.navigate(R.id.action_safetyToolsFragment_to_editInfo)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
