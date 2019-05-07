package at.klapfinator.silo;

import java.util.List;

/**
 * LogSender Interface
 */
public interface LogSender {
    void pushLogs(List<DeviceLogData> logDataList);
}
