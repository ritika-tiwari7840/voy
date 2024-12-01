package com.ritika.voy

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.GetUserResponse

import com.ritika.voy.authentication.CreateAccount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.log

class SplashFragment : Fragment() {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                try {
                    val accessToken = DataStoreManager.getToken(requireContext(), "access").first()
                    if (accessToken != null) {
                        val userResponse = getUserData(accessToken)
                        if (userResponse.success) {
                            Log.d("User Details", "User Details: ${userResponse.user}")
                            val bundle = Bundle().apply {
                                putParcelable("user_details", userResponse.user)
                            }
                            navController.navigate(
                                R.id.action_splashFragment_to_homeActivity,
                                bundle
                            )
                        }
                    } else {
                        navController.navigate(R.id.action_splashFragment_to_continueWithEmail)
                    }
                } catch (e: Exception) {
                    Log.e("SplashFragment", "Error: ${e.message}")
                    navController.navigate(R.id.action_splashFragment_to_continueWithEmail)
                }
            }
        }, 2500)
    }

    private suspend fun getUserData(accessToken: String): GetUserResponse {
        return RetrofitInstance.api.getUserData("Bearer $accessToken")
    }
}
