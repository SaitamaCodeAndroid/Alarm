/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.android.broadcastreceiver.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

const val INEXACT_ALARM_REQUEST_CODE = 1002
const val INEXACT_ALARM_WINDOW_REQUEST_CODE = 1003
const val INEXACT_REPEATING_ALARM_REQUEST_CODE = 1004

const val ALARM_REQUEST_CODE_EXTRA = "alarm_request_code_extra"

class InexactAlarmsImpl(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) : InexactAlarms {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private var inexactAlarmState = mutableStateOf(ExactAlarm.NOT_SET)
    private var windowAlarmState = mutableStateOf(WindowAlarm.NOT_SET)
    private var repeatingAlarmState = mutableStateOf(RepeatingAlarm.NOT_SET)

    override fun getInexactAlarmState(): State<ExactAlarm> = inexactAlarmState

    override fun getWindowAlarmState(): State<WindowAlarm> = windowAlarmState

    override fun getRepeatingAlarmState(): State<RepeatingAlarm> = repeatingAlarmState

    override fun rescheduleAlarms() {
        val inexactAlarm = sharedPreferences.getInexactAlarm()
        if (inexactAlarm.isSet() && inexactAlarm.isNotInPast()) {
            scheduleInexactAlarm(inexactAlarm)
        } else {
            clearInexactAlarm()
        }

        val windowAlarm = sharedPreferences.getWindowAlarm()
        if (windowAlarm.isSet() && windowAlarm.isNotInPast()) {
            scheduleWindowAlarm(windowAlarm)
        } else {
            clearWindowAlarm()
        }

        val repeatingAlarm = sharedPreferences.getRepeatingAlarm()
        if (repeatingAlarm.isSet()) {
            scheduleRepeatingAlarm(repeatingAlarm)
        } else {
            clearRepeatingAlarm()
        }
    }

    /**
     * Schedule an inexact alarm using setExact().
     */
    override fun scheduleInexactAlarm(inexactAlarm: ExactAlarm) {
        val pendingIntent = createInexactAlarmIntent(INEXACT_ALARM_REQUEST_CODE)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            inexactAlarm.triggerAtMillis,
            pendingIntent
        )
        sharedPreferences.putInexactAlarm(inexactAlarm)
        inexactAlarmState.value = inexactAlarm
    }

    /**
     * Cancel an inexact alarm, clear preferences and update state.
     */
    override fun clearInexactAlarm() {
        val pendingIntent = createInexactAlarmIntent(INEXACT_ALARM_REQUEST_CODE)
        alarmManager.cancel(pendingIntent)
        sharedPreferences.clearInexactAlarm()
        inexactAlarmState.value = ExactAlarm.NOT_SET

    }

    /**
     * Schedule an inexact alarm using setWindow().
     */
    override fun scheduleWindowAlarm(windowAlarm: WindowAlarm) {
        val pendingIntent = createInexactAlarmIntent(INEXACT_ALARM_WINDOW_REQUEST_CODE)
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            windowAlarm.triggerAtMillis,
            windowAlarm.windowLengthMillis,
            pendingIntent
        )
        sharedPreferences.putWindowAlarm(windowAlarm)
        windowAlarmState.value = windowAlarm
    }

    /**
     * Cancel a window alarm, clear preferences and update state.
     */
    override fun clearWindowAlarm() {
        val pendingIntent = createInexactAlarmIntent(INEXACT_ALARM_WINDOW_REQUEST_CODE)
        alarmManager.cancel(pendingIntent)
        sharedPreferences.clearWindowAlarm()
        windowAlarmState.value = WindowAlarm.NOT_SET
    }

    /**
     * Schedule a repeating alarm using setInexactRepeating().
     */
    override fun scheduleRepeatingAlarm(repeatingAlarm: RepeatingAlarm) {
        val pendingIntent = createInexactAlarmIntent(INEXACT_REPEATING_ALARM_REQUEST_CODE)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            repeatingAlarm.triggerAtMillis,
            repeatingAlarm.intervalMillis,
            pendingIntent
        )
        sharedPreferences.putRepeatingAlarm(repeatingAlarm)
        repeatingAlarmState.value = repeatingAlarm
    }

    /**
     * Cancel a repeating alarm, clear preferences and update state.
     */
    override fun clearRepeatingAlarm() {
        val pendingIntent = createInexactAlarmIntent(INEXACT_REPEATING_ALARM_REQUEST_CODE)
        alarmManager.cancel(pendingIntent)
        sharedPreferences.clearRepeatingAlarm()
        repeatingAlarmState.value = RepeatingAlarm.NOT_SET
    }

    /**
     * inexactAlarms.clearInexactAlarm()
     */
    private fun createInexactAlarmIntent(alarmRequestCode: Int): PendingIntent {
        val pendingIntent = Intent(
            context,
            InexactAlarmBroadcastReceiver::class.java
        ).apply {
            putExtra(ALARM_REQUEST_CODE_EXTRA, alarmRequestCode)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmRequestCode,
            pendingIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
}
