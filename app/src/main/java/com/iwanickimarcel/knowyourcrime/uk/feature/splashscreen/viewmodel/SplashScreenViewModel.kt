package com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iwanickimarcel.knowyourcrime.uk.api.CrimesInfoService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SplashScreenViewModel(private val crimesInfoService: CrimesInfoService) : ViewModel() {

    private val _loadingCategories = MutableLiveData<Boolean>()
    val loadingCategories: LiveData<Boolean> = _loadingCategories

    private val _errorCode = MutableLiveData<String>()
    val errorCode: LiveData<String> = _errorCode

    fun loadCrimeCategories() {
        viewModelScope.launch {
            crimesInfoService.getRecentCrimeCategories()
                .onStart { _loadingCategories.postValue(true) }
                .catch { exception -> _errorCode.postValue(exception.message) }
                .collect { _loadingCategories.postValue(false) }
        }
    }
}