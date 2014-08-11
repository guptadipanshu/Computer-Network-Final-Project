package com.trackchat.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GoogleMapActivity extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,LocationListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    GoogleMap map;
    boolean mUpdatesRequested = false;
    boolean update_camera=true;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;

    /**
     * **Timer ****
     */
    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    String message="";
    String msg_lat="";
    String msg_long="";
    boolean flag=true;
    String last_lat,last_long;


    /***Contacts****/

    AutoCompleteTextView textView=null;
    private ArrayAdapter<String> adapter;
    // Store contacts values in these arraylist
    public static ArrayList<String> phoneValueArr = new ArrayList<String>();
    public static ArrayList<String> nameValueArr = new ArrayList<String>();

    EditText message_box;
    String toNumberValue="";
    String receiver_name="";
    String receiver_number=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("gOOGLE", "onClick of Share: Before starting server activity.");
        setContentView(R.layout.activity_google_map);

        message_box=(EditText)findViewById(R.id.id_search_EditText);
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);

        Log.d("GooggleMapActivity", "Flag "+flag +"lat "+msg_lat +" long "+msg_long +"message "+message);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mEditor = mPrefs.edit();
        mLocationClient = new LocationClient(this, this, this);
        if(map==null)
        {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        textView = (AutoCompleteTextView) findViewById(R.id.toNumber);
        //Create adapter
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                                            new ArrayList<String>());
        textView.setThreshold(1);

        //Set adapter to AutoCompleteTextView
        textView.setAdapter(adapter);
        textView.setOnItemSelectedListener(this);
        textView.setOnItemClickListener(this);

        // Read contact data and add data to ArrayAdapter
        // ArrayAdapter used by AutoCompleteTextView


        start_reading_contacts();

    }

    @Override
    public void onLocationChanged(Location location) {
       // String msg = "Updated Location: " +
          last_lat=      Double.toString(location.getLatitude()) ;
           last_long=     Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if(update_camera==true){
            final LatLng POS = new LatLng(location.getLatitude(),location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(POS)      // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            update_camera=false;
        }
    }


    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        // If already requested, start periodic updates
        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        } else
            Toast.makeText(this, "mupdates=false", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * ***************Address**************
     */
    public void getAddress(View v) {

        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, "No geocoder available", Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            // Start the background task
            (new GoogleMapActivity.GetAddressTask(this)).execute(currentLocation);
        }
    }

    private class GetAddressTask extends AsyncTask<Location, Void, String>
    {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected String doInBackground(Location... params)
        {
            Geocoder geocoder =
                    new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            Location loc = params[0];
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        Double.toString(loc.getLatitude()) +
                        " , " +
                        Double.toString(loc.getLongitude()) +
                        " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName()
                );
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
        }
        @Override
        protected void onPostExecute(String address) {

            Toast.makeText(getApplicationContext(), "address "+ address, Toast.LENGTH_LONG).show();
        }
    }


