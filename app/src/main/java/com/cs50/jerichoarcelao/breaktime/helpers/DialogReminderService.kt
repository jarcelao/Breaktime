package com.cs50.jerichoarcelao.breaktime.helpers

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.preference.PreferenceManager
import com.ankushgrover.hourglass.Hourglass
import com.cs50.jerichoarcelao.breaktime.DialogReminderActivity
import com.cs50.jerichoarcelao.breaktime.R
import java.util.*

/** Helper class for dialog reminder functionality */
class DialogReminderService : IntentService("DialogReminderService") {

    private var dialogTimer: Hourglass? = null
    private var timeUntilDialogInMs: Long? = null
    private var timer: Timer? = null
    private var sharedPrefs: SharedPreferences? = null

    override fun onCreate() {
        super.onCreate()

        sharedPrefs =
                getSharedPreferences(
                        getString(R.string.main_activity_prefs), Context.MODE_PRIVATE)

        // Load dialog reminder preferences
        loadPreferences()

        // Countdown timer until a dialog is sent
        dialogTimer = object: Hourglass(timeUntilDialogInMs!!) {
            override fun onTimerTick(timeRemaining: Long) {
                if (isCallHelper()) { this.pauseTimer() }
            }

            override fun onTimerFinish() {
                // Check if main switch is activated
                if (sharedPrefs!!.getBoolean("switch_last_state", false)) {
                    // Send dialog reminder and reset timer
                    sendDialogReminder()
                    this.setTime(timeUntilDialogInMs!!)
                    this.startTimer()
                }
            }
        }
    }

    override fun onHandleIntent(p0: Intent?) {
        // Checks if the device is not in a call and resumes the dialog timer if paused
        timer = Timer()
        timer?.schedule(object: TimerTask() {
            override fun run() {
                if (!isCallHelper() && dialogTimer!!.isPaused) {
                    dialogTimer!!.resumeTimer()
                }
            }
        }, 500)

        // Starts the timer
        dialogTimer?.startTimer()
    }

    /** Loads dialog configuration settings from the ConfigureActivity shared preferences file */
    private fun loadPreferences() {

        // Get a reference to the preferences file
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        timeUntilDialogInMs = prefs.getString("time_until_dialog", "20").toLong() * 60000
    }

    /** Sends a break time intrusive reminder dialog */
    private fun sendDialogReminder() {
        startActivity(Intent(this, DialogReminderActivity::class.java))
    }

    /**
     * Checks if the phone is in a call
     * https://stackoverflow.com/a/17418732/9474943 */
    private fun isCallActive(context: Context): Boolean {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return manager.mode == AudioManager.MODE_IN_CALL
    }

    /** Helper function to feed the current activity context to isCallActive */
    private fun isCallHelper(): Boolean {
        return isCallActive(this)
    }
}