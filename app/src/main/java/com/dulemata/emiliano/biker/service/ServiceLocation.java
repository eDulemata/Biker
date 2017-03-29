package com.dulemata.emiliano.biker.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.util.Keys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Emiliano on 28/03/2017.
 */

public class ServiceLocation extends Service implements LocationListener {

    private static final long INTERVAL_GPS = 3*1000;
    private static final long INTERVAL_NETWORK = 10*1000;
    private static final float DISPLACEMENT_GPS = 50;
    private LocationRequest mLocationRequest;
    private Percorso percorso;
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient.OnConnectionFailedListener mOnConnectionFailed = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            mGoogleApiClient.connect();
        }
    };
    private GoogleApiClient.ConnectionCallbacks mOnConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            switch (mAction) {
                case Keys.LOCATION_NETWORK:
                    startNetwork();
                    break;
                case Keys.LOCATION_GPS:
                    startGPS();
                    break;
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
        }
    };
    private String mAction;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                String action = intent.getAction();
                switch (action) {
                    case Keys.PERCORSO_PARZIALE_SERVICE:
                        if(!mAction.equals(Keys.LOCATION_NETWORK))
                        sendPercorso(Keys.PERCORSO_PARZIALE);
                        break;
                    case Keys.PERCORSO_COMPLETO_SERVICE:
                        sendPercorso(Keys.PERCORSO_COMPLETO);
                        stopSelf();
                        break;
                }
            }
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
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(mOnConnectionFailed)
                    .addConnectionCallbacks(mOnConnectionCallback)
                    .build();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case Keys.LOCATION_NETWORK:
                    mAction = action;
                    if (!mGoogleApiClient.isConnected())
                        connectGoogleApi();
                    else
                        startNetwork();
                    break;
                case Keys.LOCATION_GPS:
                    mAction = action;
                    if (!mGoogleApiClient.isConnected())
                        connectGoogleApi();
                    else
                        startGPS();
                    break;
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Keys.PERCORSO_PARZIALE_SERVICE);
        filter.addAction(Keys.PERCORSO_COMPLETO_SERVICE);
        registerReceiver(receiver, filter);
        return START_NOT_STICKY;
    }

    private void startGPS() {
        percorso = new Percorso();
        removeUpdates();
        mLocationRequest = new LocationRequest()
                .setInterval(INTERVAL_GPS)
                .setSmallestDisplacement(DISPLACEMENT_GPS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        requestUpdates();
    }

    private void removeUpdates() {
        if (mLocationRequest != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void requestUpdates() {
        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void startNetwork() {
        if (percorso != null)
            percorso = null;
        removeUpdates();
        mLocationRequest = new LocationRequest()
                .setInterval(INTERVAL_NETWORK)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        requestUpdates();
    }

    private void connectGoogleApi() {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            switch (mAction) {
                case Keys.LOCATION_GPS:
                    sendPosition(location, Keys.POSIZIONE_GPS);
                    break;
                case Keys.LOCATION_NETWORK:
                    sendPosition(location, Keys.POSIZIONE_NETWORK);
                    break;
            }
        }
    }

    private void sendPosition(Location location, String intentAction) {
        Posizione posizione = new Posizione(location);
        if (intentAction.equals(Keys.POSIZIONE_GPS))
            percorso.addPosizione(posizione);
        Intent intent = new Intent(intentAction);
        intent.putExtra(Keys.POSIZIONE, posizione);
        intent.putExtra(Keys.BEARING,location.getBearing());
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void sendPercorso(String action) {
        Intent intent = new Intent(action);
        if (percorso != null && percorso.size() > 0)
            intent.putExtra(Keys.PERCORSO, percorso);
        sendBroadcast(intent);
    }
}
