package com.ritika.voy.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentChooseSpotBinding
import com.ritika.voy.databinding.FragmentProfileBinding

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.editProfile.setOnClickListener {
            navController.navigate(R.id.action_profile_to_editInfo)
        }

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_profile_to_home)
        }

    }


}
