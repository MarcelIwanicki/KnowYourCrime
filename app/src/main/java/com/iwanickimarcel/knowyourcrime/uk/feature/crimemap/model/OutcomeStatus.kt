package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OutcomeStatus(
    val category: String = "",
    val date: String = ""
) : Parcelable