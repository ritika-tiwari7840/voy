package com.ritika.voy.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        setupUI()
        setupRecyclerView()
        setupViewModel()
    }

    private fun setupUI() {
        binding.findPool.setOnClickListener {
            updateButtonAppearance(binding.findPool, binding.offerPool)
        }
        binding.offerPool.setOnClickListener {
            updateButtonAppearance(binding.offerPool, binding.findPool)
        }
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_myRidesFragment_to_home)
        }
    }

    private fun updateButtonAppearance(selectedButton: View, unselectedButton: View) {
        selectedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.theme_color)
        (selectedButton as? android.widget.Button)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        unselectedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
        (unselectedButton as? android.widget.Button)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMyRides.layoutManager = LinearLayoutManager(context)
        adapter = MyRidesAdapter(this)
        binding.recyclerViewMyRides.adapter = adapter
    }

    private fun setupViewModel() {
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        arguments?.getSerializable("rideItems")?.let { initialRides ->
            @Suppress("UNCHECKED_CAST")
            val rideList = initialRides as? ArrayList<MyRideItem>
            rideList?.let {
                sharedViewModel.updateRidesFromResponse(it)
            }
        }

        sharedViewModel.rideItem.observe(viewLifecycleOwner) { rides ->
            adapter.submitList(rides)
            updateEmptyState(rides)
        }
    }

    override fun onItemClick(ride: MyRideItem) {
        val bundle = Bundle()
        bundle.putSerializable("id", ride.id)
        navController.navigate(R.id.action_myRidesFragment_to_matchingMyRidesFragment, bundle)
    }

    private fun updateEmptyState(rides: List<MyRideItem>) {
        binding.recyclerViewMyRides.visibility = if (rides.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyStateView.visibility = if (rides.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

