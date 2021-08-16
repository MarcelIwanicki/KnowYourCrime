package com.iwanickimarcel.knowyourcrime.uk.di

import com.iwanickimarcel.knowyourcrime.uk.api.CrimesInfoService
import com.iwanickimarcel.knowyourcrime.uk.api.CrimesRepositoryAPI
import com.iwanickimarcel.knowyourcrime.uk.api.CrimesRepositoryAPIFactory
import org.koin.dsl.module
import retrofit2.Retrofit

val crimesAPIModule = module {
    single {
        CrimesRepositoryAPIFactory().getRetrofit()
    }

    single {
        get<Retrofit>().create(CrimesRepositoryAPI::class.java)
    }

    single {
        CrimesInfoService(get(), get(), get())
    }
}