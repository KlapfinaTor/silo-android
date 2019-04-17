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

    @Query("SELECT * FROM DeviceLogData ORDER BY id desc LIMIT :amount")
    List<DeviceLogData> getSpecificAmountOfLogs(int amount);

    @Query("SELECT * FROM DeviceLogData WHERE id = :id")
    DeviceLogData getLogById(int id);

    @Query("SELECT count(*) FROM DeviceLogData")
    int count();

    @Insert
    long insert(DeviceLogData dld);

    @Update
    void update(DeviceLogData dld);

    @Delete
    void delete(DeviceLogData dld);

    @Query("DELETE FROM DeviceLogData")
    void deleteAllLogs();

    @Query("DELETE FROM DeviceLogData WHERE dateLogged < :expireTime")
    void deleteOldLogs(int expireTime);
}
