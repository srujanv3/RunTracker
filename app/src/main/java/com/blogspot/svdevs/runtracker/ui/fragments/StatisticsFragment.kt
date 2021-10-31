package com.blogspot.svdevs.runtracker.ui.fragments

import android.graphics.Color
import android.graphics.Color.WHITE
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.utils.CustomMarkerView
import com.blogspot.svdevs.runtracker.utils.TrackingUtility
import com.blogspot.svdevs.runtracker.ui.viewmodel.StatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val tvTotalTime: TextView?
        get() = requireActivity().findViewById(R.id.tvTotalTime)

    private val tvTotalDistance: TextView?
        get() = requireActivity().findViewById(R.id.tvTotalDistance)

    private val tvTotalCals: TextView?
        get() = requireActivity().findViewById(R.id.tvTotalCalories)

    private val tvAvgSpeed: TextView?
        get() = requireActivity().findViewById(R.id.tvAverageSpeed)

    private val barChart: BarChart?
        get() = requireActivity().findViewById(R.id.barChart)

    private val viewModel: StatisticsViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setUpBarChart()
    }

    private fun subscribeToObservers() {

        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime?.text = totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDist = "${totalDistance}km"
                tvTotalDistance?.text = totalDist
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedStr = "${avgSpeed}km/h"
                tvAvgSpeed?.text = avgSpeedStr
            }
        })
        viewModel.totalCalsBurnt.observe(viewLifecycleOwner, Observer {
            it?.let {
                val calories = "${it}kcal"
                tvTotalCals?.text = calories
            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeed = it.indices.map { i -> BarEntry(i.toFloat(),it[i].avgSpeedKMH) }
                val barDataSet = BarDataSet(allAvgSpeed,"Avg Speed Over Time").apply {
                    valueTextColor = WHITE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barChart!!.data = BarData(barDataSet)
                barChart!!.marker = CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                barChart!!.invalidate()
            }
        })

    }

    private fun setUpBarChart() {
        barChart!!.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
//            setDrawGridLines(false)
        }
        barChart!!.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
//            setDrawGridLines(false)
        }

        barChart!!.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
//            setDrawGridLines(false)
        }
        barChart?.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }

}