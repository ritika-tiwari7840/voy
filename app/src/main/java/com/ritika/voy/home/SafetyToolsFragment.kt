package com.ritika.voy.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentSafetyToolsBinding
import com.ritika.voy.databinding.FragmentSettingsBinding


class SafetyToolsFragment : Fragment() {
    lateinit var _binding: FragmentSafetyToolsBinding
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
        _binding = FragmentSafetyToolsBinding.inflate(inflater, container, false)
        navController = findNavController()
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_safetyToolsFragment_to_settingsFragment)
        }
        binding.addContact.setOnClickListener {
            navController.navigate(R.id.action_safetyToolsFragment_to_editInfo)
        }

        return binding.root
    }


}