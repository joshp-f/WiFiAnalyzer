package comp6733.wifiapscanner

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.work.*
import java.util.concurrent.TimeUnit
import android.widget.EditText



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val simpleEditText = findViewById<EditText>(R.id.distanceText)
            val str = simpleEditText.text.toString()
            Toast.makeText(applicationContext, "Starting Single 3x Scan...", Toast.LENGTH_LONG).show()
            val wifiScanWork: OneTimeWorkRequest
            if (str == "") {
                wifiScanWork = OneTimeWorkRequestBuilder<WiFiScannerWorker>().setInputData(Data.Builder().putInt("mode", 1).build()).build()
            } else {
                wifiScanWork = OneTimeWorkRequestBuilder<WiFiScannerWorker>().setInputData(Data.Builder().putInt("mode", 1).putFloat("dist", str.toFloat()).build()).build()
            }
            WorkManager.getInstance().enqueue(wifiScanWork)
        }

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            Toast.makeText(applicationContext, "Starting Periodic 3x Scan...", Toast.LENGTH_LONG).show()
            val periodicWifiScanWork = PeriodicWorkRequestBuilder<WiFiScannerWorker>(15, TimeUnit.MINUTES).setInputData(Data.Builder().putInt("mode", 1).build()).build()
            WorkManager.getInstance().enqueueUniquePeriodicWork("Periodic Scan", ExistingPeriodicWorkPolicy.KEEP, periodicWifiScanWork)
        }

        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener {
            Toast.makeText(applicationContext, "Stopping Periodic 3x Scan...", Toast.LENGTH_LONG).show()
            WorkManager.getInstance().cancelUniqueWork("Periodic Scan")
        }

        val button4 = findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            Toast.makeText(applicationContext, "Starting Single 3x Scan...", Toast.LENGTH_LONG).show()
            val wifiScanSendWork = OneTimeWorkRequestBuilder<WiFiScannerWorker>().setInputData(Data.Builder().putInt("mode", 2).build()).build()
            WorkManager.getInstance().enqueue(wifiScanSendWork)
        }

    }
}
