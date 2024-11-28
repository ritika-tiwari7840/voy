package com.ritika.voy.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.ritika.voy.MainActivity
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.databinding.FragmentProfileBinding
import com.ritika.voy.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    lateinit var _binding: FragmentSettingsBinding
    private val binding get() = _binding!!
    lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.logout.setOnClickListener {
            // Clear tokens from DataStore
            lifecycleScope.launch {
                DataStoreManager.clearTokens(requireContext())
            }

            // Create intent to navigate to MainActivity
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Start MainActivity and finish current activity
            startActivity(intent)
            activity?.finish() // To close the current activity if needed
        }

        return binding.root
    }
}