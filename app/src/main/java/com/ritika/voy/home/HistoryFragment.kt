package com.ritika.voy.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.adapter.RideHistoryAdapter
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.RideDetails
import com.ritika.voy.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private lateinit var _binding: FragmentHistoryBinding
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RideHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize RecyclerView
        recyclerView = binding.recyclerViewRideHistory
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch ride history data
        fetchRideHistory()

        return view
    }

    private fun fetchRideHistory() {
        lifecycleScope.launch {
            val token = DataStoreManager.getToken(requireContext(), "access")?.first()
            try {
                val response = RetrofitInstance.api.getRideHistory("Bearer $token")
                if (response.success) {
                    val rideHistory = response.data.as_passenger
                    setupRecyclerView(rideHistory)
                    checkRecyclerViewEmpty()
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRecyclerView(rideHistory: List<RideDetails>) {
        adapter = RideHistoryAdapter(rideHistory)
        recyclerView.adapter = adapter
    }

    private fun checkRecyclerViewEmpty() {
        if (recyclerView.adapter?.itemCount == 0) {
            binding.noHistoryFound.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            binding.noHistoryFound.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
