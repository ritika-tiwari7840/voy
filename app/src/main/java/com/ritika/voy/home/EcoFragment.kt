package com.ritika.voy.home

import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.datamodels.SharedViewModel
import com.ritika.voy.databinding.FragmentChooseSpotBinding
import com.ritika.voy.databinding.FragmentEcoBinding
import com.ritika.voy.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EcoFragment : Fragment() {
    lateinit var _binding: FragmentEcoBinding
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentEcoBinding.inflate(inflater, container, false)

        val sharedViewModel: SharedViewModel by activityViewModels()

        val user = sharedViewModel.user
        if (user != null) {
            if (user.first_name != null && user.first_name != "") {
                binding.userName.text = user.first_name
            } else {
                binding.userName.text = "User Name"
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}