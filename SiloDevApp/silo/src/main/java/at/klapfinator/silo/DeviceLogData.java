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
    private String message;

    @ColumnInfo(name = "dateLogged")
    private long dateLogged;

    void setMessage(String message) {
        this.message = message;
    }

    void setDateLogged(long dateLogged) {
        this.dateLogged = dateLogged;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public long getDateLogged() {
        return dateLogged;
    }

    public long getId() {
        return id;
    }
}
