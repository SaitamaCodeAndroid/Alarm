package com.android.broadcastreceiver

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.text.format.DateFormat
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.android.broadcastreceiver.ui.home.HomeScreen
import com.android.broadcastreceiver.ui.theme.BroadcastReceiverTheme
import android.Manifest.permission.POST_NOTIFICATIONS

private const val TAG = "Notification permission"

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
            if (isGranted) {
                Log.v(TAG, "Permission is granted")
            } else {
                Log.v(TAG, "Permission is denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BroadcastReceiver)
        enableEdgeToEdge()

        askNotificationPermission()
        val exactAlarms = (application as BroadcastApplication).exactAlarms.apply {
            rescheduleAlarm()
        }
        val inexactAlarms = (application as BroadcastApplication).inexactAlarms.apply {
            rescheduleAlarms()
        }
        setContent {
            BroadcastReceiverTheme {
                val alarmRingtoneState = (application as BroadcastApplication).alarmRingtoneState
                HomeScreen(
                    exactAlarms = exactAlarms,
                    inexactAlarms = inexactAlarms,
                    onSchedulingAlarmNotAllowed = { openSettings() },
                    showStopAlarmButton = alarmRingtoneState.value != null,
                    onStopAlarmClicked = {
                        alarmRingtoneState.value?.stop()
                        alarmRingtoneState.value = null
                    })
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted")
            } else {
                Log.v(TAG, "Permission is denied")
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }
    }

    private fun openSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }

    }

    override fun onResume() {
        super.onResume()
        TimeFormat.is24HourFormat = DateFormat.is24HourFormat(this)
    }
}
