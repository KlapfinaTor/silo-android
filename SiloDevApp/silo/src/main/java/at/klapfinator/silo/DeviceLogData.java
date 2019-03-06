package at.klapfinator.silo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class DeviceLogData {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "message")
    String message;

    @ColumnInfo(name = "dateLogged")
    String dateLogged;

    void setMessage(String message) {
        this.message = message;
    }

    void setDateLogged(String dateLogged) {
        this.dateLogged = dateLogged;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public String getDateLogged() {
        return dateLogged;
    }

    public long getId() {
        return id;
    }
}
