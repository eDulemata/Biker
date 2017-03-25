package com.dulemata.emiliano.biker.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.util.Keys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Emiliano on 13/03/2017.
 */

public class ServiceNetwork extends Service {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location prev;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (prev == null) {
                prev = location;
                sendPosizione(location);
            } else if (calcolaDistanza(prev, location) > 20) {
                sendPosizione(location);
                prev = location;
            }
        }
    };

    public void sendPosizione(Location location) {
        Posizione posizione = new Posizione(location);
        Intent intent = new Intent(Keys.POSIZIONE_NETWORK);
        intent.putExtra(Keys.POSIZIONE, posizione);
        sendBroadcast(intent);
    }

    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (mLocationRequest == null) {
                mLocationRequest = new LocationRequest()
                        .setInterval(3000)
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setMaxWaitTime(5000);
            }
            //noinspection MissingPermission
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationListener);
        }

        @Override
        public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
        }
    };
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.i("seriveNetwork", "connessione fallita");
            mGoogleApiClient.connect();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
            mGoogleApiClient.disconnect();
        }
    }

    private double calcolaDistanza(Location prev, Location succ) {
        double distanza = 0;
        double lat1 = toRadiante(succ.getLatitude());
        double lat2 = toRadiante(prev.getLatitude());
        double lng1 = toRadiante(succ.getLongitude());
        double lng2 = toRadiante(prev.getLongitude());
        double fi = Math.abs(lng1 - lng2);
        double p = Math.acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2) * Math.cos(lat1) * Math.cos(fi));
        int RAGGIO_TERRA = 6371 * 1000;
        distanza = p * RAGGIO_TERRA;
        return distanza;
    }

    private double toRadiante(double grado) {
        return (grado * Math.PI) / 180;
    }

}
