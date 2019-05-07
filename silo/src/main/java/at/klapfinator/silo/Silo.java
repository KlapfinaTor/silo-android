package at.klapfinator.silo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/*
  The Simple Logger
 */
public final class Silo {
    private static boolean logCatOutputEnabled;
    private static final String TAG = "Silo";
    private static Context context;
    private static LogSender logSender;
    private static LogFormat logFormatHelper;
    private static String url;
    private static int batchLogSize;
    private static int logLevel = 2; //VERBOSE


    /**
     * Empty constructor
     */
    private Silo() {
        // none
    }

    /**
     * Initializes the Silo Logger with default settings.
     * +HTTPServer
     * +LogFormatHelper witch saves the log messages as json string
     * +Output to Logcat is disabled
     * +Batchlogsize is 100
     *
     * @param context The current context.
     */
    public static void initialize(@NonNull Context context) {
        initialize(context, new LogFormatHelper(context, true), logSender, true, 100);
    }

    /**
     * Initializes the Silo logger.
     *
     * @param context             The current context.
     * @param logFormatHelper     The LogformatHelper which will be used.
     * @param logSender           The logsender which will be used to send
     *                            the logs (DEFAULT HTTP)
     * @param logCatOutputEnabled Specifies if the logs will be passed through
     *                            to logcat when logged
     * @param batchLogSize        The amount of logs that will be send when
     *                            the push() method is called.
     */
    public static void initialize(@NonNull Context context, LogFormat logFormatHelper, LogSender logSender, Boolean logCatOutputEnabled, int batchLogSize) {
        Silo.context = context.getApplicationContext();
        Silo.logSender = logSender;
        Silo.logFormatHelper = logFormatHelper;
        Silo.logCatOutputEnabled = logCatOutputEnabled;
        Silo.batchLogSize = batchLogSize;
    }

    /**
     * Checks if Silo is initialized
     *
     * @return returns true if Silo is initialized
     */
    private static boolean isSiloInitialized() {
        if (Silo.context == null) {
            Log.e(TAG, "Silo isn't initialized: Context should not be null!");
            return false;
        } else
            return true;
    }

    /**
     * Sets the URL that is used for the LogSender
     *
     * @param url A url to a specific host where the logs will be send
     */
    public static void setUrl(String url) {
        Silo.url = url;
    }

    /**
     * Gets the current batchLogSize size
     *
     * @return Current batchLogSize
     */
    public static int getBatchLogSize() {
        return batchLogSize;
    }

    /**
     * Sets the batchLogSize
     *
     * @param batchLogSize batchLogSize
     */
    public static void setBatchLogSize(int batchLogSize) {
        Silo.batchLogSize = batchLogSize;
    }

    /**
     * Checks if the output to logcat is enabled
     *
     * @return if the output to logcat is enabled
     */
    public static boolean isLogCatOutputEnabled() {
        return logCatOutputEnabled;
    }

    /**
     * Sets the logcat output to true or false
     *
     * @param logCatOutputEnabled True or false
     */
    public static void setLogCatOutputEnabled(boolean logCatOutputEnabled) {
        Silo.logCatOutputEnabled = logCatOutputEnabled;
    }

