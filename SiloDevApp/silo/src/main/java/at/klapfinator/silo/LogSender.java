package at.klapfinator.silo;

import java.util.List;

public interface LogSender {
    void pushLogs(List<DeviceLogData> logDataList);
}
