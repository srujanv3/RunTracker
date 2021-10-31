package com.blogspot.svdevs.runtracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.db.Run
import com.blogspot.svdevs.runtracker.utils.TrackingUtility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    //Implementing list differ
    val diffCallBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    fun submitList(list: List<Run>) = differ.submitList(list)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_run,parent,false))
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(holder.imgView)

            val calender = Calendar.getInstance().apply {
                timeInMillis = run.timeStamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy",Locale.getDefault())
            holder.tvDate.text = dateFormat.format(calender.time)

            val avgSpeed = "${run.avgSpeedKMH}km/h"
            holder.tvAvgSpeed.text = avgSpeed

            val distanceInKM = "${run.distanceM / 1000f}km"
            holder.tvDistance.text = distanceInKM

            holder.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val calsBurnt = "${run.calBurnt}kcal"
            holder.tvCalories.text = calsBurnt
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class RunViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var imgView = itemView.findViewById<ImageView>(R.id.ivRunImage)
        var tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        var tvAvgSpeed = itemView.findViewById<TextView>(R.id.tvAvgSpeed)
        var tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        var tvDistance = itemView.findViewById<TextView>(R.id.tvDistance)
        var tvCalories = itemView.findViewById<TextView>(R.id.tvCalories)
    }


}