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
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import java.io.File
import java.io.FileOutputStream
import java.sql.Timestamp
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import android.provider.Settings.Secure


class WiFiScannerWorker(context : Context, params : WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        var count = 1
        val timestamp = Timestamp(System.currentTimeMillis()).toString()
        val wm: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val mode = inputData.getInt("mode", 0)
        val dist = inputData.getFloat("dist", (-1).toFloat())
        var output = if (dist == (-1).toFloat()) "Time Stamp|SSID|BSSID|RSSI\n" else "Time Stamp|SSID|BSSID|RSSI|dist\n"

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                val resultList = wm.scanResults as ArrayList<ScanResult>
                Log.d("TESTING", "WiFi scan " + count.toString() + " received")
                Log.d("TESTING", resultList.toString())

                for (result in resultList) {
                    if (dist == (-1).toFloat()) {
                        output += timestamp + "|" + result.SSID + "|" + result.BSSID + "|" + result.level + "\n"
                    } else {
                        output += timestamp + "|" + result.SSID + "|" + result.BSSID + "|" + result.level + "|" + dist + "\n"
                    }
                }

                count++
                if (count <= 3) {
                    wm.startScan()
                } else {
                    applicationContext.unregisterReceiver(this)
                    // Write to file
                    if (mode == 1) {
                        Toast.makeText(applicationContext, "Scan complete. Writing WiFi scan to file.", Toast.LENGTH_LONG).show()
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
                        // Send to server
                    } else if (mode == 2) {
                        Toast.makeText(applicationContext, "Scan complete. Sending WiFi scan to server.", Toast.LENGTH_LONG).show()
                        //Log.d("output", output)
                        val url = "https://iotproximity.herokuapp.com"
                        val pr = object : StringRequest(Request.Method.POST, url, Response.Listener { response -> Log.d("POST Response", response);  Toast.makeText(applicationContext, "Jaccard Coefficient = $response", Toast.LENGTH_LONG).show() }, Response.ErrorListener { Log.d("POST Error", "") }) {
                            override fun getParams(): Map<String, String> {
                                val params = HashMap<String, String>()
                                params["data"] = output
                                params["id"] = Secure.getString(applicationContext.contentResolver, Secure.ANDROID_ID)
                                Log.d("ID", Secure.getString(applicationContext.contentResolver, Secure.ANDROID_ID))
                                return params
                            }
                        }
                        //val pr = StringRequest(Request.Method.GET, url, Response.Listener<String> { response -> Log.d("GET response", response) }, Response.ErrorListener {Log.d("GET error", "GET error")})
                        Volley.newRequestQueue(applicationContext).add(pr)
                    }
                }
            }
        }
        wm.startScan()
        applicationContext.registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        return Result.SUCCESS
    }

}
