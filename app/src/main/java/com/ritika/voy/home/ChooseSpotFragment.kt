package com.ritika.voy.home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.ritika.voy.BuildConfig
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentChooseSpotBinding

class ChooseSpotFragment : Fragment() {
    private lateinit var _binding: FragmentChooseSpotBinding
    private val binding get() = _binding
    private lateinit var navController: NavController
    private var startLocation: Place? = null
    private var destinationLocation: Place? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentChooseSpotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAP_API_KEY)
        }

        // Set up autocomplete for start and destination
        setupAutocomplete()

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_chooseSpotFragment_to_home)
        }

        binding.doneButton.setOnClickListener {
            if (startLocation != null && destinationLocation != null) {
                Toast.makeText(
                    requireContext(),
                    "Start: ${startLocation!!.name}, Destination: ${destinationLocation!!.name}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select both start and destination locations",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.setOnMap.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Navigate to maps to find current location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupAutocomplete() {
        // Autocomplete for Start Location
        val startAutocomplete =
            childFragmentManager.findFragmentById(R.id.start_address) as AutocompleteSupportFragment
        startAutocomplete.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        startAutocomplete.setHint("Pickup Location")
        startAutocomplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                startLocation = place
                binding.start.setText(place.name)
                Log.i("ChooseSpot", "Start location selected: ${place.name}, ${place.latLng}")
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${status.statusMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Autocomplete for Destination
        val destinationAutocomplete =
            childFragmentManager.findFragmentById(R.id.destination_address) as AutocompleteSupportFragment
        destinationAutocomplete.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        destinationAutocomplete.setHint("Drop Location")
        destinationAutocomplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationLocation = place
                binding.destination.setText(place.name)
                Log.i("ChooseSpot", "Destination location selected: ${place.name}, ${place.latLng}")
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${status.statusMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
