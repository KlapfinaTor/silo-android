package at.klapfinator.silo;

import android.arch.persistence.room.Room;
import android.content.Context;

public class DeviceLogDatabaseAccess {
    private static DeviceLogDatabaseAccess instance;
    private DeviceLogDatabase database;

    public DeviceLogDatabaseAccess(Context context) {
        this.database = Room.databaseBuilder(context, DeviceLogDatabase.class, DeviceLogDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration() //TODO handle migration
                .build();
    }

    public synchronized static DeviceLogDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = new DeviceLogDatabaseAccess(context);
        }
        return instance.database;
    }
}
