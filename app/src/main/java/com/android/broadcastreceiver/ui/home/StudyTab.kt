package com.android.broadcastreceiver.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.android.broadcastreceiver.alarm.ExactAlarms
import com.android.broadcastreceiver.alarm.PreviewExactAlarms
import com.android.broadcastreceiver.alarm.convertToAlarmTimeMillis
import com.android.broadcastreceiver.isValidHour
import com.android.broadcastreceiver.isValidMinute
import com.android.broadcastreceiver.toHour24Format
import com.android.broadcastreceiver.toUserFriendlyText
import com.android.broadcastreceiver.ui.composables.AlarmInput
import com.android.broadcastreceiver.ui.composables.AlarmSetClearButtons

@SuppressLint("InlinedApi")
@Composable
fun StudyTab(
    exactAlarms: ExactAlarms,
    onSchedulingExactAlarmsNotAllowed: () -> Unit
) {

    val alarm by remember { exactAlarms.getExactAlarmState() }
    val is24HourFormat = TimeFormat.is24HourFormat
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(
                vertical = 32.dp,
                horizontal = 8.dp
            )
        ) {
            Text(
                text = "Set Study Alarm",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                fontSize = 18.sp
            )

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                var hourInput by remember { mutableStateOf("") }
                var minuteInput by remember { mutableStateOf("") }
                var showInputInvalidMessage by remember { mutableStateOf(false) }
                var isAm by remember { mutableStateOf(true) }
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
                    shouldShowClearButton = alarm.isSet(),
                    onSetClicked = {
                        if (hourInput.isValidHour(is24HourFormat) && minuteInput.isValidMinute()) {
                            showInputInvalidMessage = false

                            val hour: Int = if (is24HourFormat) {
                                hourInput.toInt()
                            } else {
                                hourInput.toInt().toHour24Format(isAm)
                            }
                            scheduleAlarm(
                                exactAlarms,
                                hour,
                                minuteInput.toInt(),
                                onSchedulingExactAlarmsNotAllowed
                            )
                            focusManager.clearFocus()
                        } else {
                            showInputInvalidMessage = true
                        }
                    },
                    onClearClicked = { exactAlarms.clearExactAlarm() }
                )
            }
        }


        if (alarm.isSet()) {
            Text(
                text = "Alarm set: ${toUserFriendlyText(alarm.triggerAtMillis, is24HourFormat)}",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun scheduleAlarm(
    exactAlarms: ExactAlarms,
    hour: Int,
    minute: Int,
    onSchedulingAlarmNotAllowed: () -> Unit
) {
    if (exactAlarms.canScheduleExactAlarms().not()) {
        onSchedulingAlarmNotAllowed.invoke()
        return
    }

    val alarmTimeMillis: Long = convertToAlarmTimeMillis(hour, minute)
    exactAlarms.scheduleExactAlarm(ExactAlarm(alarmTimeMillis))
}

@Preview(showBackground = true)
@Composable
fun StudyTabPreview() {
    StudyTab(PreviewExactAlarms) {}
}