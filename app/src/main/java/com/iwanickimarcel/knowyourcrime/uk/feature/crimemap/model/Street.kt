package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Street(
    val id: Int = 0,
    val name: String = ""
) : Parcelable