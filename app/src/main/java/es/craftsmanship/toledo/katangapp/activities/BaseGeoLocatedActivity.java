/**
 *    Copyright 2016-today Software Craftmanship Toledo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.craftsmanship.toledo.katangapp.activities;

import android.app.Dialog;

import android.content.Intent;

import android.location.Location;

import android.os.Bundle;

import android.support.annotation.NonNull;

import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * @author Manuel de la Peña
 */
public abstract class BaseGeoLocatedActivity extends BaseAndroidBusRegistrableActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final LocationRequest GPS_REQUEST = LocationRequest.create()
        .setInterval(10000)
        .setFastestInterval(3000)
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
    }

    private void initializeGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int checkGooglePlayServices = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
            /*
            * google play services is missing or update is required
            *  return code could be
            * SUCCESS,
            * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
            * SERVICE_DISABLED, SERVICE_INVALID.
            */
            Dialog errorDialog = googleApiAvailability.getErrorDialog(this,
                checkGooglePlayServices, REQUEST_CODE_RECOVER_PLAY_SERVICES);

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