package tech.runchen.mce.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeviceBroadcastReceiver : BroadcastReceiver() {

     val ACTION = "android.intent.action.BOOT_COMPLETED"

    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == ACTION_BOOT) {

//        }
    }

}