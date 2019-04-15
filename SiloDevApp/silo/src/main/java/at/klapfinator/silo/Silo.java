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
    private static int logExpiryTimeInSeconds;
    private static String url;


    private Silo() {
        // none
    }

    public static void initialize(@NonNull Context context) {

        initialize(context, DEFAULT_LOG_EXPIRY_TIME, new LogFormatHelper(context), logSender, true);
    }

    public static void initialize(@NonNull Context context, int logExpiryTimeInSeconds, LogFormatHelper logFormatHelper, LogSender logSender, Boolean logCatOutputEnabled) {
        Silo.context = context.getApplicationContext();
        Silo.logSender = logSender;
        Silo.logFormatHelper = logFormatHelper;
        Silo.logCatOutputEnabled = logCatOutputEnabled;
        Silo.logExpiryTimeInSeconds = logExpiryTimeInSeconds;
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


    private static void saveToDatabase(String message) {
        if (!isSiloInitialized())
            return;

        if (message == null)
            return;


        class SaveToDbTask implements Runnable {
            private String msg;

            private SaveToDbTask(String message) {
                msg = message;
            }

            @Override
            public void run() {
                Timestamp time = new Timestamp(System.currentTimeMillis());
                long timeStamp = time.getTime();
                DeviceLogData logData = new DeviceLogData();
                logData.setMessage(msg);
                logData.setDateLogged(timeStamp);

                try {
                    DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                    dao.insert(logData);
                } catch (Exception ex) {
                    Log.e(TAG, "Error while inserting into database: " + ex.toString());
                }

                Log.d(TAG, "Insert into database complete: " + msg);
            }
        }
        Thread t = new Thread(new SaveToDbTask(message));
        t.start();
    }

    public static void push() {
        //send logs from db to message broker or http depending on the setting
        if (url == null) {
            Log.e(TAG, "URL is null!", new Exception("No URL specified!"));
            return;
        }

        if (Silo.logSender == null) {
            logSender = new HttpSender(url, context);
            Log.e(TAG, "Http LogSender initialized!");
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // get data
                List<DeviceLogData> logsList = Silo.getAllLogsAsList();

                logSender.pushLogs(logsList);
                    //TODO delete pushed logs from database!
                //Log.i(TAG, "Logs successfully pushed!");

            }
        });
    }

    public static void log(int priority, @Nullable String tag, @NonNull final String message, @Nullable Throwable throwable) {
        if (logCatOutputEnabled) {
            Log.println(priority, tag, message);
        }
        saveToDatabase(logFormatHelper.getFormattedLogString(priority, tag, message));
    }

    //Info
    public static void i(@NonNull String message, @Nullable Throwable throwable) {
        Silo.log(4, TAG, message, throwable);
    }

    public static void i(@NonNull String message) {
        Silo.log(4, TAG, message, null);
    }

    public static void i(String tag, @NonNull String message, @Nullable Throwable throwable) {
        Silo.log(4, tag, message, throwable);
    }

    public static void i(String tag, @NonNull String message) {
        Silo.log(4, tag, message, null);
    }


    public static void d(@NonNull String message, @Nullable Throwable throwable) {
        Silo.log(1, null, message, throwable);
    }

    public static void send(String message) {
        saveToDatabase(logFormatHelper.getFormattedLogString(1, TAG, message));

    }

    public static void setUrl(String url) {
        Silo.url = url;
    }
}
