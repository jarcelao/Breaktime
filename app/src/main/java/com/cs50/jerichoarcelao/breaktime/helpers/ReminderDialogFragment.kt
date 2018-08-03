package com.cs50.jerichoarcelao.breaktime.helpers

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.support.v4.app.DialogFragment
import android.widget.TextView
import com.cs50.jerichoarcelao.breaktime.R
import java.util.*
import java.util.concurrent.TimeUnit

/** Helper class to generate a reminder dialog */
class ReminderDialogFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Load preferences for dialog reminder
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        var customDialogText = prefs.getString("custom_dialog_text", selectRandomMessage())

        if (customDialogText == "") {
            customDialogText = selectRandomMessage()
        }

        var dialogLengthInMs: Long = prefs.getString("dialog_break_length", "20").toLong() * 60000

        // Make sure that this variable is not invalid
        if (dialogLengthInMs <= 0) { dialogLengthInMs = 60000 }

        val builder = AlertDialog.Builder(activity)

        // Setup dialog contents
        val dialog = activity?.layoutInflater?.inflate(R.layout.dialog_reminder, null)

        dialog?.findViewById<TextView>(R.id.reminderText)?.text = customDialogText

        // Format timer
        // https://stackoverflow.com/a/17620827/9474943
        object: CountDownTimer(dialogLengthInMs, 1000) {
            override fun onTick(p0: Long) {
                dialog?.findViewById<TextView>(R.id.progressTimer)?.text = String.format(
                        "%d:%d", TimeUnit.MILLISECONDS.toMinutes(p0),
                        TimeUnit.MILLISECONDS.toSeconds(p0) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(p0)
                        ))
            }

            override fun onFinish() {
                activity?.finish()
            }
        }.start()

        builder.setView(dialog)
                .setPositiveButton(R.string.skip_button, { _, _ ->
                    run {
                        activity?.finish()
                    }
                })

        return builder.create()
    }

    private fun selectRandomMessage(): String? {
        // Create an instance of an RNG
        val rand = Random()

        // Generate random number
        val num: Int = rand.nextInt(1)

        // Return appropriate message
        when(num) {
            0 -> return getString(R.string.long_message_1)
            1 -> return getString(R.string.long_message_2)
        }

        return null
    }
}