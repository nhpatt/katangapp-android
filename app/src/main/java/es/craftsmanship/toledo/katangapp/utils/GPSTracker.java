package es.craftsmanship.toledo.katangapp.utils;

import android.app.Service;

import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;

import android.os.Bundle;
import android.os.IBinder;

import android.support.annotation.Nullable;

/**
 * @author Cristóbal Hermida
 */
public class GPSTracker extends Service implements LocationListener {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}