package com.blogspot.svdevs.runtracker.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blogspot.svdevs.runtracker.db.Run
import com.blogspot.svdevs.runtracker.repository.MainRepo
import com.blogspot.svdevs.runtracker.utils.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepo: MainRepo
): ViewModel() {

    val runsSortedByDate = mainRepo.getAllRunSortByDate()
    val runSortByDistance = mainRepo.getAllRunSortByDistance()
    val runsSortedByTime = mainRepo.getAllRunSortByTime()
    val runsSortedByCalsBurnt = mainRepo.getAllRunSortByCalsBurnt()
    val runsSortedBySpeed = mainRepo.getAllRunSortBySpeed()

    //merge multiple live data
    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE //default sort type

    init {
        //for sort by date
        runs.addSource(runsSortedByDate) { result ->
            if(sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        //for sort by time
        runs.addSource(runsSortedByTime) { result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
        // for sort by avg speed
        runs.addSource(runsSortedBySpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        // for sort by calories burnt
        runs.addSource(runsSortedByCalsBurnt) { result ->
            if(sortType == SortType.CALORIES_BURNT) {
                result?.let { runs.value = it }
            }
        }
        // for sort by distance
        runs.addSource(runSortByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTime.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedBySpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNT -> runsSortedByCalsBurnt.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepo.insertRun(run)
    }
}