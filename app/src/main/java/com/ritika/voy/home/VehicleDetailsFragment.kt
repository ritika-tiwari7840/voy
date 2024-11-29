package com.ritika.voy.home

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.UserResponseData
import com.ritika.voy.databinding.FragmentVehicleDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VehicleDetailsFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentVehicleDetailsBinding? = null
    private val binding get() = _binding!!
    private var selectedButtonValue: String? = null
    private var previousButton: Button? = null
    private var vehicleModel: String? = null
    private var vehicleNumber: String? = null
    private var totalSeats: String? = null

    private lateinit var selectedText: String
    private val filePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVehicleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_vehicleDetailsFragment_to_verifyFragment)
        }
        setupButtonListeners()

        binding.model.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.select_vehicle_model)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
                setupTextViewClickListeners(dialog)
            }
        }
        binding.Save.setOnClickListener {
            vehicleModel = binding.model.text.toString()
            vehicleNumber = binding.vehicleNumber.text.toString()
            totalSeats = selectedButtonValue
            if (vehicleModel!!.isEmpty() || vehicleNumber!!.isEmpty() || totalSeats == null) {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Loading...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                lifecycleScope.launch {
                    try {
                        addVehicleDetails(
                            requireContext(),
                            vehicleModel,
                            vehicleNumber,
                            totalSeats
                        )
                    } catch (e: Exception) {
                        handleError(e)
                    } finally {
                        delay(1000)
                        progressDialog.dismiss()
                        navController.navigate(R.id.action_vehicleDetailsFragment_to_verifyFragment)
                    }
                }
            }
        }

    }

    private fun setupButtonListeners() {
        val buttons = listOf(
            binding.seatOne,
            binding.seatTwo,
            binding.seatThree,
            binding.seatFour,
            binding.seatFive,
            binding.seatSix,
            binding.seatSeven
        )
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                previousButton?.let {
                    it.setBackgroundResource(R.drawable.seats_background)
                }
                selectedButtonValue = (index + 1).toString()
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = resources.getDimension(R.dimen.button_corner_radius)
                    setColor(ContextCompat.getColor(requireActivity(), R.color.theme_color))
                }
                button.background = drawable

                previousButton = button
            }
        }
    }

    fun setupTextViewClickListeners(dialog: Dialog) {
        // List of TextViews by ID
        val textViews = listOf(
            dialog.findViewById<TextView>(R.id.lexus),
            dialog.findViewById<TextView>(R.id.Volvo),
            dialog.findViewById<TextView>(R.id.Audi),
            dialog.findViewById<TextView>(R.id.Honda),
            dialog.findViewById<TextView>(R.id.Porsche),
            dialog.findViewById<TextView>(R.id.Kia),
            dialog.findViewById<TextView>(R.id.Bajaj)
        )
        for (textView in textViews) {
            textView.setOnClickListener {
                selectedText = textView.text.toString()
                Toast.makeText(requireContext(), "Selected: $selectedText", Toast.LENGTH_SHORT)
                    .show()
                binding.model.setText(selectedText)
                dialog.dismiss()
            }
        }
    }


    private suspend fun addVehicleDetails(
        context: Context,
        vehicleModel: String?,
        vehicleNumber: String?,
        vehicleSeat: String?,
    ) =
        withContext(Dispatchers.IO) {
            try {
                val token = DataStoreManager.getToken(context, "access").first()
                if (!token.isNullOrEmpty()) {
                    val response = RetrofitInstance.api.addVehicleDetails(
                        token = "Bearer $token",
                        vehicleModel = vehicleModel,
                        vehicleNumber = vehicleNumber,
                        vehicleSeats = vehicleSeat
                    )
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            showToast("Vehicle Details updated successfully")
                            Log.d("VehicleUpdate", "Update response: $response, ")
                        } else {
                            Log.e(
                                "VehicleUpdate",
                                "Error Adding Emergency Contact: ${response.message} "
                            )
                            showToast("Error Adding Emergency Contact: ${response.message}")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Token is missing")
                    }
                }
            } catch (e: Exception) {
                Log.e("VehicleUpdate", "Error Adding Emergency Contact ", e)
                withContext(Dispatchers.Main) {
                    showToast("An error occurred: ${e.message}")
                }
            }
        }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleError(error: Exception) {
        Log.e("VehicleUpdate", "Error in EditInfo", error)
        showToast("Error: ${error.message}")
    }

}