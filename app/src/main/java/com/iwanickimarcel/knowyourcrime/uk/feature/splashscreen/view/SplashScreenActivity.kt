package com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iwanickimarcel.knowyourcrime.R
import com.iwanickimarcel.knowyourcrime.uk.MainActivity
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.viewmodel.SplashScreenViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class SplashScreenActivity : AppCompatActivity() {

    private val viewModel: SplashScreenViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        viewModel.loadCrimeCategories()
        viewModel.errorCode.observe(this) { error ->
            Snackbar.make(findViewById(R.id.splash_screen), error, Snackbar.LENGTH_LONG).show()
        }
        viewModel.loadingCategories.observe(this) { loading ->
            if (loading == false) {
                routeToAppropriatePage()
            }
        }
    }

    private fun routeToAppropriatePage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}