package com.ritika.voy.home

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.databinding.ActivityHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var firstName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//      enableEdgeToEdge()
        lifecycleScope.launch {
            DataStoreManager.getUserData(this@HomeActivity, "firstName").first().let {
                firstName = it.toString()
                if (firstName.isEmpty()) {
                    firstName = "User"
                }
            }
        }

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

        binding.chat.setOnClickListener {
            Toast.makeText(this, "Chat Screen opens", Toast.LENGTH_SHORT).show()
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.verification)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
            val textView = dialog.findViewById<TextView>(R.id.user_name)
            val button = dialog.findViewById<Button>(R.id.get_verification)
            textView.text =
                "$firstName, you’re only missing few steps\n from having a Verified profile."
            button.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.home) {
            // Exit the app when at the "home" destination
            finishAffinity() // Exits the app completely
        } else {
            // Navigate to the "home" destination
            navController.navigate(R.id.home)
        }
    }


}
