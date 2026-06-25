package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.personalaccountant.data.prefs.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AmortizationSimulatorViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _loanAmountStr = MutableStateFlow(preferenceManager.getSimLoan())
    val loanAmountStr = _loanAmountStr.asStateFlow()

    private val _interestRateStr = MutableStateFlow(preferenceManager.getSimInterest())
    val interestRateStr = _interestRateStr.asStateFlow()

    private val _termMonthsStr = MutableStateFlow(preferenceManager.getSimTerm())
    val termMonthsStr = _termMonthsStr.asStateFlow()

    private val _monthlySalaryStr = MutableStateFlow(preferenceManager.getSimSalary())
    val monthlySalaryStr = _monthlySalaryStr.asStateFlow()

    fun updateLoanAmount(value: String) {
        _loanAmountStr.value = value
        preferenceManager.setSimLoan(value)
    }

    fun updateInterestRate(value: String) {
        _interestRateStr.value = value
        preferenceManager.setSimInterest(value)
    }

    fun updateTermMonths(value: String) {
        _termMonthsStr.value = value
        preferenceManager.setSimTerm(value)
    }

    fun updateMonthlySalary(value: String) {
        _monthlySalaryStr.value = value
        preferenceManager.setSimSalary(value)
    }
}
