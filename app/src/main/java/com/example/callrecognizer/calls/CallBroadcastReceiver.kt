package com.example.callrecognizer.calls

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.widget.Toast
import com.example.callrecognizer.contacts.getData


class CallBroadcastReceiver : BroadcastReceiver() {
    private fun handleIncomingCall(context: Context, phoneCall: PhoneCall.Incoming) {
        Toast.makeText(context, "Incoming: ${phoneCall.number}", Toast.LENGTH_SHORT).show()
        val ctts = getData(context)
        val mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI)
        mp.setOnCompletionListener {
            it.reset()
            it.release()
        }

        val telephonyManager: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(
                context.mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        when (state) {
                            TelephonyManager.CALL_STATE_OFFHOOK -> {
                                if(mp.isPlaying) {
                                    mp.pause()
                                }
                            }
                            TelephonyManager.CALL_STATE_IDLE -> {
                                if(mp.isPlaying) {
                                    mp.pause()
                                }
                            }
                        }
                    }
                })
        } else {
            telephonyManager.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    when (state) {
                        TelephonyManager.CALL_STATE_OFFHOOK -> {
                            if(mp.isPlaying) {
                                mp.pause()
                            }
                        }
                        TelephonyManager.CALL_STATE_IDLE -> {
                            if(mp.isPlaying) {
                                mp.pause()
                            }
                        }
                        TelephonyManager.CALL_STATE_RINGING -> {
                            if(!mp.isPlaying) {
                                mp.start()
                            }
                        }
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }

        // Declare an audio manager
        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percent = 0.7f
        val seventyVolume = (maxVolume * percent).toInt()

        val re = Regex("[^A-Za-z0-9 ]")
        var number: String

        val coming = phoneCall.number!!
            .replace(re, "")
            .replaceBefore('9', "")
            .replace("\\s".toRegex(), "")
        ctts.forEach{
            number = it.number
                .replace(re, "")
                .replaceBefore('9', "")
                .replace("\\s".toRegex(), "")
            Toast.makeText(context, "${it.number} $number $coming", Toast.LENGTH_SHORT).show()
            if (number == coming) {
                Toast.makeText(context, "${it.name} is calling", Toast.LENGTH_SHORT).show()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0)
                mp.start()
            }
        }
    }

    private fun handleOutgoingCall(context: Context, phoneCall: PhoneCall.Outgoing) {
        Toast.makeText(context, "Outgoing: ${phoneCall.number}", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (!context.checkPermissions(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.READ_PHONE_STATE
                )
        ) {
            return
        }
        when (val phoneCall = intent.phoneCallInformation()) {
            is PhoneCall.Incoming -> handleIncomingCall(context, phoneCall)
            is PhoneCall.Outgoing -> handleOutgoingCall(context, phoneCall)
            else -> {}
        }
    }
}