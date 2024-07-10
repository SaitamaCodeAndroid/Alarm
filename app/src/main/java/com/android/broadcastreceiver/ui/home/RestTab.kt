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

@file:Suppress("FunctionName")

package com.android.broadcastreceiver.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.broadcastreceiver.TimeFormat
import com.android.broadcastreceiver.alarm.ExactAlarm
import com.android.broadcastreceiver.alarm.InexactAlarms
import com.android.broadcastreceiver.alarm.PreviewInexactAlarms
import com.android.broadcastreceiver.alarm.RepeatingAlarm
import com.android.broadcastreceiver.alarm.WindowAlarm
import com.android.broadcastreceiver.alarm.convertToAlarmTimeMillis
import com.android.broadcastreceiver.isNotZero
import com.android.broadcastreceiver.isValidHour
import com.android.broadcastreceiver.isValidMinute
import com.android.broadcastreceiver.isValidWindowLength
import com.android.broadcastreceiver.toHour24Format
import com.android.broadcastreceiver.toMillis
import com.android.broadcastreceiver.toUserFriendlyText
import com.android.broadcastreceiver.ui.composables.AlarmInput
import com.android.broadcastreceiver.ui.composables.AlarmSetClearButtons
import com.android.broadcastreceiver.ui.composables.AlarmWithIntervalInput

