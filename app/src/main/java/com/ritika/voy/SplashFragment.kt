package com.ritika.voy

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
                val accessToken = DataStoreManager.getToken(requireContext(), "access").first()
                if (accessToken != null) {
                    val userResponse = getUserData(accessToken)
                    if (userResponse.success) {
                        DataStoreManager.SaveUserData(
                            requireContext(),
                            userResponse.user.id!!.toString(),
                            userResponse.user.email!!,
                            userResponse.user.first_name!!,
                            userResponse.user.last_name!!,
                            userResponse.user.full_name!!,
                            userResponse.user.created_at!!,
                            userResponse.user.phone_number!!,
                            userResponse.user.gender!!.toString(),
                            userResponse.user.emergency_contact_phone!!.toString(),
                            userResponse.user.profile_photo!!.toString(),
                            userResponse.user.rating_as_driver!!.toString(),
                            userResponse.user.rating_as_passenger!!.toString(),
                            userResponse.user.is_driver_verified!!.toString()
                        )
                        DataStoreManager.getUserData(requireContext(), "isDriverVerified").first().let {
                            val verified = it.toString()
                            Toast.makeText(requireContext(), "token $verified", Toast.LENGTH_SHORT)
                                .show()
                        }
                        navController.navigate(R.id.action_splashFragment_to_homeActivity)
                    }
                } else {
                    navController.navigate(R.id.action_splashFragment_to_continueWithEmail)
                }
            }
        }, 2500)
    }

    private suspend fun getUserData(accessToken: String): GetUserResponse {
        return RetrofitInstance.api.getUserData("Bearer $accessToken")
    }
}
