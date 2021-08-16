package com.iwanickimarcel.knowyourcrime.uk.di

import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.repository.CrimesRepository
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val crimeMapModule = module {
    single { CrimesRepository(get()) }

    viewModel { CrimeMapFragmentViewModel(get()) }
}