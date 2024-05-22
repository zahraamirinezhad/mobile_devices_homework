package com.example.android_homework

import android.content.Context
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

class BluetoothStatusWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()

        val bluetoothMode = when (bluetoothAdapter?.state) {
            android.bluetooth.BluetoothAdapter.STATE_OFF -> "OFF"
            android.bluetooth.BluetoothAdapter.STATE_ON -> "ON"
            else -> "UNKNOWN"
        }

        Log.i("BluetoothStatusWorker", "Bluetooth mode: $bluetoothMode")
        val currentTimeMillis = System.currentTimeMillis()
        saveLogToFile(
            applicationContext,
            "worker_bluetooth",
            currentTimeMillis,
            "Bluetooth mode: $bluetoothMode"
        )

        return Result.success()
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