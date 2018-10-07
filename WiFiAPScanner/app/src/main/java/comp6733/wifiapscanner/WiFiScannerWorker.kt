package comp6733.wifiapscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.sql.Timestamp
import java.text.SimpleDateFormat

class WiFiScannerWorker(context : Context, params : WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        var resultList: ArrayList<ScanResult>
        lateinit var wm: WifiManager

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                var output = "Time Stamp|SSID|BSSID|RSSI\n"
                val timestamp = Timestamp(System.currentTimeMillis()).toString()
                resultList = wm.scanResults as ArrayList<ScanResult>
                Log.d("TESTING", resultList.toString())
                applicationContext.unregisterReceiver(this)

                for (result in resultList) {
                    output += timestamp + "|" + result.SSID + "|" + result.BSSID + "|" + result.level + "\n"
                }

                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WiFi Logs")
                if (dir.mkdirs()) {
                    Log.d("Debugging Message", "Directory created")
                }
                try {
                    val file = File(dir.absolutePath + "/log.txt")
                    val outputStream = FileOutputStream(file, true)
                    outputStream.write(output.toByteArray())
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wm.startScan()
        applicationContext.registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        return Result.SUCCESS
    }

}
