package at.klapfinator.silo;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Saves the logs into a database
 */
@Database(entities = {DeviceLogData.class}, version = 1)
abstract class DeviceLogDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "logdata1.db";

    public abstract DeviceLogDataDao deviceLogDataDao();

}
