package at.klapfinator.silo;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.sql.Timestamp;
import java.util.Locale;


public class LogFormatHelper {

    private static String androidId;

    public LogFormatHelper(Context context) {
        Context mContext = context.getApplicationContext();
        // FIXME
        LogFormatHelper.androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getFormatedLogString(int logLevel, String tag, String message) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        Long timeStamp = time.getTime();
        String deviceName = BuildConfig.VERSION_NAME;
        String osVersion = "Android-" + Build.VERSION.RELEASE;
        String logLevelString = getLogLevelName(logLevel);
        String currentDeviceLanguage = Locale.getDefault().getDisplayLanguage();

        return String.format("%d | %s | %s | %s | [%s]: %s ", timeStamp, currentDeviceLanguage, deviceName, osVersion, logLevelString, message);
    }

    private static String getLogLevelName(int logLevel) {
        String logLevelString;
        switch (logLevel) {
            case Log.VERBOSE:
                logLevelString = "VERBOSE";
                break;
            case Log.DEBUG:
                logLevelString = "DEBUG";
                break;
            case Log.INFO:
                logLevelString = "INFO";
                break;
            case Log.WARN:
                logLevelString = "WARN";
                break;
            case Log.ERROR:
                logLevelString = "ERROR";
                break;
            case Log.ASSERT:
                logLevelString = "ASSERT";
                break;
            default:
                logLevelString = "NONE";
        }
        return logLevelString;
    }
}
