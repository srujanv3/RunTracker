package com.blogspot.svdevs.runtracker.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.adapters.RunAdapter
import com.blogspot.svdevs.runtracker.utils.Constants.REQUEST_CODE_LOCATION
import com.blogspot.svdevs.runtracker.utils.SortType
import com.blogspot.svdevs.runtracker.utils.TrackingUtility
import com.blogspot.svdevs.runtracker.ui.viewmodel.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment: Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    private val rvRuns: RecyclerView?
        get() = view?.findViewById(R.id.rvRuns)

    private val spFilter: Spinner?
        get() = view?.findViewById(R.id.spFilter)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)

        reqPermissions()
        setupRecyclerView()

//        when(viewModel.sortType) {
//            SortType.DATE -> spFilter?.setSelection(0)
//            SortType.RUNNING_TIME -> spFilter?.setSelection(1)
//            SortType.DISTANCE -> spFilter?.setSelection(2)
//            SortType.AVG_SPEED -> spFilter?.setSelection(3)
//            SortType.CALORIES_BURNT -> spFilter?.setSelection(4)
//        }

        spFilter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNT)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun setupRecyclerView() = rvRuns?.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun reqPermissions() {
        if(TrackingUtility.hasLocationPermission(requireContext())){ // permissions accepted by the user
            return
        }
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,
                "Please provide location permissions",
                REQUEST_CODE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else {
            EasyPermissions.requestPermissions(
                this,
                "Please provide location permissions",
                REQUEST_CODE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }else {
            reqPermissions()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}