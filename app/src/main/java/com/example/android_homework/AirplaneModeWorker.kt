package com.example.android_homework

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AirplaneModeWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        // Check airplane mode status
        val isAirplaneModeOn = isAirplaneModeOn(applicationContext)

        // Log the status
        Log.i("AirplaneModeWorker", "Airplane mode is ${if (isAirplaneModeOn) "ON" else "OFF"}")
        val currentTimeMillis = System.currentTimeMillis()
        saveLogToFile(
            applicationContext,
            "worker_airplane",
            currentTimeMillis,
            "Airplane mode is ${if (isAirplaneModeOn) "ON" else "OFF"}"
        )

        return Result.success()
    }

    private fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }

    private fun saveLogToFile(context: Context, type: String, timestamp: Long, value: String) {
        val formattedTimestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val jsonObject = JSONObject().apply {
            put("type", type)
            put("time", formattedTimestamp)
            put("value", value)
        }
        val logEntry = jsonObject.toString() + "\n"

        try {
            val file = File(context.filesDir, "AirplaneBluetoothStatus.txt")
            FileWriter(file, true).use { writer ->
                writer.write(logEntry)
            }
        } catch (e: IOException) {
            Log.e("LogWorker", e.stackTraceToString())
        }
    }
}