package com.android.broadcastreceiver.alarm

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

fun playRingtone(context: Context): Ringtone {
    val alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL)
    val ringtone = RingtoneManager.getRingtone(context, alarmUri)
    ringtone.play()
    return ringtone
}