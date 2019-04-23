package at.klapfinator.silo;


public interface LogFormat {
    String getFormattedLogString(int logLevel, String tag, String message);
}
