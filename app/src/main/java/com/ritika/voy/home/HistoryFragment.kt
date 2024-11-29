package com.ritika.voy.home

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RideHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewRideHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch data and set to RecyclerView
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
                    Log.d("history", "fetchRideHistory: $rideHistory $token")
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
}
