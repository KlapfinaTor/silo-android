package at.klapfinator.silo;

import android.arch.persistence.room.Room;
import android.content.Context;

class DeviceLogDatabaseAccess {
    private static DeviceLogDatabaseAccess instance;
    private DeviceLogDatabase database;

    DeviceLogDatabaseAccess(Context context) {
        this.database = Room.databaseBuilder(context, DeviceLogDatabase.class, DeviceLogDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    static synchronized DeviceLogDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = new DeviceLogDatabaseAccess(context);
        }
        return instance.database;
    }
}
