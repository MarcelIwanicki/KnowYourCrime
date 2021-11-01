package com.iwanickimarcel.knowyourcrime.uk.feature.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel : ViewModel() {

    companion object {
        private val matchingCrimes = mapOf(
            "violent-crime" to "violence-and-sexual-offences",
            "criminal-damage-arson" to "criminal-damage-and-arson"
        )

        private const val DATE_FORMAT = "yyyy-MM-dd"
        private const val MINIMUM_FILTER_YEAR = 2015
        private const val MINIMUM_FILTER_MONTH = 12
        private const val MINIMUM_FILTER_DAY = 1
    }

    private val _countCrimesText = MutableLiveData<String>()
    val countCrimesText: LiveData<String> = _countCrimesText

    private val _dateValid = MutableLiveData<Boolean>()
    val dateValid: LiveData<Boolean> = _dateValid

    fun setCountCrimesText(crimeCategories: CrimeCategories, allCrimes: Crimes) {
        val countCrimesText = getCountCrimesText(
            crimeCategories = crimeCategories,
            categoriesInProcessingFormat = getCategoriesInProcessingFormat(crimeCategories),
            allCrimes = allCrimes
        )
        _countCrimesText.value = countCrimesText
    }

    private fun getCountCrimesText(
        crimeCategories: CrimeCategories,
        categoriesInProcessingFormat: List<String>,
        allCrimes: Crimes
    ) = buildString {
        crimeCategories.forEachIndexed { index, _ ->
            append(crimeCategories[index].name)
            append(" = ")
            append(countSelectedCrimes(allCrimes, categoriesInProcessingFormat[index]))
            append("\n")
        }
    }

    private fun getCategoriesInProcessingFormat(crimeCategories: CrimeCategories) =
        crimeCategories.map {
            it.name.lowercase().replace(" ", "-")
        }

    private fun countSelectedCrimes(allCrimes: Crimes, selectedCategory: String) = allCrimes.count {
        it.category == selectedCategory || matchingCrimes.contains(it.category)
    }

    fun validateDate(dateText: String, dateFilteredBy: String) {
        _dateValid.value = isDateValid(dateText, dateFilteredBy)
    }

    private fun isDateValid(dateText: String, dateFilteredBy: String) = try {
        isDateCorrectlyParsed(dateText, dateFilteredBy)
    } catch (exception: Exception) {
        false
    }

    private fun isDateCorrectlyParsed(dateText: String, dateFilteredBy: String): Boolean {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.UK)
        val date = dateFormat.parse(getDateWithSampleDay(dateText))
        val filterDate = dateFormat.parse(getDateWithSampleDay(dateFilteredBy))

        date?.let { currentDate ->
            filterDate?.let { maxDate ->
                if (isDateAfterMinimum(currentDate) && isDateBeforeMaximum(currentDate, maxDate)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getDateWithSampleDay(dateWithYearAndMonthText: String) =
        "$dateWithYearAndMonthText-01"

    private fun isDateAfterMinimum(date: Date) =
        date.after(
            GregorianCalendar(
                MINIMUM_FILTER_YEAR,
                MINIMUM_FILTER_MONTH - 1,
                MINIMUM_FILTER_DAY
            ).time
        )

    private fun isDateBeforeMaximum(date: Date, maxDate: Date) =
        date.before(maxDate)
}