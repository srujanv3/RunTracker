package com.blogspot.svdevs.runtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.blogspot.svdevs.runtracker.repository.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepo: MainRepo
): ViewModel() {

    val totalTimeRun = mainRepo.getTotalTimeInMillis()
    val totalDistance = mainRepo.getTotalDistance()
    val totalCalsBurnt = mainRepo.getTotalCalsBurnt()
    val totalAvgSpeed = mainRepo.getTotalAvgSpeed()

    val runsSortedByDate = mainRepo.getAllRunSortByDate()
}