package com.iwanickimarcel.knowyourcrime.uk.feature.detailsscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.util.IconFactory
import com.iwanickimarcel.knowyourcrime.uk.feature.detailsscreen.model.ScreenDetailsCrime
import kotlin.math.roundToInt

class ScreenDetailsViewModel : ViewModel() {

    private val _crimesItem = MutableLiveData<ScreenDetailsCrime>()
    val crimesItem: LiveData<ScreenDetailsCrime> = _crimesItem

    fun updateCrimesItem(crimeDetails: CrimesItem) {
        _crimesItem.value = ScreenDetailsCrime(
            crimeDetails.category.replaceFirstChar {
                it.uppercase()
            }.replace('-', ' '),
            crimeDetails.location_type,
            crimeDetails.month,
            "${crimeDetails.distanceFromGPS.roundToInt()} m",
            crimeDetails.location.latitude,
            crimeDetails.location.longitude,
            crimeDetails.id.toString(),
            crimeDetails.location.street.name,
            IconFactory.getIconIndexBasedOnCategoryName(crimeDetails.category)
        )
    }

}