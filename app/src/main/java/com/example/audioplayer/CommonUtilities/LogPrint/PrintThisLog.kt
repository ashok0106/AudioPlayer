package com.example.audioplayer.CommonUtilities.LogPrint

import android.util.Log

fun PrintThisLog(message: String,tag : String = "TAG") {
    Log.d(tag, message)
}