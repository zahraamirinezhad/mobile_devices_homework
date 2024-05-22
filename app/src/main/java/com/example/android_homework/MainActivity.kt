package com.example.android_homework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.android_homework.ui.theme.Android_homeworkTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Android_homeworkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Android_homeworkTheme {
        Greeting("Android")
    }
}