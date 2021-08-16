package com.iwanickimarcel.knowyourcrime.uk.feature.settings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iwanickimarcel.knowyourcrime.databinding.FragmentSettingsScreenBinding
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import com.iwanickimarcel.knowyourcrime.uk.feature.settings.viewmodel.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

private const val DEFAULT_DATE = "2021-05"

class SettingsScreenFragment : Fragment() {

    private val crimeMapViewModel by sharedViewModel<CrimeMapFragmentViewModel>()
    private val settingsViewModel by viewModel<SettingsViewModel>()

    private lateinit var _binding: FragmentSettingsScreenBinding
    private val binding get() = _binding

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsScreenBinding.inflate(inflater, container, false)
        bottomSheetBehavior =
            BottomSheetBehavior.from(binding.bottomSheetSettings.bottomSheetSettings)

        settingsViewModel.countCrimesText.observe(viewLifecycleOwner) {
            binding.bottomSheetSettings.textViewCoutedCrimes.text = it
        }

        settingsViewModel.dateValid.observe(viewLifecycleOwner) {
            binding.buttonBack.isEnabled = it
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigateToMap()
        setCheckBoxAndEditText()
        validDateEditText()
        buttonCountCrimesListener()
        navigateToAboutUs()

        crimeMapViewModel.crimeCategories.value?.let { categories ->
            crimeMapViewModel.allCrimes.value?.let { crimes ->
                settingsViewModel.countCrimes(
                    categories,
                    crimes
                )
            }
        }
    }

    private fun navigateToMap() {
        binding.buttonBack.setOnClickListener {
            crimeMapViewModel.setDataFilteredBy(binding.editTextDateFrom.editText?.text.toString())
            crimeMapViewModel.resetView = true

            val action =
                SettingsScreenFragmentDirections.actionSettingsScreenFragmentToCrimeMapFragment()
            findNavController().navigate(action)
        }
    }

    private fun setCheckBoxAndEditText() {
        if (binding.checkboxUpToDate.isChecked) {
            binding.editTextDateFrom.editText?.isEnabled = false
            binding.editTextDateFrom.editText?.setText(DEFAULT_DATE)
        } else {
            binding.editTextDateFrom.editText?.setText(crimeMapViewModel.dateFilteredBy.value.toString())
            binding.editTextDateFrom.editText?.isEnabled = true
        }

        binding.checkboxUpToDate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.editTextDateFrom.editText?.isEnabled = false
                binding.editTextDateFrom.editText?.setText(DEFAULT_DATE)
            } else {
                binding.editTextDateFrom.editText?.isEnabled = true
            }
        }
    }

    private fun validDateEditText() {
        binding.editTextDateFrom.editText?.doAfterTextChanged {
            settingsViewModel.validateDate(it.toString())
        }
    }

    private fun buttonCountCrimesListener() {
        binding.buttonCountCrimes.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            else
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun navigateToAboutUs() {
        binding.buttonAboutUs.setOnClickListener {
            val action =
                SettingsScreenFragmentDirections.actionSettingsScreenFragmentToAboutUsFragment()
            findNavController().navigate(action)
        }
    }

}