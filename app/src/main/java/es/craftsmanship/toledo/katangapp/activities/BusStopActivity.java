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

import es.craftsmanship.toledo.katangapp.R;
import es.craftsmanship.toledo.katangapp.db.FavoriteDAO;
import es.craftsmanship.toledo.katangapp.db.model.Favorite;
import es.craftsmanship.toledo.katangapp.maps.GoogleMapsCameraHelper;
import es.craftsmanship.toledo.katangapp.models.BusStop;
import es.craftsmanship.toledo.katangapp.utils.ExtrasConstants;

import android.content.Intent;

import android.graphics.drawable.Drawable;

import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanorama.OnStreetViewPanoramaChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

/**
 * @author Manuel de la Peña
 */
public class BusStopActivity extends BaseGeoLocatedActivity
    implements OnMarkerDragListener, OnStreetViewPanoramaChangeListener {

    private static final String MARKER_POSITION_KEY = "MarkerPosition";

    private BusStop busStop;
    private LatLng busStopLatLng;
    private Marker currentPositionMarker;
    private boolean isFavorite;
    private SupportMapFragment mapFragment;
    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;
    private StreetViewPanorama streetViewPanorama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bus_stop);

        mapFragment =
            (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bus_stop_map);

        streetViewPanoramaFragment =
            (SupportStreetViewPanoramaFragment)
                getSupportFragmentManager().findFragmentById(R.id.street_view_panorama);

        Intent intent = getIntent();

        if (intent.hasExtra(ExtrasConstants.BUS_STOP) &&
            (intent.getSerializableExtra(ExtrasConstants.BUS_STOP) != null)) {

            busStop = (BusStop) intent.getSerializableExtra(ExtrasConstants.BUS_STOP);

            String title = busStop.getId() + " - " + busStop.getAddress();

            this.setTitle(title);

            busStopLatLng = new LatLng(busStop.getLatitude(), busStop.getLongitude());

            configureGoogleMaps(savedInstanceState);
            configureGoogleStreetView(savedInstanceState);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

            setSupportActionBar(toolbar);

            try (FavoriteDAO favoriteDAO = new FavoriteDAO(this)) {
                favoriteDAO.open();

                Favorite favorite = favoriteDAO.getFavorite(busStop.getId());

                isFavorite = (favorite != null);
            }

            final FloatingActionButton floatingActionButton =
                (FloatingActionButton) findViewById(R.id.fab);

            floatingActionButton.setImageDrawable(getFavoritedDrawable(isFavorite));

            floatingActionButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    String message = "Parada AÑADIDA a favoritas con éxito";

                    try (FavoriteDAO favoriteDAO = new FavoriteDAO(BusStopActivity.this)) {
                        favoriteDAO.open();

                        if (!isFavorite) {
                            favoriteDAO.createFavorite(busStop);
                        } else {
                            message = "Parada ELIMINADA de favoritas con éxito";

                            Favorite favorite = new Favorite(busStop);

                            favoriteDAO.deleteFavorite(favorite);
                        }

                        isFavorite = !isFavorite;

                        floatingActionButton.setImageDrawable(getFavoritedDrawable(isFavorite));

                        Snackbar.make(
                            view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }

            });

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        streetViewPanorama.setPosition(marker.getPosition(), 150);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
        if (location != null) {
            currentPositionMarker.setPosition(location.position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(MARKER_POSITION_KEY, currentPositionMarker.getPosition());
    }

    private void configureGoogleMaps(final Bundle savedInstanceState) {
        final LatLng markerPosition;

        if (savedInstanceState == null) {
            markerPosition = busStopLatLng;
        }
        else {
            markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap map) {
                map.setOnMarkerDragListener(BusStopActivity.this);

                currentPositionMarker = map.addMarker(
                    new MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                        .draggable(true));

                LatLng currentPosition = busStopLatLng;

                if (getLatitude() != null && getLongitude() != null) {
                    currentPosition = new LatLng(getLatitude(), getLongitude());
                }

                map.moveCamera(GoogleMapsCameraHelper.getCameraUpdate(currentPosition, 16));
            }

        });
    }

    private void configureGoogleStreetView(final Bundle savedInstanceState) {
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
            new OnStreetViewPanoramaReadyCallback() {

                @Override
                public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                    streetViewPanorama = panorama;

                    streetViewPanorama.setOnStreetViewPanoramaChangeListener(BusStopActivity.this);

                    // Only need to set the position once as the streetview fragment will maintain
                    // its state.

                    if (savedInstanceState == null) {
                        streetViewPanorama.setPosition(busStopLatLng);
                    }
                }

        });
    }

    private Drawable getFavoritedDrawable(boolean favorited) {
        if (favorited) {
            return ContextCompat.getDrawable(BusStopActivity.this, android.R.drawable.ic_delete);
        }

        return ContextCompat.getDrawable(BusStopActivity.this, R.drawable.ic_star_favorite_24dp);
    }

}