package com.cs50.jerichoarcelao.breaktime

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.cs50.jerichoarcelao.breaktime.helpers.ReminderDialogFragment
import kotlinx.android.synthetic.main.activity_dialog_reminder.*
import java.util.Random

class DialogReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_reminder)

        constraint_layout.post({
            // Show dialog
            ReminderDialogFragment().show(supportFragmentManager, "")
        })
    }
}
