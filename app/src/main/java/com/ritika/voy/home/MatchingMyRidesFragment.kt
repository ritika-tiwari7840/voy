package com.ritika.voy.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonParseException
import com.ritika.voy.adapter.MatchingRidesAdapter
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.DriverRequestList
import com.ritika.voy.databinding.FragmentMatchingMyRidesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MatchingMyRidesFragment : Fragment() {
    private var _binding: FragmentMatchingMyRidesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MatchingRidesAdapter
    private lateinit var navController: NavController
    private val TAG = "MatchingMyRidesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMatchingMyRidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        setupRecyclerView()
        setupBackButton()

        val id = when (val serializedId = arguments?.getSerializable("id")) {
            is String -> serializedId
            is Int -> serializedId.toString()
            else -> null
        }

        Log.d(TAG, "onViewCreated: id=$id")

        id?.let { fetchDriverRequestList(it) } ?: run {
            Log.e(TAG, "onViewCreated: id is null, cannot fetch driver request list")
            showEmptyState("No driver ID provided")
        }
    }

    private fun setupRecyclerView() {
        adapter = MatchingRidesAdapter(emptyList())
        binding.recyclerViewMyRides.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MatchingMyRidesFragment.adapter
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun fetchDriverRequestList(driverId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val authToken = DataStoreManager.getToken(requireContext(), "access").first()
                if (authToken.isNullOrEmpty()) {
                    throw Exception("Authentication token is null or empty")
                }
                val response = getDriverRequestedList("Bearer $authToken", driverId.toInt())
                withContext(Dispatchers.Main) {
                    response?.let {
                        if (it.success && it.data.isNotEmpty()) {
                            adapter.updateRides(it.data)
                            showRecyclerView()
                        } else {
                            showEmptyState("No matching rides found")
                        }
                    } ?: showEmptyState("Failed to fetch driver request list")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while fetching driver request list", e)
                withContext(Dispatchers.Main) {
                    showEmptyState("An error occurred: ${e.localizedMessage}")
                    Toast.makeText(
                        context,
                        "Failed to load rides: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private suspend fun getDriverRequestedList(
        authToken: String,
        driverId: Int,
    ): DriverRequestList? {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "getDriverRequestedList: authToken=$authToken, driverId=$driverId")
            try {
                val response = RetrofitInstance.api.driverListRequest("$authToken", driverId)
                Log.d(TAG, "Driver request list: $response")
                response
            } catch (e: IOException) {
                Log.e(TAG, "Network Error: ${e.localizedMessage}")
                null
            } catch (e: JsonParseException) {
                Log.e(TAG, "Parsing Error: ${e.localizedMessage}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected Error: ${e.localizedMessage}")
                null
            }
        }
    }

    private suspend fun showLoading(isLoading: Boolean) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showRecyclerView() {
        binding.recyclerViewMyRides.visibility = View.VISIBLE
        binding.emptyStateView.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.recyclerViewMyRides.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
        binding.emptyStateText.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}