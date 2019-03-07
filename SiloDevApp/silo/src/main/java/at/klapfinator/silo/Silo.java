package at.klapfinator.silo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Silo {
    private static final Silo ourInstance = new Silo();

    private boolean enableLogCatOutput = true;
    private boolean enableDBLogging = true;
    private boolean enableRemoteLogging = true;

    private final String TAG = "Silo";
    private final int DEFAULT_LOG_EXPIRY_TIME = 7 * 24 * 60 * 60; // 7 Days
    private Context context;
    private ExecutorService executorService;

    public static Silo getInstance() {
        return ourInstance;
    }


    public void initialize(@NonNull Context context) {
        initialize(context, DEFAULT_LOG_EXPIRY_TIME);
    }

    public void initialize(@NonNull Context context, int logExpiryTimeInSeconds) {
        if (context == null) {
            Log.e(TAG, "Silo isn't initialized: Context couldn't be null");
            return;
        }

        this.context = context.getApplicationContext();

    }

    public void log(int priority, @Nullable String tag, @Nullable final String message, @Nullable Throwable throwable) {
        //save to db

        if (enableLogCatOutput) {
            Log.d(tag, message);
        }

        saveToDatabase(message);

        // DELETME print all inserted logs


    }

    public List<DeviceLogData> getAllLogsAsList() {
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
        //FIXME
        return null;
    }

    private boolean isSiloInitialized() {
        if (context == null)
            return false;
        else
            return true;
    }

    private void saveToDatabase(final String message) {
        if (message == null)
            return;

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                DeviceLogData logData = new DeviceLogData();
                logData.setMessage(message.toString());
                logData.setDateLogged("12345");

                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                dao.insert(logData);
                Log.d(TAG, "Insert into database complete: " + message.toString());
            }
        });
        t1.start();
    }

    public void d(@NonNull String message, @Nullable Object... args) {
        //save to db

        if (enableLogCatOutput) {
            Log.d(TAG, message);
        }
    }

    public void push() {
        //send logs from db to message broker or http depending on the setting
    }

}
