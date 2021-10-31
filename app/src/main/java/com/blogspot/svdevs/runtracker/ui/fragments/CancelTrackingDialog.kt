package com.blogspot.svdevs.runtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.blogspot.svdevs.runtracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel run ?")
            .setMessage("Are you sure to cancel current run ?")
            .setIcon(R.drawable.ic_cancel)
            .setPositiveButton("Yes") { _,_ ->
                yesListener?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton("No") { di, _ ->
                di.cancel()
            }
            .create()
        return dialog
    }

    //send listener for positive button of diaog to the tracking fragment
    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }

}