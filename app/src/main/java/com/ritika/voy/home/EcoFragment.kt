package com.ritika.voy.home

import android.app.ProgressDialog
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.datamodels.SharedViewModel
import com.ritika.voy.databinding.FragmentChooseSpotBinding
import com.ritika.voy.databinding.FragmentEcoBinding
import com.ritika.voy.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.log

class EcoFragment : Fragment() {
    lateinit var _binding: FragmentEcoBinding
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentEcoBinding.inflate(inflater, container, false)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val user = sharedViewModel.user
        if (user != null) {
            if (user.first_name != null && user.first_name != "") {
                binding.userName.text = user.first_name
            } else {
                binding.userName.text = "User Name"
            }
        }

        val rideId = sharedViewModel.rideId
        if (rideId != 0) {
            Log.d("ECO", "onCreateView: $rideId")
            lifecycleScope.launch {
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Loading...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                try {
                    val authToken = DataStoreManager.getToken(requireContext(), "access").first()
                    if (authToken.isNullOrEmpty()) {
                        throw Exception("Authentication token is null or empty")
                    }
                    eco("Bearer $authToken", rideId)

                } catch (e: Exception) {
                    Log.e(
                        "ECO",
                        "Error fetching token or updating status: ${e.message}"
                    )
                } finally {
                    progressDialog.dismiss()
                }
            }
        } else {
            Log.e(
                "ECO",
                "Context is not a LifecycleOwner. Unable to launch coroutine."
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private suspend fun eco(authToken: String, rideId: Int?) {
        val TAG: String = "ECO"
        if (rideId == null) {
            Log.e(TAG, "Ride ID cannot be null.")
            return
        }

        try {
            val response = RetrofitInstance.api.eco(authHeader = authToken, rideId = rideId)
            if (response.success) {
                Log.d("ECO", "ECO update successful: $response")
            } else {
                Log.e("ECO", "Error updating status: ${response}")
            }
        } catch (e: Exception) {
            Log.e("ECO", "Error updating status: ${e.message}", e)
        }
    }
}

