package at.klapfinator.silo;

import java.util.List;

public interface LogSender {
    boolean pushLogs(List<DeviceLogData> logDataList);
}
