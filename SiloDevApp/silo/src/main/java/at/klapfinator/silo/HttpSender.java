package at.klapfinator.silo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HttpSender implements LogSender {
    private static final String TAG = "Silo";
    private final Boolean sendAsJSON;
    private String url;
    private Context context;

    HttpSender(String url, Context context, Boolean sendAsJSON) {
        this.url = url;
        this.context = context;
        this.sendAsJSON = sendAsJSON;
    }

    @Override
    public void pushLogs(List<DeviceLogData> logDataList) {
        if (sendAsJSON) {
            pushLogsAsJSON(logDataList);
        } else {
            pushLogsAsString(logDataList);
        }
    }

    private void pushLogsAsJSON(List<DeviceLogData> logDataList) {
        JSONObject jsonData = new JSONObject();
        try {
            for (DeviceLogData log : logDataList) {
                jsonData.put(String.valueOf(log.getDateLogged()), log.getMessage());
            }

        } catch (Exception e) {
            Log.e(TAG, "Excpetion occured while building JSON Object", e);
        }

        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("Silo", "Response from http: " + response);
                            Silo.deleteAllLogs();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error while sending logs with httpSender: " + error);
                        }
                    });
            Volley.newRequestQueue(context).add(jsonObjectRequest);

        } catch (Exception e) {
            Log.e(TAG, "Excpetion occured while sending POST Request JSON", e);
        }
    }

    private void pushLogsAsString(List<DeviceLogData> logDataList) {
        class StringRequestHelper extends StringRequest {
            public List<DeviceLogData> listData;

            public StringRequestHelper(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener, List<DeviceLogData> data) {
                super(method, url, listener, errorListener);
                this.listData = data;
            }
        }

        try {
            StringRequestHelper stringRequest = new StringRequestHelper(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Silo", "Response from http: " + response);
                    Silo.deleteAllLogs();
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
