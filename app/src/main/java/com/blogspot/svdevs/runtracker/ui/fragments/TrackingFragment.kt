package com.blogspot.svdevs.runtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.db.Run
import com.blogspot.svdevs.runtracker.services.PolyLine
import com.blogspot.svdevs.runtracker.services.TrackingService
import com.blogspot.svdevs.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.blogspot.svdevs.runtracker.utils.Constants.ACTION_START_OR_RESUME
import com.blogspot.svdevs.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.blogspot.svdevs.runtracker.utils.Constants.MAP_ZOOM
import com.blogspot.svdevs.runtracker.utils.Constants.POLYLINE_COLOR
import com.blogspot.svdevs.runtracker.utils.Constants.POLYLINE_WIDTH
import com.blogspot.svdevs.runtracker.utils.TrackingUtility
import com.blogspot.svdevs.runtracker.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG = "CANCEL_TRACKING_DIALOG"
@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var currTimeInMillis = 0L

    private val btnRun: Button?
        get() = view?.findViewById(R.id.btnToggleRun)

    private val btnFinish: Button?
        get() = view?.findViewById(R.id.btnFinishRun)

    private val tvTimer: TextView?
        get() = view?.findViewById(R.id.tvTimer)

    private val mapView: MapView?
        get() = view?.findViewById(R.id.mapView)

    private var menu: Menu? = null

    private var isTracking = false
    private var pathpoints = mutableListOf<PolyLine>()

    private var map: GoogleMap? = null

    @set:Inject
    var weight = 80f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // need this function while setting up options menu in fragment
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnRun?.setOnClickListener {
            //sendCommandToService(ACTION_START_OR_RESUME)
            toggleRun()
        }

        // handles stop run functionality  when the device is rotated
        if(savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG
            )as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        btnFinish?.setOnClickListener {
            zoomToShowCompleteTrack()
            endRunAndSaveToDB()
        }

        mapView?.onCreate(savedInstanceState)

        mapView?.getMapAsync { // this block is called when a new instance of TrackingFragment is created
            map = it
            addAllPolylines()
        }
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathpoints = it
            addPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currTimeInMillis,true)
            tvTimer?.text = formattedTime
        })
    }

    //Handle run button functionality
    private fun toggleRun() {
        if(isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else {
            sendCommandToService(ACTION_START_OR_RESUME)
        }
    }

    // Cancel a run functionality
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_track_menu, menu)
        this.menu = menu
    }

    //Cancel run options menu
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true // display cancel run icon
        }
    }

    //Cancel run options menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.cancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Cancel run options menu alert dialog
    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG)
    }

    //Stop run
    private fun stopRun() {
        tvTimer?.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    //Observe the service and update the changes accordingly
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(!isTracking && currTimeInMillis > 0L) {
            btnRun?.text = "Start"
            btnFinish?.visibility = View.VISIBLE
        }else if(isTracking) {
            btnRun?.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinish?.visibility = View.GONE
        }
    }


    //Move camera to the user position when he changes his position
    private fun moveCameraToUser() {
        if(pathpoints.isNotEmpty() && pathpoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathpoints.last().last(), //coordinate to which the camera should move
                    MAP_ZOOM //how much to zoom in
                )
            )
        }
    }

    //Get screenshot of the run track
    private fun zoomToShowCompleteTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathpoints) { // for each polyline in the polylines list
            for(pos in polyline) { // for each latlng coordinate
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView!!.width,
                mapView!!.height,
                (mapView!!.height * 0.05f).toInt()
            )
        )
    }

    //Finish the run and save data to db
    private fun endRunAndSaveToDB() {
        map?.snapshot { bmp ->
            //Calculate total distance of the run
            var distanceInMeters = 0
            for (polyline in pathpoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            // Calculate avg speed in kmph
            val avgSpeed = round((distanceInMeters/1000f) / (currTimeInMillis/1000f/60/60)*10)/10f
            //Calculate date time stamp
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            //Calculate calories burnt
            val calBurnt = ((distanceInMeters/1000f) * weight).toInt()

            val run = Run(bmp, dateTimeStamp,avgSpeed,distanceInMeters,currTimeInMillis,calBurnt)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    //Redraw the polyline when the device is rotated
    private fun addAllPolylines() {
        for (polyline in pathpoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    //Draw polyline track on the map
    private fun addPolyline() {
        if(pathpoints.isNotEmpty() && pathpoints.last().size>1) {
            val preLastLatLng = pathpoints.last()[pathpoints.last().size - 2]
            val lastLatLng = pathpoints.last().last()
            //define how the polyline should look like
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mapView?.onDestroy()
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}