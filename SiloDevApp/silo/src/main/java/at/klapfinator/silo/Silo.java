package at.klapfinator.silo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Silo {
    private static boolean logCatOutputEnabled = true;
    private static final String TAG = "Silo";
    private static final int DEFAULT_LOG_EXPIRY_TIME = 7 * 24 * 60 * 60; // 7 Days
    private static Context context;
    private static LogSender logSender;
    private static LogFormatHelper logFormatHelper;
    private static String url;
    private static int batchLogSize;


    private Silo() {
        // none
    }

    public static void initialize(@NonNull Context context) {

        initialize(context, new LogFormatHelper(context), logSender, true, 20);
    }

    public static void initialize(@NonNull Context context, LogFormatHelper logFormatHelper, LogSender logSender, Boolean logCatOutputEnabled, int batchLogSize) {
        Silo.context = context.getApplicationContext();
        Silo.logSender = logSender;
        Silo.logFormatHelper = logFormatHelper;
        Silo.logCatOutputEnabled = logCatOutputEnabled;
        Silo.batchLogSize = batchLogSize;
    }

    private static boolean isSiloInitialized() {
        if (Silo.context == null) {
            Log.e(TAG, "Silo isn't initialized: Context should not be null!");
            return false;
        } else
            return true;
    }

    public static void setUrl(String url) {
        Silo.url = url;
    }

    public static int getBatchLogSize() {
        return batchLogSize;
    }

    public static void setBatchLogSize(int batchLogSize) {
        Silo.batchLogSize = batchLogSize;
    }

    private static List<DeviceLogData> getSpecificAmountOfLogsAsList(final int amount) throws ExecutionException, InterruptedException {
        Callable<List<DeviceLogData>> callable = new Callable<List<DeviceLogData>>() {
            @Override
            public List<DeviceLogData> call() throws Exception {
                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                return dao.getSpecificAmountOfLogs(amount);
            }
        };

        Future<List<DeviceLogData>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    private static List<DeviceLogData> getAllLogsAsList() throws ExecutionException, InterruptedException {
        Callable<List<DeviceLogData>> callable = new Callable<List<DeviceLogData>>() {
            @Override
            public List<DeviceLogData> call() throws Exception {
                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                return dao.getAllLogs();
            }
        };

        Future<List<DeviceLogData>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    private static long insertLogIntoDb(final String logMessage) throws ExecutionException, InterruptedException {
        Callable<Long> callable = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Timestamp time = new Timestamp(System.currentTimeMillis());
                long timeStamp = time.getTime();
                DeviceLogData logData = new DeviceLogData();
                logData.setMessage(logMessage);
                logData.setDateLogged(timeStamp);

                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                return dao.insert(logData);
            }
        };

        Future<Long> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    /**
     * Delets specific logs
     *
     * @param logsToDelete a list of id's to delete
     * @throws ExecutionException   Exception
     * @throws InterruptedException Exception
     */
    static void deleteLogs(final List<Long> logsToDelete) throws ExecutionException, InterruptedException {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                for (Long log : logsToDelete) {
                    dao.deleteLogById(log);
                    Log.d(TAG, "Deleting Log with id: " + log);
                }
            }
        });
    }

    public static void push() {
        if (!isSiloInitialized()) {
            Log.e(TAG, "Silo is not initialized!");
            return;
        }
        //send logs from db to message broker or http depending on the setting
        if (url == null) {
            Log.e(TAG, "URL is null!", new Exception("No URL specified!"));
            return;
        }

        if (Silo.logSender == null) {
            logSender = new HttpSender(url, context, false);
            Log.e(TAG, "Http LogSender initialized!");
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // get data
                List<DeviceLogData> logsList = null;
                try {
                    logsList = Silo.getSpecificAmountOfLogsAsList(50);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (DeviceLogData log : logsList) {
                    Log.e(TAG, "LogID: " + log.getId());
                }
                logSender.pushLogs(logsList);
            }
        });
    }

    public static void log(int priority, @Nullable String tag, @NonNull final String message, @Nullable Throwable throwable) {
        if (logCatOutputEnabled) {
            Log.println(priority, tag, message);
        }
        try {
            insertLogIntoDb(logFormatHelper.getFormattedLogString(priority, tag, message));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public static DeviceLogData getLogById(final long logId) throws ExecutionException, InterruptedException {
        Callable<DeviceLogData> callable = new Callable<DeviceLogData>() {
            @Override
            public DeviceLogData call() throws Exception {
                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                return dao.getLogById(logId);
            }
        };

        Future<DeviceLogData> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }


    public static void send(final String message) {
        //send logs from db to message broker or http depending on the setting
        if (url == null) {
            Log.e(TAG, "URL is null!", new Exception("No URL specified!"));
            return;
        }

        if (Silo.logSender == null) {
            logSender = new HttpSender(url, context, false);
            Log.i(TAG, "Http LogSender initialized!");
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // get data
                List<DeviceLogData> logsList = new ArrayList<>();
                try {
                    DeviceLogData log = Silo.getLogById(insertLogIntoDb(logFormatHelper.getFormattedLogString(1, TAG, message)));
                    logsList.add(log);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logSender.pushLogs(logsList);
            }
        });
    }
}
