package at.klapfinator.silo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Silo {
    private static boolean logCatOutputEnabled = true;

    private static final String TAG = "Silo";
    private static final int DEFAULT_LOG_EXPIRY_TIME = 7 * 24 * 60 * 60; // 7 Days
    private static Context context;
    private static ExecutorService executorService;
    private static LogSender logSender;
    private static LogFormatHelper logFormatHelper;


    public static void initialize(@NonNull Context context) {
        initialize(context, DEFAULT_LOG_EXPIRY_TIME, new LogFormatHelper(context), null);
    }

    public static void initialize(@NonNull Context context, int logExpiryTimeInSeconds, LogFormatHelper logFormatHelper, LogSender logSender) {
        Silo.context = context.getApplicationContext();
        Silo.logSender = logSender;
        Silo.logFormatHelper = logFormatHelper;
    }

    public static void log(int priority, @Nullable String tag, @Nullable final String message, @Nullable Throwable throwable) {
        if (logCatOutputEnabled) {
            Log.d(tag, message);
        }
        saveToDatabase(logFormatHelper.getFormattedLogString(priority, tag, message));
    }


    public static List<DeviceLogData> getAllLogsAsList() {
        if (executorService == null)
            executorService = Executors.newSingleThreadExecutor();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                    List<DeviceLogData> logsList = dao.getAllLogs();

                    for (DeviceLogData log : logsList) {
                        Log.i(TAG, log.getId() + "|" + log.getMessage() + "|" + log.getDateLogged());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        executorService.submit(runnable);
        //FIXME RETURN LIST
        return null;
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
                DeviceLogData logData = new DeviceLogData();
                logData.setMessage(message);
                logData.setDateLogged("12345");

                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                dao.insert(logData);
                Log.d(TAG, "Insert into database complete: " + message);
            }
        });
        t1.start();
    }

    public static void d(@NonNull String message, @Nullable Object... args) {
        if (logCatOutputEnabled) {
            Log.d(TAG, message);
        }
        log(1, null, message, null);
    }

    public static void push() {
        if (Silo.logSender == null) {
            Log.e(TAG, "No LogSender initialized!");
            return;
        }

        logSender.pushLogs();
        //send logs from db to message broker or http depending on the setting
    }
}
