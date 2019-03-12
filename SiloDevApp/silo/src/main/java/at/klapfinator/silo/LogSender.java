package at.klapfinator.silo;

import java.util.List;

public interface LogSender {
    public void pushLogs(List<DeviceLogData> logDataList);
}
