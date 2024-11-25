package com.ritika.voy.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.ritika.voy.BuildConfig
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentChooseSpotBinding

class ChooseSpotFragment : Fragment() {

    private lateinit var binding: FragmentChooseSpotBinding
    private lateinit var navController: NavController
    private lateinit var role:String
    private val placesClient by lazy { Places.createClient(requireContext()) }
    private val startAdapter by lazy {
        SuggestionsAdapter { suggestion ->
            onSuggestionClicked(
                suggestion,
                isStart = true
            )
        }
    }
    private val destinationAdapter by lazy {
        SuggestionsAdapter { suggestion ->
            onSuggestionClicked(
                suggestion,
                isStart = false
            )
        }
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChooseSpotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        role = arguments?.getString("role") ?: "passenger"
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAP_API_KEY)
        }

        setupRecyclerViews()
        setupListeners()

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_chooseSpotFragment_to_home)
        }

        binding.doneButton.setOnClickListener {

            if (binding.start.text.toString().isBlank() || binding.destination.text.toString()
                    .isBlank()
            ) {
                Toast.makeText(requireContext(), "Please enter both the fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val bundle = Bundle()
                bundle.putString("startLocation", binding.start.text.toString())
                bundle.putString("destinationLocation", binding.destination.text.toString())
                bundle.putString("role",role)
                navController.navigate(R.id.action_chooseSpotFragment_to_mapActivity, bundle)
            }
        }
        binding.setOnMap.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("role",role)
            navController.navigate(R.id.action_chooseSpotFragment_to_mapActivity,bundle)
        }
    }

    private fun setupRecyclerViews() {
        binding.startRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = startAdapter
        }

        binding.destinationRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = destinationAdapter
        }

    }

    private fun setupListeners() {
        binding.start.addTextChangedListener { text ->
            fetchPlaceSuggestions(text.toString()) { predictions ->
                startAdapter.submitList(predictions)
                binding.startRecyclerView.visibility =
                    if (checkCursorAndShowToast(binding.start)) View.GONE else View.VISIBLE
                if (predictions.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        binding.destination.addTextChangedListener { text ->
            fetchPlaceSuggestions(text.toString()) { predictions ->
                destinationAdapter.submitList(predictions)
                binding.destinationRecyclerView.visibility =
                    if (checkCursorAndShowToast(binding.destination)) View.GONE else View.VISIBLE
                if (predictions.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun fetchPlaceSuggestions(
        query: String,
        callback: (List<AutocompletePrediction>) -> Unit,
    ) {
        if (query.isBlank()) {
            callback(emptyList())
            return
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountry("IN") // Adjust or remove for global predictions
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                callback(response.autocompletePredictions)
                Log.d(
                    "ChooseSpotFragment",
                    "Fetched ${response.autocompletePredictions.size} predictions"
                )
            }
            .addOnFailureListener { exception ->
                Log.e("ChooseSpotFragment", "Error fetching place predictions", exception)
                callback(emptyList())
            }
    }

    private fun onSuggestionClicked(suggestion: AutocompletePrediction, isStart: Boolean) {
        binding.startRecyclerView.visibility = View.GONE
        binding.destinationRecyclerView.visibility = View.GONE
        if (isStart) {
            binding.start.setText(suggestion.getPrimaryText(null).toString())
        } else {
            binding.destination.setText(suggestion.getPrimaryText(null).toString())
        }
    }


    fun checkCursorAndShowToast(editText: EditText): Boolean {
        val cursorStart = editText.selectionStart
        val text = editText.text.toString()

        if (cursorStart == 0 && text.isNotEmpty()) {
            binding.startRecyclerView.visibility = View.GONE
            return true
        }
        return false
    }
}
