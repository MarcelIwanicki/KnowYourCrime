package com.iwanickimarcel.knowyourcrime.uk.di

import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val networkApiModule = module {
    single {
        OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .cache(null)
            .build()
    }
}