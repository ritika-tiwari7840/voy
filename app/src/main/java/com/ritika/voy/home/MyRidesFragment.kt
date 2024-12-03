package com.ritika.voy.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritika.voy.R
import com.ritika.voy.adapter.MyRidesAdapter
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.datamodels.SharedViewModel
import com.ritika.voy.databinding.FragmentMyRidesBinding

class MyRidesFragment : Fragment() {
    private var _binding: FragmentMyRidesBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyRidesAdapter
    private var rideList = mutableListOf<MyRideItem>()
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRidesBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerViewMyRides
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MyRidesAdapter(rideList)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // Prioritize arguments over ViewModel
        val argumentRides = arguments?.getSerializable("rideItems") as? ArrayList<MyRideItem>
        argumentRides?.let {
            rideList.clear()
            rideList.addAll(it)
            adapter.notifyDataSetChanged()
        }

        // Optional: Still observe ViewModel for future updates
        viewModel.rideItem.observe(viewLifecycleOwner) { rides ->
            rideList.clear()
            rideList.addAll(rides)
            adapter.notifyDataSetChanged()
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}