package at.klapfinator.silo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DeviceLogDataDao {
    @Query("SELECT * FROM DeviceLogData")
    List<DeviceLogData> getAllLogs();

    @Query("SELECT * FROM DeviceLogData WHERE id = :id")
    DeviceLogData getLogById(int id);

    @Insert
    void insert(DeviceLogData dld);

    @Update
    void update(DeviceLogData dld);

    @Delete
    void delete(DeviceLogData dld);

    @Query("DELETE FROM DeviceLogData WHERE 1=1")
    void deleteAllLogs();
}
