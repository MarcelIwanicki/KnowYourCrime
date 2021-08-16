package com.iwanickimarcel.knowyourcrime

import android.app.Application
import com.iwanickimarcel.knowyourcrime.uk.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KnowYourCrimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@KnowYourCrimeApplication)
            modules(
                networkApiModule,
                crimesAPIModule,
                crimeMapModule,
                splashScreenModule,
                settingsModule,
                screenDetailsModule
            )
        }
    }
}