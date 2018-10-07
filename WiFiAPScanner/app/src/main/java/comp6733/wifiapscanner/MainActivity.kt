package comp6733.wifiapscanner

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Toast.makeText(applicationContext, "Writing WiFi scan to file", Toast.LENGTH_LONG).show()
            val wifiScanWork = OneTimeWorkRequestBuilder<WiFiScannerWorker>().build()
            WorkManager.getInstance().enqueue(wifiScanWork)
        }

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            Toast.makeText(applicationContext, "Starting Periodic Scan", Toast.LENGTH_LONG).show()
            val periodicWifiScanWork = PeriodicWorkRequestBuilder<WiFiScannerWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance().enqueueUniquePeriodicWork("Periodic Scan", ExistingPeriodicWorkPolicy.KEEP, periodicWifiScanWork)
        }

        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener {
            Toast.makeText(applicationContext, "Stopping Periodic Scan", Toast.LENGTH_LONG).show()
            WorkManager.getInstance().cancelUniqueWork("Periodic Scan")
        }
    }
}
