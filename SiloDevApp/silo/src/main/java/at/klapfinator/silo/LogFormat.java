package at.klapfinator.silo;

/**
 * Logformat Interface
 */
public interface LogFormat {
    String getFormattedLogString(int logLevel, String tag, String message);
}
