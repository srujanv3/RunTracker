package com.blogspot.svdevs.runtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.blogspot.svdevs.runtracker.R
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_NAME
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private val etName: EditText?
        get() = requireActivity().findViewById(R.id.etName)

    private val etWeight: EditText?
        get() = requireActivity().findViewById(R.id.etWeight)

    private val toolbarTitle: TextView?
        get() = requireActivity().findViewById(R.id.tvToolbarTitle)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnApply = view.findViewById<Button>(R.id.btnApplyChanges)
        loadFields()
        btnApply.setOnClickListener {
            val success = applyChangesToSharedPrefs()
            if(success) {
                Snackbar.make(view,"Changes Saved!",Snackbar.LENGTH_LONG).show()
            }else {
                Snackbar.make(view,"Please fill all fields",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFields() {
        val name = sharedPrefs.getString(KEY_NAME,"")
        val weight = sharedPrefs.getFloat(KEY_WEIGHT,80f)
        etName?.setText(name)
        etWeight?.setText(weight.toString())
    }

    private fun applyChangesToSharedPrefs(): Boolean {

        val name = etName?.text.toString()
        val weight = etWeight?.text.toString()

        if(name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPrefs.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .apply()
        val toolbarText = "Let's go $name"
        toolbarTitle?.text = toolbarText
        return true

    }
}