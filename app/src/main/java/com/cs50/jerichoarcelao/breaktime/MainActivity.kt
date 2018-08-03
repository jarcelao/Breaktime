package com.cs50.jerichoarcelao.breaktime

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ToggleButton

import kotlinx.android.synthetic.main.activity_main.*
import com.cs50.jerichoarcelao.breaktime.helpers.DialogReminderService
import com.cs50.jerichoarcelao.breaktime.helpers.NotificationIntentService

class MainActivity : AppCompatActivity() {

    private var mainMenu: Menu? = null

    // Vars that will contain the values from the ConfigureActivity SharedPreferences
    private var reminderEnabled: Boolean? = null
    private var dialogEnabled: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // This will keep track of notifications that the app will send
        val notificationManager = NotificationManagerCompat.from(this)

        // This will keep track of what state the main switch was left in
        val sharedPrefs =
                getSharedPreferences(getString(R.string.main_activity_prefs),
                        Context.MODE_PRIVATE)

        // Load saved preferences
        loadPreferences()

        // Declare functionality for the main switch
        val mainSwitch: ToggleButton = findViewById(R.id.mainSwitch)
        mainSwitch.isChecked = sharedPrefs.getBoolean("switch_last_state", false)

        // Checks what state the switch is in
        mainSwitch.setOnCheckedChangeListener { _, switchActive ->
            if (switchActive) {

                // Remove access to the settings menu
                mainMenu?.findItem(R.id.action_settings)?.isEnabled = false

                // Create a pending intent (notification tap action)
                val persistentIntent = Intent(this, MainActivity::class.java)
                val persistentPendingIntent =
                        PendingIntent.getActivity(this, 0
                        , persistentIntent, 0)

                // Build the persistent notification contents
                val persistentNotification =
                        NotificationCompat.Builder(this,
                                getString(R.string.persistent_notification_channel_id))
                                .setSmallIcon(R.drawable.alarm_icon)
                                .setContentTitle(
                                        getString(R.string.persistent_notification_title))
                                .setContentText(getString(R.string.persistent_notification_text))
                                .setPriority(NotificationCompat.PRIORITY_MIN)
                                .setContentIntent(persistentPendingIntent)
                                .setOngoing(true)

                // Remember last switch state
                with(sharedPrefs.edit()) {
                    putBoolean("switch_last_state", true)
                    apply()
                }

                // Notify the user
                notificationManager.notify(0, persistentNotification.build())
                Snackbar.make(findViewById(R.id.coordinator_layout),
                        R.string.breaktime_active_toast, Snackbar.LENGTH_LONG).show()

                // Start the countdowns towards next break
                if(reminderEnabled!!) {
                    startService(Intent(this, NotificationIntentService::class.java))
                }

                if(dialogEnabled!!) {
                    startService(Intent(this, DialogReminderService::class.java))
                }

            } else {

                // Restore access to the settings menu
                mainMenu?.findItem(R.id.action_settings)?.isEnabled = true

                // Stop the services
                stopService(Intent(this, NotificationIntentService::class.java))
                stopService(Intent(this, DialogReminderService::class.java))

                // Remove the persistent notification
                notificationManager.cancel(0)

                // Remember switch state
                with(sharedPrefs.edit()) {
                    putBoolean("switch_last_state", false)
                    apply()
                }

                // Notify the user
                Snackbar.make(findViewById(R.id.coordinator_layout),
                        R.string.breaktime_inactive_toast, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Reload preferences (applying changed settings)
        loadPreferences()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Get a reference to the menu
        mainMenu = menu

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            // Opens the settings page
            R.id.action_settings -> {
                startActivity(Intent(this, ConfigureActivity::class.java))
                true
            }

            // Opens the about page
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendInvalidDialog(message: Int) {

        // Build invalid dialog contents
        val invalidDialog = AlertDialog.Builder(this)
                .setTitle(R.string.invalid_alert_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok_button, { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    startActivity(Intent(this, ConfigureActivity::class.java))
                })
                .create()

        // Show the dialog
        invalidDialog.show()
    }

    /** Loads configuration settings from the ConfigureActivity shared preferences file */
    private fun loadPreferences() {

        // Get a reference to the preferences file
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Convert to ms (for timer) and reset value if invalid
        val timeUntilNotifyInMs =
                prefs.getString("time_until_notification", "5").toLong() * 60000

        // Make sure that value is valid
        if (timeUntilNotifyInMs <= 0) {
            sendInvalidDialog(R.string.invalid_notification_content)
        }

        val timeUntilDialogInMs = prefs.getString("time_until_dialog", "20").toLong() * 60000
        if (timeUntilDialogInMs <= 0) {
            sendInvalidDialog(R.string.invalid_dialog_content)
        }

        reminderEnabled = prefs.getBoolean("reminder_notification_enabled", true)
        dialogEnabled = prefs.getBoolean("dialog_reminder_enabled", false)
    }
}