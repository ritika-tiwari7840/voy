package com.ritika.voy.home

import android.app.TimePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ritika.voy.R
import java.util.Calendar

class ModalBottomSheet : BottomSheetDialogFragment() {

    private var previousButton: Button? = null
    private var selectedButtonValue: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startLocation = arguments?.getString("startLocation")
        val endLocation = arguments?.getString("destinationLocation")
        val firstMarkerLatLng = arguments?.getString("firstMarkerLatLng")
        val secondMarkerLatLng = arguments?.getString("secondMarkerLatLng")
        Log.d(
            "BottomSheet",
            "onViewCreated: $startLocation $endLocation $firstMarkerLatLng $secondMarkerLatLng"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.modal_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonListeners(view)

    }

    private fun setupButtonListeners(view: View) {
        // Collect button references
        val buttons = listOf(
            view.findViewById<Button>(R.id.button_1),
            view.findViewById<Button>(R.id.button_2),
            view.findViewById<Button>(R.id.button_3),
            view.findViewById<Button>(R.id.button_4),
            view.findViewById<Button>(R.id.button_5)
        )

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                // Reset the previous button
                previousButton?.setBackgroundResource(R.drawable.seats_background)

                // Update the selected button value
                selectedButtonValue = (index + 1).toString()

                // Set a new background for the selected button
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = resources.getDimension(R.dimen.button_corner_radius)
                    setColor(ContextCompat.getColor(requireActivity(), R.color.theme_color))
                }
                button.background = drawable

                previousButton = button

                // Display the selected value as a toast (or send it elsewhere)
                Toast.makeText(
                    requireContext(),
                    "Selected Seat: $selectedButtonValue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Handle proceed button
        view.findViewById<Button>(R.id.proceed_button)?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Proceeding with seat $selectedButtonValue",
                Toast.LENGTH_SHORT
            ).show()
            dismiss() // Close the bottom sheet
        }
    }

    private fun openTimePicker(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val formattedDateTime = String.format(
                "%04d-%02d-%02d %02d:%02d",
                year, month + 1, day, selectedHour, selectedMinute
            )
            val dateTime = view?.findViewById<Button>(R.id.date_time_picker)
            if (dateTime != null) {
                dateTime.text = formattedDateTime
            }
            Toast.makeText(requireContext(), "Selected: $formattedDateTime", Toast.LENGTH_SHORT)
                .show()
        }, hour, minute, true).show()
    }
}
