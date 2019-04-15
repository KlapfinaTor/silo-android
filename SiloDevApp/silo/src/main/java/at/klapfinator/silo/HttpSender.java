package at.klapfinator.silo;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HttpSender implements LogSender {
    private static final String TAG = "Silo";
    private String url;
    private Context context;
    private List<DeviceLogData> logDataList;

    HttpSender(String url, Context context) {
        this.url = url;
        this.context = context;
    }

    @Override
    public void pushLogs(List<DeviceLogData> logDataList) {
        this.logDataList = logDataList;

        try {
            final JSONObject paramJson = new JSONObject();
            for (DeviceLogData log : logDataList) {
                //Log.i(TAG, log.getId() + "|" + log.getMessage() + "|" + log.getDateLogged());
                paramJson.put(String.valueOf(log.getId()), log.getMessage());
            }

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, paramJson, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("Silo", "Response from http: " + response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "" + error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> postMap = new HashMap<>();
                    postMap.put("param1", "value1");

                    //TODO json in hashmap convertieren
                    for (DeviceLogData log : paramJson) {
                        //Log.i(TAG, log.getId() + "|" + log.getMessage() + "|" + log.getDateLogged());
                        paramJson.put(String.valueOf(log.getId()), log.getMessage());
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
