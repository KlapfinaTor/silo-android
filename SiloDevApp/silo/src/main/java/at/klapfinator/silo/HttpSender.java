package at.klapfinator.silo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HttpSender implements LogSender {
    private static final String TAG = "Silo";
    private String url;
    private Context context;

    HttpSender(String url, Context context) {
        this.url = url;
        this.context = context;
    }

    @Override
    public void pushLogs(final List<DeviceLogData> logDataList) {
        //dont try to push a empty list
        if (logDataList.isEmpty()) {
            return;
        }
        //helper Class
        @SuppressWarnings("WeakerAccess")
        class StringRequestHelper extends StringRequest {
            public List<DeviceLogData> listData;

            StringRequestHelper(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener, List<DeviceLogData> data) {
                super(method, url, listener, errorListener);
                this.listData = data;
            }
        }

        try {
            StringRequestHelper stringRequest = new StringRequestHelper(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Silo", "Response from http: " + response);
                    List<Long> logsToDelete = new ArrayList<>();

                    for (DeviceLogData log : logDataList) {
                        logsToDelete.add(log.getId());
                    }
                    try {
                        Silo.deleteLogs(logsToDelete);
                    } catch (Exception e) {
                        Silo.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error while sending logs with httpSender: " + error);
                }
            }, logDataList) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> postMap = new HashMap<>();

                    for (DeviceLogData log : listData) {
                        postMap.put(String.valueOf(log.getId()), log.getMessage());
                    }
                    return postMap;
                }
            };
            Volley.newRequestQueue(context).add(stringRequest);

        } catch (Exception e) {
            Log.e(TAG, "Excpetion occured while sending POST Request", e);
        }
    }
}
