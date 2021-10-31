package com.blogspot.svdevs.runtracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val toolbar: Toolbar
        get() = findViewById(R.id.toolbar)

    private val bottomNavView: BottomNavigationView
        get() = findViewById(R.id.bottomNavigationView)

    private val navHostFragment: View
        get() = findViewById(R.id.nav_host_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        bottomNavView.setupWithNavController(navHostFragment.findNavController())
        bottomNavView.setOnItemReselectedListener { /* NO-OPERATION */ }

        navigateToTrackingFragment(intent)

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _,destination,_ ->

                when(destination.id){
                    R.id.settingsFragment,R.id.statisticsFragment,R.id.runFragment -> {
                        bottomNavView.visibility = View.VISIBLE
                    } else -> bottomNavView.visibility = View.GONE
                }

            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }

    private fun navigateToTrackingFragment (intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }
}