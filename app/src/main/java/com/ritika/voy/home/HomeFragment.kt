package com.ritika.voy.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentHomeBinding
import com.ritika.voy.databinding.FragmentLoginBinding

class HomeFragment : Fragment() {
    lateinit var _binding: FragmentHomeBinding
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController = Navigation.findNavController(view)

        binding.findPool.setOnClickListener {
            binding.findPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.theme_color)
            binding.findPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.offerPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.offerPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        }

        binding.offerPool.setOnClickListener {
            binding.offerPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.theme_color)
            binding.offerPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.findPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.findPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        }

        binding.whereTo.setOnClickListener {
            navController.navigate(R.id.action_home_to_chooseSpotFragment)
        }
    }

}