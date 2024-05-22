package com.example.android_homework

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.android_homework.ui.theme.Android_homeworkTheme
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 0)
        }

        setContent {
            Android_homeworkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val intentFilter: IntentFilter = IntentFilter()
                    intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED")
                    intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED")
                    val broadcastReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            if (intent.action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                                Toast.makeText(context, "POWER CONNECTED", Toast.LENGTH_SHORT)
                                    .show()
                                val serviceIntent = Intent(
                                    applicationContext,
                                    MyService.Actions.START::class.java
                                )
                                serviceIntent.setPackage(applicationContext.packageName)
                                applicationContext.startService(serviceIntent)
                                val currentTimeMillis = System.currentTimeMillis()
                                saveLogToFile(
                                    applicationContext,
                                    "worker_bluetooth",
                                    currentTimeMillis,
                                    "POWER CONNECTED"
                                )
                            } else if (intent.action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                                Toast.makeText(context, "POWER DISCONNECTED", Toast.LENGTH_SHORT)
                                    .show()
                                val serviceIntent = Intent(
                                    applicationContext,
                                    MyService.Actions.STOP::class.java
                                )
                                serviceIntent.setPackage(applicationContext.packageName)
                                applicationContext.startService(serviceIntent)
                                val currentTimeMillis = System.currentTimeMillis()
                                saveLogToFile(
                                    applicationContext,
                                    "power_connection",
                                    currentTimeMillis,
                                    "POWER DISCONNECTED"
                                )
                            }
                        }
                    }

                    registerReceiver(broadcastReceiver, intentFilter)

                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                        .build()

                    val periodicWorkRequest =
                        PeriodicWorkRequestBuilder<BluetoothStatusWorker>(
                            repeatInterval = 2,
                            repeatIntervalTimeUnit = TimeUnit.MINUTES
                        )
                            .setInitialDelay(5, TimeUnit.SECONDS)
                            .build()

                    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                        "loggerPeriodicWorkRequest1",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        periodicWorkRequest
                    )

                    val constraintsAir = Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                        .build()

                    val periodicWorkRequestAir =
                        PeriodicWorkRequestBuilder<AirplaneModeWorker>(2, TimeUnit.MINUTES)
                            .setConstraints(constraintsAir)
                            .build()

                    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                        "loggerPeriodicWorkRequest",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        periodicWorkRequestAir
                    )
                }
            }

            MainContent()
        }
    }

    private fun saveLogToFile(context: Context, type: String, timestamp: Long, value: String) {
        val formattedTimestamp =
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(timestamp))

        val jsonObject = JSONObject().apply {
            put("type", type)
            put("time", formattedTimestamp)
            put("value", value)
        }
        val logEntry = jsonObject.toString() + "\n"

        try {
            val file = File(context.filesDir, "data.txt")
            FileWriter(file, true).use { writer ->
                writer.write(logEntry)
            }
        } catch (e: IOException) {
            Log.e("LogWorker", e.stackTraceToString())
        }
    }
}


@Composable
fun MainContent() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        mergeFilesScreen(context = context)
        LogList(context = context)
    }
}
private fun mergeFilesScreen(context: Context) {
    val file1Content = readFileContent(context, "AirplaneBluetoothStatus.txt")
    val file2Content = readFileContent(context, "data.txt")
    val mergedContent = file1Content + "\n" + file2Content
    writeFileContent(context, "final.txt", mergedContent)
}

private fun readFileContent(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)
    return file.readText()
}

private fun writeFileContent(context: Context, fileName: String, content: String) {
    val file = File(context.filesDir, fileName)
    file.writeText(content)
}

@Composable
fun LogList(context: Context) {
    val logs = remember { readLogs(context) }
    val sortedLogs = remember { sortLogsByTime(logs) }

    LazyColumn {
        items(sortedLogs) { log ->
            Text(text = log)
        }
    }
}

private fun readLogs(context: Context): List<String> {
    val file = File(context.filesDir, "final.txt")
    return if (file.exists()) {
        file.readLines().reversed() // Read logs and reverse the list
    } else {
        emptyList()
    }
}


private fun sortLogsByTime(logs: List<String>): List<String> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    return logs.sortedByDescending {
        try {
            val timeString = it.substringBefore(" - ")
            dateFormat.parse(timeString)
        } catch (e: Exception) {
            null
        }
    }
}