/***************************Misc functions include error boxes******/
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent intent)
{

    // Choose what to do based on the request code
    switch (requestCode)
    {

        // If the request code matches the code sent in onConnectionFailed
        case CONNECTION_FAILURE_RESOLUTION_REQUEST :

            switch (resultCode)
            {
                // If Google Play services resolved the problem
                case Activity.RESULT_OK:
                    break;

                // If any other result was returned by Google Play services
                default:
                    break;
            }
            // If any other request code was received
        default:
            break;
    }
}
    private void showErrorDialog(int errorCode)
    {

        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,this,CONNECTION_FAILURE_RESOLUTION_REQUEST);


        if (errorDialog != null) {


            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(),
                    "Please make sure you are connected to interned and gps is on");
        }
    }

    private boolean servicesConnected()
    {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Google Map", "play services found");

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(),
                        "Please make sure you are connected to interned and gps is on");
            }
            return false;
        }
    }



    public static class ErrorDialogFragment extends DialogFragment
    {

    // Global field to contain the error dialog
         private Dialog mDialog;
        public ErrorDialogFragment()
        {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog)
        {
            mDialog = dialog;
        }

    @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            return mDialog;
        }
    }

    public void send_btn_server(View v)
    {

        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString("regId", "");

        if (TextUtils.isEmpty(registrationId)) {
            Toast.makeText(this, "Kindly register first with 3rd party GCM SERVER",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            String userMessage=message_box.getText().toString();
           // Log.e("GoogleMap","rec number is before check"+receiver_number);
            if(receiver_number==null) {
                receiver_number = textView.getText().toString();
             //   Log.e("GoogleMap","rec number"+receiver_number);
            }
            boolean istrue=true;
            if(receiver_number.charAt(0)=='+')
            {
                for(int i=1;i<receiver_number.length();i++){
                    int num=receiver_number.charAt(i)-'0';
                    if(!(num>=0 && num<=9)){
                        Toast.makeText(this,"The number entere is invalid.Format is +xxxxx. with no spaces"
                                ,Toast.LENGTH_LONG).show();
                        istrue=false;
                    }

                }
                if(istrue)
                    start_sending(registrationId, last_lat, last_long, userMessage, receiver_number);
            }
                else
                Toast.makeText(this,"The number entered is invalid format is +xxxxx. with no spaces"
                ,Toast.LENGTH_LONG).show();

        }
    }
    public void start_sending(final String regId,final String lat,final String log,final String userMessage,
                              final String receiver_number)
    {
        final ShareExternalServer appUtil;
       AsyncTask<Void, Void, String> shareRegidTask;
        appUtil = new ShareExternalServer();
        final Context context = this;
        shareRegidTask = new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                Log.e("Googlemap","data is"+ receiver_number);
                String result = appUtil.shareRegIdWithAppServer(context, regId,lat,log,userMessage,receiver_number);
                return result;
            }

            @Override
            protected void onPostExecute(String result)
            {
                Toast.makeText(getApplicationContext(), result,
                        Toast.LENGTH_LONG).show();

            }

        };
        shareRegidTask.execute(null, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.google_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /******************Start stop pause resume**********/
    @Override
    protected void onStart()
    {
        super.onStart();
        mLocationClient.connect();
    }
    @Override
    protected void onStop()
    {
        if (mLocationClient.isConnected())
        {
            mLocationClient.removeLocationUpdates(this);
        }
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
    @Override
    protected void onResume()
    {
        if (mPrefs.contains("KEY_UPDATES_ON"))
        {
            mUpdatesRequested =mPrefs.getBoolean("KEY_UPDATES_ON", false);
            mUpdatesRequested=true;
        }
        else
        {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
            mUpdatesRequested=false;
        }
        Log.e("GoogleMap","Onresume");
        check_notification();
        super.onResume();
    }
/*****Display notification****/
    public void check_notification()
    {
        message = getIntent().getStringExtra("message");
        msg_lat = getIntent().getStringExtra("lat");
        msg_long = getIntent().getStringExtra("long");
        String from=getIntent().getStringExtra("name");
        Log.e("GoogleMap","message"+message+msg_lat+msg_long);
        if (msg_lat != null && msg_long != null)
            flag = true;
        else
            flag = false;
        if(flag==true)
        {
            final LatLng POS = new LatLng(Double.parseDouble(msg_lat),
                    Double.parseDouble(msg_long));
            if(map!=null) {
                Marker perth = map.addMarker(new MarkerOptions()
                        .position(POS)
                        .title(from)
                        .snippet(message)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
            else
                Toast.makeText(this,"Cannot load map",Toast.LENGTH_LONG).show();
            flag=false;
            msg_long=null;
            msg_lat=null;
        }
    }
    /*********************************To read Contacts*****************************************/

  public void start_reading_contacts(){
      AsyncTask<Void, Void, String> get_contacts;
      get_contacts = new AsyncTask<Void, Void, String>()
      {
          @Override
          protected String doInBackground(Void... params)
          {

              readContacts();
              return "";
          }

          @Override
          protected void onPostExecute(String result)
          {
              Log.i("readcontacts","background read over");

          }

      };
      get_contacts.execute(null, null, null);
  }
    public void readContacts()
    {

        try
        {

            /*********** Reading Contacts Name And Number **********/

            String phoneNumber = "";
            ContentResolver cr = getBaseContext().getContentResolver();

            //Query to get contact name

            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            // If data data found in contacts
            if (cur.getCount() > 0)
            {

                Log.i("AutocompleteContacts", "Reading   contacts........");

                int k=0;
                String name = "";

                while (cur.moveToNext())
                {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                   // Log.e("READ", "Name "+name+ " ");
                    //Check contact have phone number
                    if (Integer.parseInt(cur
                            .getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                    {

                        //Create query to get phone number by contact id
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                        + " = ?",
                                new String[] { id },
                                null);
                        int j=0;

                        while (pCur.moveToNext())
                        {
                            // Sometimes get multiple data
                            if(j==0)
                            {
                                // Get Phone number
                                phoneNumber =""+pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                // Add contacts names to adapter
                              //  Log.e("READ", "no "+phoneNumber+ " ");
                                adapter.add(name);
                                // Add ArrayList names to adapter
                                phoneValueArr.add(phoneNumber.toString());
                                nameValueArr.add(name.toString());
                                j++;
                                k++;
                            }
                        }  // End while loop
                        pCur.close();
                       // Log.d("read","phone number found");
                    }// End if
                    else {
                        // Log.d("read","no phone number");
                    }
                }  // End while loop

            } // End Cursor value check
            cur.close();
        }
        catch (Exception e)
        {
            Log.i("AutocompleteContacts","Exception : "+ e);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
                               long arg3) {
        // TODO Auto-generated method stub
        //Log.d("AutocompleteContacts", "onItemSelected() position " + position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

        InputMethodManager imm = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub

        // Get Array index value for selected name
        int i = nameValueArr.indexOf(""+arg0.getItemAtPosition(arg2));

        // If name exist in name ArrayList
        if (i >= 0) {

            // Get Phone Number
            toNumberValue = phoneValueArr.get(i);

            InputMethodManager imm = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            // Show Alert
            /*Toast.makeText(getBaseContext(),
                    "Position:"+arg2+" Name:"+arg0.getItemAtPosition(arg2)+" Number:"+toNumberValue,
                    Toast.LENGTH_LONG).show();
             */
            receiver_name= (String) arg0.getItemAtPosition(arg2);
            receiver_number=toNumberValue;


            Log.d("AutocompleteContacts","Position:"+arg2+" Name:"+arg0.getItemAtPosition(arg2)+
                    " Number:"+toNumberValue);

        }

    }



}
