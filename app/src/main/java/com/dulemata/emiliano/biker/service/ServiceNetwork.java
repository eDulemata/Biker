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

public class ServiceNetwork extends Service implements LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Thread thread;
    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (mLocationRequest == null) {
                mLocationRequest = new LocationRequest()
                        .setInterval(3000)
                        //.setSmallestDisplacement(10)
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            }
            //noinspection MissingPermission
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ServiceNetwork.this);
        }

        @Override
        public void onConnectionSuspended(int i) {

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
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (thread == null) {
            thread = new Thread() {
                @Override
                public void run() {
                    Posizione posizione = new Posizione(location);
                    Intent intent = new Intent(Keys.POSIZIONE_NETWORK);
                    intent.putExtra(Keys.POSIZIONE, posizione);
                    sendBroadcast(intent);
                }
            };
        }
        thread.run();
    }

}
