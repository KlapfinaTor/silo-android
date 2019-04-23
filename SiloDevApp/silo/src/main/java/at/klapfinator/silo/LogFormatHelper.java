package at.klapfinator.silo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Locale;

/**
 * Logformathelper that formats a logmessage string with additional information
 */
public class LogFormatHelper implements LogFormat {
    private String androidId;
    private boolean sendAsJSONString;

    @SuppressLint("HardwareIds")
    public LogFormatHelper(Context context, boolean sendAsJSONString) {
        Context mContext = context.getApplicationContext();
        androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.sendAsJSONString = sendAsJSONString;
    }

    /**
     * Gets the formated Log message with additional information
     *
     * @param logLevel The loglevel
     * @param tag      Is used to identifiy the source of the message
     * @param message  Logmessage
     * @return A string or a formatedJSON string @see sendAsJsonString
     */
    public String getFormattedLogString(int logLevel, String tag, String message) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        Long timeStamp = time.getTime();
        String appVersionName = BuildConfig.VERSION_NAME;
        String appId = BuildConfig.APPLICATION_ID;
        String osVersion = "Android-" + Build.VERSION.RELEASE;
        String logLevelString = getLogLevelName(logLevel);
        String currentDeviceLanguage = Locale.getDefault().getDisplayLanguage();

        if (androidId == null) {
            androidId = "android_id_not_set";
        }
        if (sendAsJSONString) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject
                        .put("TimeStamp", time)
                        .put("DeviceID", androidId)
                        .put("AppID", appId)
                        .put("AppVersionName", appVersionName)
                        .put("DeviceLanguage", currentDeviceLanguage)
                        .put("OSVersion", osVersion)
                        .put("LogMessage", new JSONObject()
                                .put("LogLevel", logLevelString)
                                .put("TAG", tag)
                                .put("Message", message
                                )
                        );
            } catch (JSONException e) {
                Silo.e(Log.getStackTraceString(e));
            }

            return jsonObject.toString();
        } else {

            return String.format(Locale.US, "%d | %s | %s | %s | %s | %s | [%s / %s]: %s",
                    timeStamp, androidId, appId, appVersionName, currentDeviceLanguage, osVersion, logLevelString, tag, message);
        }
    }

    /**
     * Helper method to get the correct loglevel name
     *
     * @param logLevel Loglevel
     * @return Loglevel name
     */
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
