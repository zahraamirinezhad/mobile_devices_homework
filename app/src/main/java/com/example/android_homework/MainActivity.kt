package com.example.android_homework

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.android_homework.ui.theme.Android_homeworkTheme

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
                            } else if (intent.action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                                Toast.makeText(context, "POWER DISCONNECTED", Toast.LENGTH_SHORT)
                                    .show()
                                val serviceIntent = Intent(
                                    applicationContext,
                                    MyService.Actions.STOP::class.java
                                )
                                serviceIntent.setPackage(applicationContext.packageName)
                                applicationContext.startService(serviceIntent)
                            }
                        }
                    }

                    registerReceiver(broadcastReceiver, intentFilter)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Android_homeworkTheme {
        Greeting("Android")
    }
}