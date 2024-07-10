package com.android.broadcastreceiver.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.broadcastreceiver.BroadcastApplication

class RescheduleAlarmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (it.action == Intent.ACTION_BOOT_COMPLETED) {
                (context?.applicationContext as BroadcastApplication).apply {
                    exactAlarms.rescheduleAlarm()
                    inexactAlarms.rescheduleAlarms()
                }
            }
        }
    }
}
