package com.iwanickimarcel.knowyourcrime.uk.feature.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories

class SettingsViewModel : ViewModel() {

    private val _countCrimesText = MutableLiveData<String>()
    val countCrimesText: LiveData<String> = _countCrimesText

    private val _dateValid = MutableLiveData<Boolean>()
    val dateValid: LiveData<Boolean> = _dateValid

    fun countCrimes(crimeCategories: CrimeCategories, allCrimes: Crimes) {
        val stringBuilder = StringBuilder()

        val newCategories = crimeCategories.toMutableList()
        val categoryNames = mutableListOf<String>()
        if (newCategories != null) {
            repeat(newCategories.size) { i ->
                crimeCategories.get(i)?.name?.lowercase()
                    ?.let { categoryNames.add(it.replace(" ", "-")) }
            }
        }
        categoryNames.forEach { crimeCategoriesItem ->
            var count = 0
            if (crimeCategoriesItem != "all-crime") {
                count = allCrimes.count { crimesItem ->
                    crimesItem.category == crimeCategoriesItem || (crimesItem.category == "violent-crime" && crimeCategoriesItem == "violence-and-sexual-offences")
                            || (crimesItem.category == "criminal-damage-arson" && crimeCategoriesItem == "criminal-damage-and-arson")
                }!!
                stringBuilder.append(
                    "${
                        crimeCategoriesItem.replaceFirstChar {
                            it.uppercase()
                        }.replace('-', ' ')
                    } = $count \n"
                )
            }
        }

        _countCrimesText.value = stringBuilder.toString()
    }

    fun validateDate(dateText: String) {
        if (dateText.length == 7 && (dateText.substring(4, 5) == "-" && (dateText.substring(0, 4)
                .toInt() <= 2021) && (dateText.substring(0, 4)
                .toInt() > 2015) && (dateText.substring(5, 7)
                .toInt() < 13) && (dateText.substring(5, 7)
                .toInt() > 0))
        ) {
            if (dateText.substring(0, 4).toInt() == 2021) {
                _dateValid.value = dateText.substring(5, 7).toInt() < 6
            } else {
                _dateValid.value = true
            }
        } else {
            _dateValid.value = false
        }
    }
}