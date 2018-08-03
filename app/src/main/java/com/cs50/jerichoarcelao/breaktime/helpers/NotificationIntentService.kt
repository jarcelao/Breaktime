package com.cs50.jerichoarcelao.breaktime.helpers

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.cs50.jerichoarcelao.breaktime.R
import java.util.*

/** Provides notification reminder functionality */
class NotificationIntentService : IntentService("NotificationIntentService") {

    // Vars that need to be accessed on different local scopes
    private var notificationManager: NotificationManagerCompat? = null
    private var notificationTimer: CountDownTimer? = null
    private var persistentPendingIntent: PendingIntent?= null
    private var sharedPrefs: SharedPreferences? = null

    // Notification reminder settings
    private var timeUntilNotifyInMs: Long? = null
    private var customReminderText: String? = null

    override fun onCreate() {
        super.onCreate()

        // Load notification reminder preferences
        loadPreferences()

        // Get a reference to shared preferences
        sharedPrefs = getSharedPreferences(getString(R.string.main_activity_prefs),
                Context.MODE_PRIVATE)

        // Declare notification manager
        notificationManager = NotificationManagerCompat.from(this)

        // Countdown timer until a reminder is sent
        notificationTimer = object: CountDownTimer(timeUntilNotifyInMs!!, 1000) {
            override fun onTick(p0: Long) {}

            override fun onFinish() {

                // Check if main switch is deactivated
                if (!sharedPrefs!!.getBoolean("switch_last_state", false)) {
                    this.cancel()
                } else {

                    // Send reminder notification and reset timer
                    sendReminderNotification()
                    this.start()
                }
            }
        }
    }

    override fun onHandleIntent(p0: Intent?) {

        // Start the notification timer
        notificationTimer?.start()
    }

    /** Sends a break time reminder notification */
    @SuppressLint("InlinedApi")
    private fun sendReminderNotification() {
        createNotificationChannel(getString(R.string.reminder_notification_channel_id),
                getString(R.string.reminder_notification_channel_name),
                getString(R.string.reminder_notification_channel_description),
                NotificationManager.IMPORTANCE_MAX)

        // Build the notification contents
        val reminderNotification: NotificationCompat.Builder = NotificationCompat.Builder(
                this, resources.getString(R.string.reminder_notification_channel_id))
                .setSmallIcon(R.drawable.alarm_icon)
                .setContentTitle(getString(R.string.reminder_notification_title))
                .setContentText(customReminderText)
                .setContentIntent(persistentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)

        // Notify the user
        notificationManager?.notify(0, reminderNotification.build())
    }

    /** Loads notification configuration settings from the
     * ConfigureActivity shared preferences file
     */
    private fun loadPreferences() {
        // Get a reference to the preferences file
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Convert to ms (for timer) and reset value if invalid
        timeUntilNotifyInMs = prefs.getString("time_until_notification", "5").toLong() * 60000

        customReminderText = prefs.getString("custom_reminder_text", selectRandomMessage())

        // Make sure that this variable is not empty
        if (customReminderText == "") {
            customReminderText = selectRandomMessage()
        }
    }

    /** Select a random message (to be used in reminders) */
    private fun selectRandomMessage(): String? {
        // Create an instance of an RNG
        val rand = Random()

        // Generate random number
        val num: Int = rand.nextInt(6)

        // Return appropriate message
        when (num) {
            0 -> return getString(R.string.short_message_1)
            1 -> return getString(R.string.short_message_2)
            2 -> return getString(R.string.short_message_3)
            3 -> return getString(R.string.short_message_4)
            4 -> return getString(R.string.short_message_5)
            5 -> return getString(R.string.short_message_6)
            6 -> return getString(R.string.short_message_7)
        }

        return null
    }

    /** Creates a notification channel (for Android 8 and above) */
    private fun createNotificationChannel(channelID: String, name: CharSequence,
                                          description: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = description

            val notificationManager: NotificationManager =
                    getSystemService(NotificationManager :: class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}