package com.blogspot.svdevs.runtracker.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.blogspot.svdevs.runtracker.services.PolyLine
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermission(context:Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        // includeMillis is specified because we need to include millis at some places and exclude them at some places like the notifications
        var milliseconds = ms

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds) // this var is required to display in the string later on
        milliseconds -= TimeUnit.HOURS.toMillis(hours) // Reconvert hours to millis

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) // same as above
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes) // same as above

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        // subtract milliscounds each time to separate hours, mins and secs

        if(!includeMillis) {
            return "${if (hours<10) "0" else ""}$hours:" +
                    "${if (minutes<10) "0" else ""}$minutes:" +
                    "${if (seconds<10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10 //to get only two digits for the milliseconds

        return "${if (hours<10) "0" else ""}$hours:" +
                "${if (minutes<10) "0" else ""}$minutes:" +
                "${if (seconds<10) "0" else ""}$seconds:" +
                "${if (milliseconds<10) "0" else ""}$milliseconds"
    }

    fun calculatePolylineLength(polyline: PolyLine): Float {
        var distance = 0f

        for (i in 0..polyline.size - 2) { // subtract 2 because highest index would be i-1
            val posOne = polyline[i]
            val posTwo = polyline[i+1] // hence i+1-1 would give an error. So exclude last 2 points by subtracting 2

            val result = FloatArray(1)
            Location.distanceBetween(
                posOne.latitude,
                posOne.longitude,
                posTwo.latitude,
                posTwo.longitude,
                result
            )
            distance += result[0]
        }
        return distance

    }

}