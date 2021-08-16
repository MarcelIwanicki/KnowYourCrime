package com.iwanickimarcel.knowyourcrime.uk.di

import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository.CrimeCategoriesRepositoryAPI
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository.LastUpdatedRepositoryAPI
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository.LocalCrimeCategoriesRepository
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository.LocalLastUpdatedRepository
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.viewmodel.SplashScreenViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val splashScreenModule = module {
    single<CrimeCategoriesRepositoryAPI> {
        LocalCrimeCategoriesRepository(get())
    }

    single<LastUpdatedRepositoryAPI> {
        LocalLastUpdatedRepository(get())
    }

    viewModel {
        SplashScreenViewModel(get())
    }
}