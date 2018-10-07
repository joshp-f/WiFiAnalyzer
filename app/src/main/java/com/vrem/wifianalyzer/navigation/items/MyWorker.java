package com.vrem.wifianalyzer.navigation.items;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.work.Worker;

public class MyWorker extends Worker {
    @Override
    public Result doWork() {
        Log.d("Debugging Message", "Worker doing work");
        //MainContext.INSTANCE.getScannerService().update();
        MainContext.INSTANCE.getScannerService().pause();
        MainContext.INSTANCE.getScannerService().resume();
        final String TIME_STAMP_FORMAT = "yyyy/MM/dd HH:mm:ss";
        List<WiFiDetail> wiFiDetails = MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
        String timestamp = new SimpleDateFormat(TIME_STAMP_FORMAT).format(new Date());
        String data = getData(timestamp, wiFiDetails);

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WiFi Logs");
        if (dir.mkdirs()) {
            Log.e("Debugging Message", "Directory created");
        }
        try {
            File file = new File(dir.getAbsolutePath() + "/log.txt");
            FileOutputStream outputStream = new FileOutputStream(file, true);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        Log.d("MyApp", "Made a logfile");
        return Result.SUCCESS;

    }

    @NonNull
    String getData(String timestamp, @NonNull List<WiFiDetail> wiFiDetails) {
        final StringBuilder result = new StringBuilder();
        result.append(
                String.format(Locale.ENGLISH,
                        "Time Stamp|SSID|BSSID|Strength|Primary Channel|Primary Frequency|Center Channel|Center Frequency|Width (Range)|Distance|Security%n"));
        IterableUtils.forEach(wiFiDetails, new WiFiDetailClosure(timestamp, result));
        return result.toString();
    }
    private class WiFiDetailClosure implements Closure<WiFiDetail> {
        private final StringBuilder result;
        private final String timestamp;

        private WiFiDetailClosure(String timestamp, @NonNull StringBuilder result) {
            this.result = result;
            this.timestamp = timestamp;
        }

        @Override
        public void execute(WiFiDetail wiFiDetail) {
            WiFiSignal wiFiSignal = wiFiDetail.getWiFiSignal();
            result.append(String.format(Locale.ENGLISH, "%s|%s|%s|%ddBm|%d|%d%s|%d|%d%s|%d%s (%d - %d)|%s|%s%n",
                    timestamp,
                    wiFiDetail.getSSID(),
                    wiFiDetail.getBSSID(),
                    wiFiSignal.getLevel(),
                    wiFiSignal.getPrimaryWiFiChannel().getChannel(),
                    wiFiSignal.getPrimaryFrequency(),
                    WiFiSignal.FREQUENCY_UNITS,
                    wiFiSignal.getCenterWiFiChannel().getChannel(),
                    wiFiSignal.getCenterFrequency(),
                    WiFiSignal.FREQUENCY_UNITS,
                    wiFiSignal.getWiFiWidth().getFrequencyWidth(),
                    WiFiSignal.FREQUENCY_UNITS,
                    wiFiSignal.getFrequencyStart(),
                    wiFiSignal.getFrequencyEnd(),
                    wiFiSignal.getDistance(),
                    wiFiDetail.getCapabilities()));
        }
    }
}
