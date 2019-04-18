package at.klapfinator.silo;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.sql.Timestamp;
import java.util.Locale;


public class LogFormatHelper {
    private String androidId;

    public LogFormatHelper(Context context) {
        Context mContext = context.getApplicationContext();
        // FIXME
        androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    String getFormattedLogString(int logLevel, String tag, String message) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        Long timeStamp = time.getTime();
        String appVersionName = BuildConfig.VERSION_NAME;
        int appVersion = BuildConfig.VERSION_CODE;
        String appId = BuildConfig.APPLICATION_ID;
        String osVersion = "Android-" + Build.VERSION.RELEASE;
        String logLevelString = getLogLevelName(logLevel);
        String currentDeviceLanguage = Locale.getDefault().getDisplayLanguage();

        if (androidId == null) {
            androidId = "android_id_not_set";
        }

        return String.format("%d | %s | %s | %s | %s | %s | [%s / %s]: %s",
                timeStamp, androidId, appId, appVersionName, currentDeviceLanguage, osVersion, logLevelString, tag, message);
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