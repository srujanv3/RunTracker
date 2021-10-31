package com.blogspot.svdevs.runtracker.utils

import android.content.Context
import android.widget.TextView
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    private val tvDate: TextView
        get() = findViewById(R.id.tvDate)

    private val tvAvgSpeed: TextView
        get() = findViewById(R.id.tvAvgSpeed)

    private val tvDistance: TextView
        get() = findViewById(R.id.tvDistance)

    private val tvDuration: TextView
        get() = findViewById(R.id.tvDuration)

    private val tvCalories: TextView
        get() = findViewById(R.id.tvCaloriesBurnt)

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())

    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val calender = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calender.time)

        val avgSpeed = "${run.avgSpeedKMH}km/h"
        tvAvgSpeed.text = avgSpeed

        val distanceInKM = "${run.distanceM / 1000f}km"
        tvDistance.text = distanceInKM

        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val calsBurnt = "${run.calBurnt}kcal"
        tvCalories.text = calsBurnt
    }

}