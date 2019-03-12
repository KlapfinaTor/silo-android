package at.klapfinator.silo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.Timestamp;
import java.util.List;

public final class Silo {
    private static boolean logCatOutputEnabled = true;

    private static final String TAG = "Silo";
    private static final int DEFAULT_LOG_EXPIRY_TIME = 7 * 24 * 60 * 60; // 7 Days
    private static Context context;
    private static LogSender logSender;
    private static LogFormatHelper logFormatHelper;


    public static void initialize(@NonNull Context context) {
        logSender = new HttpSender();
        initialize(context, DEFAULT_LOG_EXPIRY_TIME, new LogFormatHelper(context), logSender);
    }

    public static void initialize(@NonNull Context context, int logExpiryTimeInSeconds, LogFormatHelper logFormatHelper, LogSender logSender) {
        Silo.context = context.getApplicationContext();
        Silo.logSender = logSender;
        Silo.logFormatHelper = logFormatHelper;
    }


    private static List<DeviceLogData> getAllLogsAsList() {
        DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
        return dao.getAllLogs();
    }

    private static boolean isSiloInitialized() {
        if (Silo.context == null) {
            Log.e(TAG, "Silo isn't initialized: Context couldn't be null");
            return false;
        } else
            return true;
    }


    private static void saveToDatabase(final String message) {
        if (!isSiloInitialized())
            return;

        if (message == null)
            return;

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Timestamp time = new Timestamp(System.currentTimeMillis());
                long timeStamp = time.getTime();
                DeviceLogData logData = new DeviceLogData();
                logData.setMessage(message);
                logData.setDateLogged(timeStamp);

                try {
                    DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                    dao.insert(logData);
                } catch (Exception ex) {
                    Log.e(TAG, "Error while inserting into database: " + ex.toString());
                }

                Log.d(TAG, "Insert into database complete: " + message);
            }
        });
        t1.start();
    }

    public static void push() {
        if (Silo.logSender == null) {
            Log.e(TAG, "No LogSender initialized!");
            return;
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // get data
                List<DeviceLogData> logsList = Silo.getAllLogsAsList();

                logSender.pushLogs(logsList);
            }
        });


        Log.i(TAG, "Push called!");
        //send logs from db to message broker or http depending on the setting
    }

    public static void log(int priority, @Nullable String tag, @Nullable final String message, @Nullable Throwable throwable) {
        if (logCatOutputEnabled) {
            Log.d(tag, message);
        }

        saveToDatabase(logFormatHelper.getFormattedLogString(priority, tag, message));
    }

    public static void d(@NonNull String message, @Nullable Object... args) {
        if (logCatOutputEnabled) {
            Log.d(TAG, message);
        }

        log(1, null, message, null);
    }
}
