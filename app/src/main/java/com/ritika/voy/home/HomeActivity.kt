package com.ritika.voy.home

import com.ritika.voy.api.datamodels.SharedViewModel
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ritika.voy.R
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.dataclasses.UserXX
import com.ritika.voy.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var firstName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//      enableEdgeToEdge()

        // Retrieve data from Intent
        val user: UserXX? = intent.getParcelableExtra("user_details")
        // Initialize ViewModel
        var sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        sharedViewModel.user = user


        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavMenu.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home, R.id.history, R.id.eco, R.id.profile -> {
                    binding.bottomNavMenu.visibility = android.view.View.VISIBLE
                }

                else -> {
                    binding.bottomNavMenu.visibility = android.view.View.GONE
                }
            }
            when (destination.id) {
                R.id.home -> {
                    binding.chat.visibility = android.view.View.VISIBLE
                }

                else -> {
                    binding.chat.visibility = android.view.View.GONE
                }
            }
        }
        if (intent.getBooleanExtra("show_dialog", false)) {
            showRideReachedDialog()
        }
    }

    private fun showRideReachedDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val rides =
            intent.getSerializableExtra("rideItems") as? ArrayList<MyRideItem> // Get ride data

        dialogBuilder.apply {
            setTitle("Ride Created")
            setMessage("Your ride has been successfully created!")
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()

                // Pass data to MyRidesFragment
                val bundle = Bundle()
                rides?.let {
                    bundle.putSerializable("rideItems", it) // Pass ride details
                }

                // Navigate to MyRidesFragment
                navController.navigate(R.id.myRidesFragment, bundle)
            }

            // Optional: Show first ride details in the message
            rides?.firstOrNull()?.let { ride ->
                setMessage("Ride from ${ride.startLocation} to ${ride.endLocation}")
            }
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.home) {
            // Exit the app when at the "home" destination
            finishAffinity() // Exits the app completely
        } else {
            super.onBackPressed()
        }
    }


}