    /**
     * Gets the current loglevel
     *
     * @return loglevel
     */
    public static int getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the loglevel. All logs that are lower then the loglevel will not be logged.
     * The default loglevel is VERBOSE(2).
     * <ul>
     * <li>{@link Log#VERBOSE}</li>
     * <li>{@link Log#DEBUG}</li>
     * <li>{@link Log#INFO}</li>
     * <li>{@link Log#WARN}</li>
     * <li>{@link Log#ERROR}</li>
     * <li>{@link Log#ASSERT}</li>
     * </ul>
     *
     * @param logLevel Loglevel
     */
    public static void setLogLevel(int logLevel) {
        Silo.logLevel = logLevel;
    }

    /**
     * Gets a specific amount of Logs as a List
     *
     * @param amount Amount of logs
     * @return Returns a list of logs
     * @throws ExecutionException   Exception
     * @throws InterruptedException Exception
     */
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


    /**
     * Inserts logs into the roomdb
     *
     * @param logMessage Log message to insert
     * @return Returns the ID of the inserted log
     * @throws ExecutionException   Exception
     * @throws InterruptedException Exception
     */
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
                }
            }
        });
    }

    /**
     * Push the saved logs to a external server, see @batchLogSize for the amount to push. The logmessages will be automatically deleted when successfully send.
     */
    public static void push() {
        if (!isSiloInitialized()) {
            Log.e(TAG, "Silo is not initialized!");
            return;
        }
        if (url == null) {
            Log.e(TAG, "URL is null!", new Exception("No URL specified!"));
            return;
        }

        if (Silo.logSender == null) {
            // Create a HttpSender as default when no other Sender is initialized!
            logSender = new HttpSender(url, context);
            Silo.i(TAG, "Http LogSender initialized!");
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // get data
                List<DeviceLogData> logsList = null;
                try {
                    logsList = Silo.getSpecificAmountOfLogsAsList(getBatchLogSize());
                } catch (Exception e) {
                    Silo.e(TAG, exceptionToString(e));
                }
                logSender.pushLogs(logsList);
            }
        });
    }

    /**
     * Converts a throwable to a string
     *
     * @param e Throwable
     * @return Returns the stacktrace from the throwable as a string
     */
    private static String exceptionToString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Gets a log by its id
     *
     * @param logId LogID
     * @return A DeviceDataLog
     * @throws ExecutionException   Exception
     * @throws InterruptedException Exception
     */
    private static DeviceLogData getLogById(final long logId) throws ExecutionException, InterruptedException {
        Callable<DeviceLogData> callable = new Callable<DeviceLogData>() {
            @Override
            public DeviceLogData call() {
                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                return dao.getLogById(logId);
            }
        };

        Future<DeviceLogData> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    /**
     * Gets the pending amount of logfiles in the database
     *
     * @return Amount of logfiles
     * @throws ExecutionException   Exception
     * @throws InterruptedException Exception
     */
    private static int getLogAmountInDatabase() throws ExecutionException, InterruptedException {
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() {
                DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
                return dao.count();
            }
        };
        Future<Integer> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    /**
     * Gets the pending amount of logfiles in the database.
     *
     * @return Amount of logs in the Database.
     */
    public static int getPendingLogAmount() {
        int logAmount = 0;
        try {
            logAmount = getLogAmountInDatabase();
        } catch (Exception e) {
            Silo.e(TAG, exceptionToString(e));
        }
        return logAmount;
    }

    /**
     * Sends a Logmessage directly to a external server.
     * The logmessage will be automatically deleted when successfully send.
     * The logmessage will always be send independently from the loglevel.
     *
     * @param message Log-message to send
     */
    public static void send(final String message) {
        //send logs from db to message broker or http depending on the setting
        if (url == null) {
            Log.e(TAG, "URL is null!", new Exception("No URL specified!"));
            return;
        }

        if (Silo.logSender == null) {
            logSender = new HttpSender(url, context);
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

                } catch (Exception e) {
                    Silo.e(TAG, exceptionToString(e));
                }
                logSender.pushLogs(logsList);
            }
        });
    }

    /**
     * Saves a log into the database when its loglevel is higher then the loglevel.
     *
     * @param priority  Priority of the log message
     * @param tag       Tag to identify the source of the log.
     * @param message   Log message to save
     * @param throwable Excpetion message to save
     */
    private static void log(int priority, @Nullable String tag, @NonNull final String message, @Nullable Throwable throwable) {
        if (priority < logLevel) {
            return;
        }
        String logMessage = message;
        if (logCatOutputEnabled) {
            Log.println(priority, tag, message);
        }
        if (throwable != null) {
            logMessage += Log.getStackTraceString(throwable);
        }
        try {
            insertLogIntoDb(logFormatHelper.getFormattedLogString(priority, tag, logMessage));
        } catch (Exception e) {
            Log.e(TAG, exceptionToString(e));
        }
    }
    /* Verbose */

    /**
     * Log a Verbose Log message.
     *
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void v(@NonNull String message, @NonNull Throwable throwable) {
        Silo.log(2, TAG, message, throwable);
    }

    /**
     * Log a Verbose Log message.
     *
     * @param message Log message you want to log.
     */
    public static void v(@NonNull String message) {
        Silo.log(2, TAG, message, null);
    }

    /**
     * Log a Verbose Logmessage.
     *
     * @param tag       Used to identify the source of a log message.
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void v(String tag, @NonNull String message, @NonNull Throwable throwable) {
        Silo.log(2, tag, message, throwable);
    }

    /**
     * Log a Verbose Logmessage.
     *
     * @param tag     Used to identify the source of a log message.
     * @param message Log message you want to log.
     */
    public static void v(String tag, @NonNull String message) {
        Silo.log(2, tag, message, null);
    }

    /* INFO */

    /**
     * Log a Info Log message.
     *
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void i(@NonNull String message, @NonNull Throwable throwable) {
        Silo.log(4, TAG, message, throwable);
    }

    /**
     * Log a Info Log message.
     *
     * @param message Log message you want to log.
     */
    public static void i(@NonNull String message) {
        Silo.log(4, TAG, message, null);
    }

    /**
     * Log a Info Logmessage.
     *
     * @param tag       Used to identify the source of a log message.
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void i(String tag, @NonNull String message, @NonNull Throwable throwable) {
        Silo.log(4, tag, message, throwable);
    }

    /**
     * Log a Info Logmessage.
     *
     * @param tag     Used to identify the source of a log message.
     * @param message Log message you want to log.
     */
    public static void i(String tag, @NonNull String message) {
        Silo.log(4, tag, message, null);
    }

    /* DEBUG */

    /**
     * Log a DEBUG Log message.
     *
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void d(@NonNull String message, @NonNull Throwable throwable) {
        Silo.log(1, TAG, message, throwable);
    }

    /**
     * Log a DEBUG Log message.
     *
     * @param message Log message you want to log.
     */
    public static void d(@NonNull String message) {
        Silo.log(1, TAG, message, null);
    }

    /**
     * Log a DEBUG Logmessage.
     *
     * @param tag       Used to identify the source of a log message.
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void d(String tag, @NonNull String message, @NonNull Throwable throwable) {
        Silo.log(1, tag, message, throwable);
    }

    /**
     * Log a DEBUG Logmessage.
     *
     * @param tag     Used to identify the source of a log message.
     * @param message Log message you want to log.
     */
    public static void d(String tag, @NonNull String message) {
        Silo.log(1, tag, message, null);
    }

    /* ERROR */

    /**
     * Log a ERROR Log message.
     *
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void e(@NonNull String message, @NonNull Throwable throwable) {
        Silo.log(6, TAG, message, throwable);
    }

    /**
     * Log a ERROR Log message.
     *
     * @param message Log message you want to log.
     */
    public static void e(@NonNull String message) {
        Silo.log(6, TAG, message, null);
    }

    /**
     * Log a ERROR Logmessage.
     *
     * @param tag       Used to identify the source of a log message.
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void e(String tag, @NonNull String message, @NonNull Throwable throwable) {
        Silo.log(6, tag, message, throwable);
    }

    /**
     * Log a ERROR Logmessage.
     *
     * @param tag     Used to identify the source of a log message.
     * @param message Log message you want to log.
     */
    public static void e(String tag, @NonNull String message) {
        Silo.log(6, tag, message, null);
    }

    /* WARNING */

    /**
     * Log a WARNING Log message.
     *
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void w(@NonNull String message, @NonNull Throwable throwable) {
        Silo.log(5, TAG, message, throwable);
    }

    /**
     * Log a WARNING Log message.
     *
     * @param message Log message you want to log.
     */
    public static void w(@NonNull String message) {
        Silo.log(5, TAG, message, null);
    }

    /**
     * Log a WARNING Logmessage.
     *
     * @param tag       Used to identify the source of a log message.
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void w(String tag, @NonNull String message, @NonNull Throwable throwable) {
        Silo.log(5, tag, message, throwable);
    }

    /**
     * Log a WARNING Logmessage.
     *
     * @param tag     Used to identify the source of a log message.
     * @param message Log message you want to log.
     */
    public static void w(String tag, @NonNull String message) {
        Silo.log(5, tag, message, null);
    }

    /* other */

    /**
     * Logs a what a terrible failure that should never had happend.
     *
     * @param message   Log message you want to log.
     * @param throwable A exception you want to log.
     */
    public static void wtf(@NonNull String message, @NonNull Throwable throwable) {
        Silo.log(1, null, message, throwable);
    }

    /**
     * Assert
     *
     * @param message Log message you want to log.
     */
    public static void a(@NonNull String message) {
        Silo.log(7, TAG, message, null);
    }
}
