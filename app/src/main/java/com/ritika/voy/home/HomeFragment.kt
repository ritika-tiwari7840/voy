package com.ritika.voy.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            navController.navigate(R.id.action_home_to_chooseSpotFragment)
        }
        binding.offerPool.setOnClickListener {
            navController.navigate(R.id.action_home_to_chooseSpotFragment)
        }
    }

}