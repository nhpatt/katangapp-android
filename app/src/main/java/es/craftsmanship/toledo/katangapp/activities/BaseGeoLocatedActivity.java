package es.craftsmanship.toledo.katangapp.activities;

import es.craftsmanship.toledo.katangapp.subscribers.BusStopsSubscriber;
import es.craftsmanship.toledo.katangapp.utils.AndroidBus;

import android.app.Dialog;

import android.content.Intent;

import android.location.Location;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * @author Manuel de la Peña
 */
public abstract class BaseGeoLocatedActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, BusStopsSubscriber {

    private static final LocationRequest GPS_REQUEST = LocationRequest.create()
        .setInterval(3000)
        .setFastestInterval(16)
        .setNumUpdates(3)
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;

    private GoogleApiClient googleApiClient;
    private Double latitude;
    private Double longitude;

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            longitude = lastLocation.getLongitude();
            latitude = lastLocation.getLatitude();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient, GPS_REQUEST, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_RECOVER_PLAY_SERVICES) {
            return;
        }

        if (resultCode == RESULT_OK) {
            if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                googleApiClient.connect();
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(
                this, "Google Play Services must be installed.", Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    protected Double getLatitude() {
        return latitude;
    }

    protected Double getLongitude() {
        return longitude;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGooglePlayServices();
    }

    @Override
    protected void onPause() {
        AndroidBus.getInstance().unregister(this);

        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        AndroidBus.getInstance().register(this);
    }

    private void initializeGooglePlayServices() {
        int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
            /*
            * google play services is missing or update is required
            *  return code could be
            * SUCCESS,
            * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
            * SERVICE_DISABLED, SERVICE_INVALID.
            */
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                checkGooglePlayServices, this, REQUEST_CODE_RECOVER_PLAY_SERVICES);

            errorDialog.show();

            return;
        }

        googleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

}