@Composable
fun RestTab(inexactAlarms: InexactAlarms) {

    val inexactAlarm by remember { inexactAlarms.getInexactAlarmState() }
    val windowAlarm by remember { inexactAlarms.getWindowAlarmState() }
    val repeatingAlarm by remember { inexactAlarms.getRepeatingAlarmState() }
    val is24HourFormat = TimeFormat.is24HourFormat


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            InexactAlarmInput(inexactAlarms, inexactAlarm)
            Spacer(modifier = Modifier.height(16.dp))
            WindowAlarmInput(inexactAlarms, windowAlarm)
            Spacer(modifier = Modifier.height(16.dp))
            RepeatingAlarmInput(inexactAlarms, repeatingAlarm)

            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxWidth()
            ) {
                if (inexactAlarm.isSet()) {
                    Text(
                        text = "Inexact alarm set: ${
                            toUserFriendlyText(inexactAlarm.triggerAtMillis, is24HourFormat)
                        }",
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                    )
                }

                if (windowAlarm.isSet()) {
                    Text(
                        text = "Window alarm set: ${
                            toUserFriendlyText(
                                windowAlarm.triggerAtMillis,
                                windowAlarm.windowLengthMillis,
                                is24HourFormat
                            )
                        }",
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                    )
                }

                if (repeatingAlarm.isSet()) {
                    Text(
                        text = "Repeating alarm set: ${
                            toUserFriendlyText(
                                repeatingAlarm.triggerAtMillis,
                                repeatingAlarm.intervalMillis,
                                is24HourFormat
                            )
                        }",
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun InexactAlarmInput(
    inexactAlarms: InexactAlarms,
    inexactAlarm: ExactAlarm
) {
    Text(
        text = "Set Rest Alarm",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        fontSize = 18.sp
    )

    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var showInputInvalidMessage by remember { mutableStateOf(false) }
    var isAm by remember { mutableStateOf(true) }
    val is24HourFormat = TimeFormat.is24HourFormat

    Row(modifier = Modifier.padding(top = 16.dp)) {
        AlarmInput(
            hourInput = hourInput,
            minuteInput = minuteInput,
            onHourInputChanged = { hourInput = it },
            onMinuteInputChanged = { minuteInput = it },
            showInputInvalidMessage = showInputInvalidMessage,
            is24HourFormat = is24HourFormat,
            isAm = isAm,
            onIsAmEvent = { isAmValue -> isAm = isAmValue }
        )

        Spacer(Modifier.weight(1F, true))

        val focusManager = LocalFocusManager.current
        AlarmSetClearButtons(
            shouldShowClearButton = inexactAlarm.isSet(),
            onSetClicked = {
                if (hourInput.isValidHour(is24HourFormat) && minuteInput.isValidMinute()) {
                    showInputInvalidMessage = false

                    val hour: Int = if (is24HourFormat) {
                        hourInput.toInt()
                    } else {
                        hourInput.toInt().toHour24Format(isAm)
                    }
                    scheduleAlarm(inexactAlarms, hour, minuteInput.toInt())
                    focusManager.clearFocus()
                } else {
                    showInputInvalidMessage = true
                }
            },
            onClearClicked = { inexactAlarms.clearInexactAlarm() }
        )
    }
}

@Composable
private fun WindowAlarmInput(
    inexactAlarms: InexactAlarms,
    windowAlarm: WindowAlarm
) {
    Text(
        text = "Set Rest Window (at least 10min)",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        fontSize = 18.sp
    )

    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var windowInput by remember { mutableStateOf("") }
    var showInputInvalidMessage by remember { mutableStateOf(false) }
    var isAm by remember { mutableStateOf(true) }
    val is24HourFormat = TimeFormat.is24HourFormat

    Row(modifier = Modifier.padding(top = 16.dp)) {
        AlarmWithIntervalInput(
            hourInput = hourInput,
            minuteInput = minuteInput,
            intervalInput = windowInput,
            onHourInputChanged = { hourInput = it },
            onMinuteInputChanged = { minuteInput = it },
            onIntervalInputChanged = { windowInput = it },
            showInputInvalidMessage = showInputInvalidMessage,
            is24HourFormat = is24HourFormat,
            isAm = isAm,
            onIsAmEvent = { isAmValue -> isAm = isAmValue }
        )

        Spacer(Modifier.weight(1F, true))

        val focusManager = LocalFocusManager.current
        AlarmSetClearButtons(
            shouldShowClearButton = windowAlarm.isSet(),
            onSetClicked = {
                if (hourInput.isValidHour(is24HourFormat)
                    && minuteInput.isValidMinute()
                    && windowInput.isValidWindowLength()
                ) {
                    showInputInvalidMessage = false

                    val hour: Int = if (is24HourFormat) {
                        hourInput.toInt()
                    } else {
                        hourInput.toInt().toHour24Format(isAm)
                    }
                    scheduleWindowAlarm(
                        inexactAlarms,
                        hour,
                        minuteInput.toInt(),
                        windowInput.toInt()
                    )
                    focusManager.clearFocus()
                } else {
                    showInputInvalidMessage = true
                }
            },
            onClearClicked = { inexactAlarms.clearWindowAlarm() }
        )
    }
}

@Composable
private fun RepeatingAlarmInput(
    inexactAlarms: InexactAlarms,
    repeatingAlarm: RepeatingAlarm
) {
    Text(
        text = "Set Repeating Rest Alarm",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        fontSize = 18.sp
    )

    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var intervalInput by remember { mutableStateOf("") }
    var showInputInvalidMessage by remember { mutableStateOf(false) }
    var isAm by remember { mutableStateOf(true) }
    val is24HourFormat = TimeFormat.is24HourFormat

    Row(modifier = Modifier.padding(top = 16.dp)) {
        AlarmWithIntervalInput(
            hourInput = hourInput,
            minuteInput = minuteInput,
            intervalInput = intervalInput,
            onHourInputChanged = { hourInput = it },
            onMinuteInputChanged = { minuteInput = it },
            onIntervalInputChanged = { intervalInput = it },
            showInputInvalidMessage = showInputInvalidMessage,
            is24HourFormat = is24HourFormat,
            isAm = isAm,
            onIsAmEvent = { isAmValue -> isAm = isAmValue }
        )

        Spacer(Modifier.weight(1F, true))

        val focusManager = LocalFocusManager.current
        AlarmSetClearButtons(
            shouldShowClearButton = repeatingAlarm.isSet(),
            onSetClicked = {
                if (hourInput.isValidHour(is24HourFormat)
                    && minuteInput.isValidMinute()
                    && intervalInput.isNotZero()
                ) {
                    showInputInvalidMessage = false

                    val hour: Int = if (is24HourFormat) {
                        hourInput.toInt()
                    } else {
                        hourInput.toInt().toHour24Format(isAm)
                    }
                    scheduleRepeatingAlarm(
                        inexactAlarms,
                        hour,
                        minuteInput.toInt(),
                        intervalInput.toInt()
                    )
                    focusManager.clearFocus()
                } else {
                    showInputInvalidMessage = true
                }
            },
            onClearClicked = { inexactAlarms.clearRepeatingAlarm() }
        )
    }
}

private fun scheduleAlarm(inexactAlarms: InexactAlarms, hour: Int, minute: Int) {
    val alarmTimeMillis: Long = convertToAlarmTimeMillis(hour, minute)
    inexactAlarms.scheduleInexactAlarm(ExactAlarm(alarmTimeMillis))
}

private fun scheduleWindowAlarm(
    inexactAlarms: InexactAlarms,
    hour: Int,
    minute: Int,
    minuteWindowLength: Int
) {
    val alarmTimeMillis: Long = convertToAlarmTimeMillis(hour, minute)
    val windowLengthMillis: Long = minuteWindowLength.toMillis()
    inexactAlarms.scheduleWindowAlarm(WindowAlarm(alarmTimeMillis, windowLengthMillis))
}

private fun scheduleRepeatingAlarm(
    inexactAlarms: InexactAlarms,
    hour: Int,
    minute: Int,
    minuteInterval: Int
) {
    val alarmTimeMillis: Long = convertToAlarmTimeMillis(hour, minute)
    val intervalMillis: Long = minuteInterval.toMillis()
    inexactAlarms.scheduleRepeatingAlarm(RepeatingAlarm(alarmTimeMillis, intervalMillis))
}

@Preview(showBackground = true)
@Composable
fun RestTabPreview() {
    RestTab(PreviewInexactAlarms)
}
