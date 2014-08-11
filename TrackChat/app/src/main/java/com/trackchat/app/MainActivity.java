package com.trackchat.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends Fragment {
    GoogleCloudMessaging gcm;
    Context context;
    String regId;

    public static final String REG_ID = "regId";
    private static final String APP_VERSION = "appVersion";
    EditText number,name;

    static final String TAG = "Register Activity";
    String user_number;
    String user_name;
    TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_main,
                container, false);
        number=(EditText)view.findViewById(R.id.us_number);
        name=(EditText)view.findViewById(R.id.chat_name);
        number.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
        tv=(TextView)view.findViewById(R.id.tv_reg);
        final SharedPreferences pref2s = view.getContext().getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String mPhoneNumber = pref2s.getString("Number","");
        String name_tx = pref2s.getString("Name","");
        if(mPhoneNumber!=null && name_tx!=null)
         tv.setText("Registered "+mPhoneNumber +"\n\t\t"+name_tx);

        Button button = (Button) view.findViewById(R.id.btn_register);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (TextUtils.isEmpty(regId) || number.getText().toString()!=null) {
                    Log.d("RegisterActivity", "Btn pressed");
                    user_number=number.getText().toString();
                    user_name=name.getText().toString();
                    regId = registerGCM();
                    tv.setText("Registered "+user_number +"\n\t\t"+user_name);

                    Log.d("RegisterActivity", "GCM RegId: " + regId);
                } else {
                    Log.d("RegisterActivity", "Already registered with same number");
                }
            }
        });
        return view;
    }


    public String registerGCM()
    {

        gcm = GoogleCloudMessaging.getInstance(this.getActivity());
        regId = getRegistrationId(context);

        if (TextUtils.isEmpty(regId)) {

            registerInBackground();

            Log.d("RegisterActivity",
                    "registerGCM - successfully registered with GCM server - regId: "
                            + regId);


        } else {
            Toast.makeText(this.getActivity(),
                    "Already Registered with GCM server " + regId,
                    Toast.LENGTH_LONG).show();
        }
        return regId;
    }

    private String getRegistrationId(Context context)
    {
        final SharedPreferences prefs = this.getActivity().getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty())
        {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = 1;//getAppVersion(context);
        if (registeredVersion != currentVersion)
        {
            Log.i(TAG, "App version changed.");
            return "";
        }
        //also check for phone number

        String mPhoneNumber =prefs.getString("Number","");
        if(!mPhoneNumber.equals(user_number))
        {
            Log.i(TAG, " changed. m"+ mPhoneNumber +"u " +user_number);
            Log.i(TAG, "user_number changed.");
            return "";
        }
        String name_ch =prefs.getString("Name","");
        if(!name_ch.equals(user_name))
            return "";
        Log.i(TAG, "nothing changed. m"+ mPhoneNumber +"u " +user_number);
        return registrationId;
    }

    private static int getAppVersion(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (Exception e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }

    }

    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {

                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(Config.GOOGLE_PROJECT_ID);
                    Log.d("RegisterActivity", "registerInBackground - regId: "
                            + regId);
                    msg = "Device registered, registration ID=" + regId;

                    storeRegistrationId(context, regId);
                }
                catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                    Log.d("RegisterActivity", "Error: " + msg);
                }
                Log.d("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
               // Toast.makeText(this.getActivity(),
                 //       "Registered with GCM Server." + msg, Toast.LENGTH_LONG)
                   //     .show();
            }
        }.execute(null, null, null);
    }
    private void storeRegistrationId(Context context, String regId)
    {
        final SharedPreferences prefs = this.getActivity().getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        int appVersion = 1;
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putString("Number",user_number);
        editor.putString("Name",user_name);
        editor.putInt(APP_VERSION, appVersion);
        editor.commit();

    }
/*********************To start server communication***************************************/
   /*
    public void start_server_comm_btn(View v)
    {
        regId = getRegistrationId(context);
        if (TextUtils.isEmpty(regId)) {
            Toast.makeText(this.getActivity(), "Kindly register first with 3rd party GCM SERVER",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            Intent i = new Intent(this.getActivity(), ServerTalker.class);
            i.putExtra("regId", regId);
            Log.d("RegisterActivity","onClick of Share: Before starting server activity.");
            startActivity(i);

        }
    }
    */


    /********Misc code below***********/


   }
