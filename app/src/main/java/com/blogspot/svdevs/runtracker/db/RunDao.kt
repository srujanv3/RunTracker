package com.blogspot.svdevs.runtracker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface RunDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timeStamp DESC")
    fun getAllRunSortByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceM DESC")
    fun getAllRunSortByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedKMH DESC")
    fun getAllRunSortBySpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunSortByTime(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY calBurnt DESC")
    fun getAllRunSortByCalsBurnt(): LiveData<List<Run>>

    //functions to get totals for calculating statistics

    @Query("SELECT AVG(avgSpeedKMH) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(calBurnt) FROM running_table")
    fun getTotalCalsBurnt(): LiveData<Int>

    @Query("SELECT SUM(distanceM) FROM running_table")
    fun getTotalDistance(): LiveData<Int>
}