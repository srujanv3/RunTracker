package com.blogspot.svdevs.runtracker.repository

import com.blogspot.svdevs.runtracker.db.Run
import com.blogspot.svdevs.runtracker.db.RunDao
import javax.inject.Inject

class MainRepo @Inject constructor(
    val runDao: RunDao
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)
    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunSortByDate() = runDao.getAllRunSortByDate()
    fun getAllRunSortByCalsBurnt() = runDao.getAllRunSortByCalsBurnt()
    fun getAllRunSortByDistance() = runDao.getAllRunSortByDistance()
    fun getAllRunSortBySpeed() = runDao.getAllRunSortBySpeed()
    fun getAllRunSortByTime() = runDao.getAllRunSortByTime()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()
    fun getTotalCalsBurnt() = runDao.getTotalCalsBurnt()
    fun getTotalDistance() = runDao.getTotalDistance()
    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

}