package com.blogspot.svdevs.runtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_NAME
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @set:Inject
    var isFirstAppLaunch = true

    private val etName: EditText?
        get() = view?.findViewById(R.id.etName)

    private val etWeight: EditText?
        get() = view?.findViewById(R.id.etWeight)

    private val tvToolbarText: TextView?
        get() = requireActivity().findViewById(R.id.tvToolbarTitle)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvContinue = view.findViewById<TextView>(R.id.tvContinue)

        if(!isFirstAppLaunch) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )

        }

        tvContinue.setOnClickListener {
            val success = writeDataToSharedPreferences()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else {
                Snackbar.make(requireView(),"Please fill all fields", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    private fun writeDataToSharedPreferences(): Boolean {
        val name = etName?.text.toString().trim()
        val weight = etWeight?.text.toString().trim()

        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        //save name and weight to shared preferences
        sharedPrefs.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()// apply() asynchronous, commit() synchronous
        val toolbarText = "Let's go $name!"
        tvToolbarText?.text = toolbarText // *******
        return true
    }

}