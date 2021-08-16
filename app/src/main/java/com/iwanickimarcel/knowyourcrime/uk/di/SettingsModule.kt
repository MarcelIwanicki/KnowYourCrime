package com.iwanickimarcel.knowyourcrime.uk.di

import com.iwanickimarcel.knowyourcrime.uk.feature.settings.viewmodel.SettingsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel()
    }
}