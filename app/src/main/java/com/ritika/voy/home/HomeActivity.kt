package com.ritika.voy.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ritika.voy.R
import com.ritika.voy.api.datamodels.SharedViewModel
import com.ritika.voy.api.dataclasses.MyRideItem
import com.ritika.voy.api.dataclasses.UserXX
import com.ritika.voy.databinding.ActivityHomeBinding
import kotlin.math.log

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        // Retrieve user data from Intent and update ViewModel if necessary
        val user: UserXX? = intent.getParcelableExtra("user_details")
        if (user != null && sharedViewModel.user == null) {
            sharedViewModel.user = user
        }
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavMenu.setupWithNavController(navController)
        handleIntentExtras(intent)
        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
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
    }

    private fun handleIntentExtras(intent: Intent) {
        val showDialog = intent.getBooleanExtra("show_dialog", false)
        val rides = intent.getSerializableExtra("rideItems") as? ArrayList<MyRideItem>

        if (showDialog) {
            showRideReachedDialog(intent)
        }
    }

    private fun showRideReachedDialog(intent: Intent) {
        val rides = intent.getSerializableExtra("rideItems") as? ArrayList<MyRideItem>
        Log.d("Rides", "showRideReachedDialog: $rides")
        rides?.let {
            sharedViewModel.addRideItems(it)
        }
        val customView = LayoutInflater.from(this).inflate(R.layout.ride_posted, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(customView)
            .setCancelable(false)
            .create()

        customView.setOnClickListener {
            dialog.dismiss()
            navController.navigate(R.id.myRidesFragment)
        }
        dialog.show()
    }


    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.home) {
            finishAffinity() // Exit the app completely
        } else {
            super.onBackPressed()
        }
    }
}
