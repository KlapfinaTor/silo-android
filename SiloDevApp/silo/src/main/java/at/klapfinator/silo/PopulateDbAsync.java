package at.klapfinator.silo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Timestamp;

public class PopulateDbAsync extends AsyncTask<Object, Object, Object> {
    private AsyncResponse delegate = null; //Callback interface
    private final String TAG = "Silo";
    private Context context;
    private long idOfInsertedRow;
    private String msg;

    public PopulateDbAsync(AsyncResponse asyncResponse, Context context, String msg) {
        delegate = asyncResponse;//Assigning call back interface
        this.context = context;
        this.msg = msg;
    }

    @Override
    protected Object doInBackground(Object... params) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        long timeStamp = time.getTime();
        DeviceLogData logData = new DeviceLogData();
        logData.setMessage(msg);
        logData.setDateLogged(timeStamp);

        try {
            DeviceLogDataDao dao = DeviceLogDatabaseAccess.getDatabase(context).deviceLogDataDao();
            idOfInsertedRow = dao.insert(logData);
        } catch (Exception ex) {
            Log.e(TAG, "Error while inserting into database: " + ex.toString());
        }

        return idOfInsertedRow;
    }

    @Override
    protected void onPostExecute(Object result) {
        delegate.processFinish(result);
    }
    //onDestroy() den delegate auf null setzen
}
