package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Location(
    val latitude: String = "",
    val longitude: String = "",
    val street: Street = Street()
) : Parcelable