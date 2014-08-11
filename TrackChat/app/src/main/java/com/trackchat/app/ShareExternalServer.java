package com.trackchat.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Dipanshu on 7/30/2014.
 */
public class ShareExternalServer
{
    public String shareRegIdWithAppServer(final Context context, final String regId,final String lat,final String log
        ,final String user_message,final String receiver_number)
    {
        String result = "";
        final SharedPreferences prefs = context.getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String mPhoneNumber = prefs.getString("Number", "");
        String mname=prefs.getString("Name", "");

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("regId", regId);
        paramsMap.put("from", mPhoneNumber);
        paramsMap.put("to", receiver_number);
        String latlong=lat+":"+log;
        paramsMap.put("message", user_message);
        paramsMap.put("latlong", latlong);
        paramsMap.put("name", mname);

        try {
            URL serverUrl = null;
            try
            {
                serverUrl = new URL(Config.APP_SERVER_URL);
            }
            catch (MalformedURLException e)
            {
                Log.e("AppUtil", "URL Connection Error: "+ Config.APP_SERVER_URL, e);
                result = "Invalid URL: " + Config.APP_SERVER_URL;
            }

            StringBuilder postBody = new StringBuilder();
            Iterator<Map.Entry<String, String>> iterator = paramsMap.entrySet().iterator();

            while (iterator.hasNext())
            {
                Map.Entry<String, String> param = iterator.next();
                postBody.append(param.getKey()).append('=').append(param.getValue());
                if (iterator.hasNext())
                {
                    postBody.append('&');
                }
            }
            String body = postBody.toString();
            byte[] bytes = body.getBytes();
            HttpURLConnection httpCon = null;
            Log.e("AppUtil", "message " + body);
            try
            {
                httpCon = (HttpURLConnection) serverUrl.openConnection();
                httpCon.setDoOutput(true);
                httpCon.setUseCaches(false);
                httpCon.setFixedLengthStreamingMode(bytes.length);
                httpCon.setRequestMethod("POST");
                httpCon.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
                OutputStream out = httpCon.getOutputStream();
                out.write(bytes);
                out.close();
               // Log.d("ShareExternal","messge is "+body);
                int status = httpCon.getResponseCode();
                if (status == 200)
                {
                    result = "Message sent was "+body;
                }
                else
                {
                    result = "Post Failure." + " Status: " + status;
                }
            }
            finally
            {
                if (httpCon != null)
                {
                    httpCon.disconnect();
                }
            }

        }
        catch (IOException e)
        {
            result = "Post Failure. Error in sharing with App Server.";
            Log.e("AppUtil", "Error in sharing with App Server: " + e);
        }
        return result;
    }
}
