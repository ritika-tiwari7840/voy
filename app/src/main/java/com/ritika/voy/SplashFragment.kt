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
                            id = userResponse.user.id.toString()?:"",
                            email = userResponse.user.email?:"",
                            firstName = userResponse.user.first_name.toString()?:"",
                            lastName = userResponse.user.last_name.toString()?:"",
                            fullName = userResponse.user.full_name.toString()?:"",
                            createdAt = userResponse.user.created_at?:"",
                            phoneNo = userResponse.user.phone_number.toString()?:"",
                            gender = userResponse.user.gender.toString()?:"",
                            emergencyContact = userResponse.user.emergency_contact_phone.toString()?:"",
                            profilePhoto = userResponse.user.profile_photo.toString()?:"",
                            ratingAsDriver = "5.0",
                            ratingAsPassenger = "5.0",
                            isDriverVerified = userResponse.user.toString()?:""
                        )
                        DataStoreManager.getUserData(requireContext(), "email").first().let {
                            val email = it.toString()
                            Toast.makeText(requireContext(), "token $email", Toast.LENGTH_SHORT)
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
