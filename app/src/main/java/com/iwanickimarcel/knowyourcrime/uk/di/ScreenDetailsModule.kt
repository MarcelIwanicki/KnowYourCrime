package com.iwanickimarcel.knowyourcrime.uk.di

import com.iwanickimarcel.knowyourcrime.uk.feature.detailsscreen.viewmodel.ScreenDetailsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val screenDetailsModule = module {
    viewModel { ScreenDetailsViewModel() }
}