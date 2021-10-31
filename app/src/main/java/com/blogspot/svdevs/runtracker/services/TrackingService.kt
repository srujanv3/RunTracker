package com.blogspot.svdevs.runtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.blogspot.svdevs.runtracker.utils.Constants.ACTION_START_OR_RESUME
import com.blogspot.svdevs.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.blogspot.svdevs.runtracker.utils.Constants.FASTEST_LOCATION_INTERVAL
import com.blogspot.svdevs.runtracker.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.blogspot.svdevs.runtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.blogspot.svdevs.runtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.blogspot.svdevs.runtracker.utils.Constants.NOTIFICATION_ID
import com.blogspot.svdevs.runtracker.utils.Constants.TIMER_UPDATE_INTERVAL
import com.blogspot.svdevs.runtracker.utils.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceDestroyed = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSec = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()

        val isTracking = MutableLiveData<Boolean>()

        // save coordinates to later draw run path on the map >> List of List of coordinates
        val pathPoints = MutableLiveData<PolyLines>()
        //val pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()
    }

    private fun postInitialsToLiveData() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSec.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialsToLiveData()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resume service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Pause service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop service")
                    destroyService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // setting up the run timer
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeTotalRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L //last second in millis

    private fun startTimer() {
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                //add new laptime to the total time
                timeRunInMillis.postValue(timeTotalRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSec.postValue(timeRunInSec.value!! + 1)
                    lastSecondTimestamp += 1000
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            //add last lap's time to the total time
            timeTotalRun += lapTime
        }
    }

    private fun destroyService() {
        serviceDestroyed = true
        isFirstRun = true
        pauseService()
        postInitialsToLiveData()
        stopForeground(true)
        stopSelf()
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    //update run time in the notification
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            //pause the service
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            // resume the service
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //remove previous actions before updating the notification
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceDestroyed){
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }

    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = com.google.android.gms.location.LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    //when new location is retrived, add it to the last of the polyline list
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            if (isTracking.value!!) {
                result?.locations?.let { locs ->
                    for (location in locs) {
                        addPoints(location)
                        Timber.d("NEW LOCATION : ${location.latitude}, ${location.longitude}")
                    }
                }
            }

        }
    }

    // add coordinates to the last polyline
    private fun addPoints(location: Location) {

        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }

    }

    // disconnect run when pause button is clicked and continue when resumed again later
    private fun addEmptyPolyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // start empty polyline

    private fun startForegroundService() {
        startTimer()

        isTracking.postValue(true)

        // system service used when working with notifications
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        //inject notification builder
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSec.observe(this, Observer {
            if (!serviceDestroyed) {
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })

        //create actual notification
//        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//            .setAutoCancel(false)
//            .setOngoing(true)
//            .setSmallIcon(R.drawable.ic_run)
//            .setContentTitle("Running App...")
//            .setContentText("00:00:00") // update later
//            .setContentIntent(getMainActivityPendingIntent())

    }

    //pending intent function
//    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
//        this,
//        0,
//        Intent(this,MainActivity::class.java).also {
//            it.action = ACTION_SHOW_TRACKING_FRAGMENT
//        },
//        FLAG_UPDATE_CURRENT //UPDATES PREVIOUSLY RUNNING PENDING INTENT
//    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

    }

}