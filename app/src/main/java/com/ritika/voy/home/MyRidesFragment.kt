package com.ritika.voy.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ritika.voy.R
import com.ritika.voy.adapter.MyRidesAdapter
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.datamodels.SharedViewModel
import com.ritika.voy.databinding.FragmentMyRidesBinding

class MyRidesFragment : Fragment(), MyRidesAdapter.OnItemClickListener {
    private var _binding: FragmentMyRidesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MyRidesAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyRidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.findPool.setOnClickListener {
            binding.findPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.theme_color)
            binding.findPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.offerPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.offerPool.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        binding.offerPool.setOnClickListener {
            Toast.makeText(requireContext(), "You are Driver now", Toast.LENGTH_SHORT).show()
            binding.offerPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.theme_color)
            binding.offerPool.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.findPool.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.findPool.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }
        binding.recyclerViewMyRides.layoutManager = LinearLayoutManager(context)
        adapter = MyRidesAdapter(this)
        binding.recyclerViewMyRides.adapter = adapter
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        arguments?.getSerializable("rideItems")?.let { initialRides ->
            @Suppress("UNCHECKED_CAST")
            val rideList = initialRides as? ArrayList<MyRideItem>
            rideList?.let {
                sharedViewModel.addRideItems(it)
            }
        }

        // Observe ride items and update adapter
        sharedViewModel.rideItem.observe(viewLifecycleOwner) { rides ->
            // Submit list to adapter
            adapter.submitList(rides)

            // Update visibility of empty state
            updateEmptyState(rides)
        }

    }

    // Implement optional click handler
    override fun onItemClick(ride: MyRideItem) {
        val bundle = Bundle()
        bundle.putSerializable("driverId", ride.driver)
        navController.navigate(R.id.action_myRidesFragment_to_matchingMyRidesFragment, bundle)
    }

    private fun updateEmptyState(rides: List<MyRideItem>) {
        // Show/hide views based on ride list
        binding.recyclerViewMyRides.visibility = if (rides.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyStateView.visibility = if (rides.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}