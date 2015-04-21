package com.parkingapp.sample;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parkingapp.connection.SFParkHandler;
import com.parkingapp.exception.ParkingAppException;

/**
 * App api key and Google play services version are mentioned in the manifest file.
 * Permissions to access the internet, access_network_state and write_external_storage added to manifest file in order to use
 * android api. Also MyLocation layer has been added, so permissions to add coarse and fine location are also added in the
 * manifest file.
 * GoogleApiClient is an interface to Google Play Services. ConnectionCallbacks and OnConnectionFailedListener are
 * listeners to receive connection events and connection failures respectively
 */
public class MapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient myGoogleApiClient;
    protected Location myCurrentLocation;
    private double myLatitude;
    private double myLongitude;
    private static final int zoomAtStart = 15;
    private static final String SFParkRadius = "0.25";

    /**
     * Adds a map based on the fragment in the xml file.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        buildGoogleApiClient();
    }

    /**
     * Builds the myGoogleApiClient object and requests the Location Services API
     */
    protected synchronized void buildGoogleApiClient() {
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        myGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myGoogleApiClient.isConnected()) {
            myGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs after myGoogleApiClient successfully connects using connect().
     * FusedLocationApi provides a simple API to request the last known location from the getLastLocation method.
     * Precise location can be accessed due to the ACCESS_FINE_LOCATION setting in the manifest file
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        myCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(myGoogleApiClient);
        if (myCurrentLocation != null) {
            myLatitude = myCurrentLocation.getLatitude();
            myLongitude = myCurrentLocation.getLongitude();
            setUpMapIfNeeded();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    /**
     * Runs if myGoogleApiClient is temporarily disconnected
     */
    @Override
    public void onConnectionSuspended(int cause) {
        myGoogleApiClient.connect();
    }

    /**
     * SupportMapFragment is a wrapper around view of a map. Fragment mentioned in the XML file is added by findFragmentById()
     * setMyLocationEnabled enables the MyLocation layer on the map.
     * The addMarker() method adds a marker at the user's current location and the animateCamera() method moves the camera
     * to display the user's location at the center.
     */
    private void setUpMapIfNeeded() {
        GoogleMap myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        if (myMap != null) {
            myMap.setMyLocationEnabled(true);
            myMap.addMarker(new MarkerOptions().position(new LatLng(myLatitude, myLongitude)).title("CurrentLocation"));
            LatLng position = new LatLng(myLatitude, myLongitude);
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoomAtStart));

            TextView t = (TextView) findViewById(R.id.apimsgTextView);
            SFParkHandler sfParkHandler = new SFParkHandler();
            String latitude = String.valueOf(myLatitude);
            String longitude = String.valueOf(myLongitude);
            StringBuilder response = null;

            try {
                response = sfParkHandler.callAvailabilityService(latitude, longitude, SFParkRadius);
            } catch (ParkingAppException e) {
                e.printStackTrace();
            }
            if (response != null) {
                t.setText(response);
            }
        }
    }

}
