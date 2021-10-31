package com.blogspot.svdevs.runtracker.utils

import android.graphics.Color

object Constants {

    const val RUNNING_DB_NAME = "running_db"
    const val REQUEST_CODE_LOCATION = 0

    const val ACTION_START_OR_RESUME = "ACTION_START_OR_RESUME"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1 //MINIMUM VALUE OF NOTIFICATION_ID SHOULD BE 1

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.CYAN
    const val POLYLINE_WIDTH = 8F

    const val MAP_ZOOM = 30F

    const val TIMER_UPDATE_INTERVAL = 50L

    const val SHARED_PREFERENCES = "sharedPrefs"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
}