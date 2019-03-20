package at.klapfinator.silo;

import android.util.Log;

import java.util.List;

class HttpSender implements LogSender {
    private static final String TAG = "Silo";

    @Override
    public boolean pushLogs(List<DeviceLogData> logDataList) {
        Log.i(TAG, "Pushing Logs!");
        for (DeviceLogData log : logDataList) {
            Log.i(TAG, log.getId() + "|" + log.getMessage() + "|" + log.getDateLogged());
        }
        Log.i(TAG, "Logs pushed!");
        return true;
    }
}
