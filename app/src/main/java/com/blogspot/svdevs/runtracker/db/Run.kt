package com.blogspot.svdevs.runtracker.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var img: Bitmap? = null,
    var timeStamp: Long = 0L,// (time of the run )long values are easier to use while sorting as compared to date values
    var avgSpeedKMH: Float = 0f,
    var distanceM: Int = 0,
    var timeInMillis: Long = 0, // duration of the run
    var calBurnt: Int = 0
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}