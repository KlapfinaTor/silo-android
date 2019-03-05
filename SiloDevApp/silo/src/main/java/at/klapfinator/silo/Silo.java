package at.klapfinator.silo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public final class Silo {
    private static final Silo ourInstance = new Silo();
    private boolean enableLogCatOutput = true;
    private boolean enableDBLogging = true;
    private boolean enableRemoteLogging = true;

    public static Silo getInstance() {
        return ourInstance;
    }

    private Silo() {
    }

    public void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable throwable){
        //save to db

        if(enableLogCatOutput){
            Log.d(tag,message);
        }

    }

    public static void d(@NonNull String message, @Nullable Object... args) {
       //save to db
    }

    public static void push(){
        //send logs from db to message broker or http depending on the setting
    }
}
