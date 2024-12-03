package com.ritika.voy.home

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonParseException
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.EndPoint
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.dataclasses.OfferRideRequest
import com.ritika.voy.api.dataclasses.OfferRideResponse
import com.ritika.voy.api.dataclasses.StartPoint
import com.ritika.voy.geocoding_helper.GeocodingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class ModalBottomSheet : BottomSheetDialogFragment() {

    private var previousButton: Button? = null
    private var selectedButtonValue: String? = null
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var formattedDateTime: String? = null
    private lateinit var dateTimePicker: TextView
    private var startAddress: String? = null
    private var destinationAddress: String? = null
    private var firstMarkerLatLng: LatLng? = null
    private var secondMarkerLatLng: LatLng? = null
    private lateinit var geocodingHelper: GeocodingHelper
    private var TAG: String = "BottomSheet"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocodingHelper = GeocodingHelper(requireContext())

        // Retrieve LatLng objects from arguments
        firstMarkerLatLng = arguments?.getParcelable("firstMarkerLatLng")
        secondMarkerLatLng = arguments?.getParcelable("secondMarkerLatLng")
        Log.d(TAG, "onCreate: $firstMarkerLatLng $secondMarkerLatLng")

        // Perform reverse geocoding
        lifecycleScope.launch {
            try {
                startAddress = firstMarkerLatLng?.let {
                    reverseGeoCode(it.latitude, it.longitude)
                }
                destinationAddress = secondMarkerLatLng?.let {
                    reverseGeoCode(it.latitude, it.longitude)
                }
                Log.d(
                    TAG,
                    "Locations: $startAddress to $destinationAddress"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error during reverse geocoding: ${e.message}")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.modal_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSeatButtons(view)
        setupProceedButton(view)

        dateTimePicker = view.findViewById(R.id.date_time_picker)
        dateTimePicker.setOnClickListener {
            showDatePickerDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.isHideable = false
                behavior.skipCollapsed = false
            }
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setDimAmount(0.0f)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                showTimePickerDialog()
            },
            year,
            month,
            day
        ).apply { setTitle("Select a Date") }.show()

    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val hourIn12Format = if (selectedHour > 12) selectedHour - 12 else selectedHour
                selectedTime = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm)
                dateTimePicker.text = "$selectedDate $selectedTime"
            }, hour, minute, false // Set is24HourView to false for AM/PM
        )
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    private fun setupSeatButtons(view: View) {
        val buttons = listOf(
            view.findViewById<Button>(R.id.button_1),
            view.findViewById<Button>(R.id.button_2),
            view.findViewById<Button>(R.id.button_3),
            view.findViewById<Button>(R.id.button_4),
            view.findViewById<Button>(R.id.button_5)
        )

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                previousButton?.setBackgroundResource(R.drawable.seats_background)
                selectedButtonValue = (index + 1).toString()
                button.background = createSelectedButtonDrawable()
                previousButton = button
                Toast.makeText(
                    requireContext(), "Selected Seat: $selectedButtonValue", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private var lastFormattedDateTime: String? = null

    private fun setupProceedButton(view: View) {
        view.findViewById<Button>(R.id.proceed_button).setOnClickListener {
            formattedDateTime = formatToIso8601(selectedDate!!, selectedTime!!)

            // Show a Toast and Log the current values
            Toast.makeText(
                requireContext(),
                "Proceeding with: $formattedDateTime, $selectedButtonValue, $startAddress -> $destinationAddress",
                Toast.LENGTH_LONG
            ).show()
            Log.d(
                TAG,
                "Proceeding with: $formattedDateTime, $selectedButtonValue, $startAddress -> $destinationAddress"
            )
            if (formattedDateTime == lastFormattedDateTime) {
                Toast.makeText(
                    requireContext(),
                    "You already have a ride scheduled at this time!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "setupProceedButton: Ride creation skipped as the time is unchanged.")
                return@setOnClickListener // Exit early if time is the same
            }

            if (areFieldsValid()) {
                lifecycleScope.launch {
                    try {
                        val authToken =
                            DataStoreManager.getToken(requireContext(), "access").first()
                        if (!authToken.isNullOrEmpty()) {
                            val response = offerRide(authToken)

                            // Dismiss the bottom sheet
                            dismiss()
                            val rides = response?.data?.let { data ->
                                listOf(
                                    MyRideItem(
                                        driverName = data.driver_name,
                                        startLocation = data.start_location,
                                        endLocation = data.end_location,
                                        startTime = data.start_time,
                                        status = data.status,
                                        id = data.id,
                                        driver = data.driver
                                    )
                                )
                            } ?: emptyList()

                            val bundle = Bundle().apply {
                                putSerializable("rideItems", ArrayList(rides))
                            }

                            val intent = Intent(requireContext(), HomeActivity::class.java).apply {
                                // Pass any necessary data
                                putExtra("show_dialog", true)
                                putExtra("rideItems", ArrayList(rides))
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                            activity?.finish()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error occurred while creating ride", e)
                        Toast.makeText(
                            requireContext(),
                            "Failed to create ride: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun areFieldsValid(): Boolean {
        return !selectedDate.isNullOrEmpty() &&
                !selectedTime.isNullOrEmpty() &&
                !startAddress.isNullOrEmpty() &&
                !destinationAddress.isNullOrEmpty() &&
                !selectedButtonValue.isNullOrEmpty()
    }

    private fun createSelectedButtonDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = resources.getDimension(R.dimen.button_corner_radius)
            setColor(ContextCompat.getColor(requireActivity(), R.color.theme_color))
        }
    }

    fun formatToIso8601(date: String, time: String): String {
        val inputDateFormat = SimpleDateFormat("dd/MM/yyyyHH:mm", Locale.getDefault())
        val inputDate = inputDateFormat.parse("$date$time")
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
        return iso8601Format.format(inputDate!!)
    }

    private suspend fun reverseGeoCode(latitude: Double, longitude: Double): String? {
        return try {
            val result = geocodingHelper.reverseGeocode(latitude, longitude)
            if (result.success) result.formattedAddress else "Unknown address"
        } catch (e: Exception) {
            "Error retrieving address"
        }
    }

    private suspend fun offerRide(authToken: String): OfferRideResponse? {
        val requestBody = OfferRideRequest(
            start_location = startAddress ?: "Unknown Start Location",
            end_location = destinationAddress ?: "Unknown End Location",
            start_point = StartPoint(
                type = "Point", coordinates = listOf(
                    firstMarkerLatLng!!.longitude, firstMarkerLatLng!!.latitude
                )
            ),
            end_point = EndPoint(
                type = "Point", coordinates = listOf(
                    secondMarkerLatLng!!.longitude, secondMarkerLatLng!!.latitude
                )
            ),
            start_time = formattedDateTime ?: "Unknown Start Time",
            available_seats = selectedButtonValue!!.toInt()
        )
        Log.d("MyLocation", "MyLocation: $firstMarkerLatLng $secondMarkerLatLng ")
        Log.d(TAG, "offerRide: $requestBody")
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Inside dispatcher: ${authToken} ")
                val response = RetrofitInstance.api.offerRide("Bearer $authToken", requestBody)
                Log.d(TAG, "offerRide here: $response")
                if (response != null) {
                    if (response.success) {
                        Log.d(TAG, "Offer Ride Success: ${response.message}")
                        response
                    } else {
                        Log.e(TAG, "Offer Ride Failed: ${response.error ?: "Unknown Error"}")
                        Toast.makeText(requireContext(), "failed", Toast.LENGTH_SHORT).show()
                        null
                    }
                } else {
                    Log.e(TAG, "Offer Ride Failed: Received null response")
                    null
                }
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

}


