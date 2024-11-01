package com.ritika.voy

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ritika.voy.R
import com.ritika.voy.authentication.VerifyEmailFragment
import com.ritika.voy.authentication.VerifyPhoneFragment
import com.ritika.voy.signup.CreateAccount

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            parentFragmentManager.beginTransaction()
                .replace(R.id.splash,VerifyEmailFragment())
                .commit()
        }, 2000)
    }
}
