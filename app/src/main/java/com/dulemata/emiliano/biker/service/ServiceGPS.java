package com.dulemata.emiliano.biker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;

import com.dulemata.emiliano.biker.MainActivity;
import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.SavePercorsoActivity;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.util.Keys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Emiliano on 12/03/2017.
 */

public class ServiceGPS extends Service implements LocationListener {

    private Percorso aPercorso;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Thread thread;
    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (mLocationRequest == null) {
                mLocationRequest = new LocationRequest()
                        .setInterval(3000)
                        .setMaxWaitTime(5000)
                        //.setSmallestDisplacement(10)
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            }
            //noinspection MissingPermission
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ServiceGPS.this);
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            mGoogleApiClient.connect();
        }
    };
    private BroadcastReceiver receiver;
    private TrackingNotification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        aPercorso = new Percorso();
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
        if (aPercorso == null)
            aPercorso = new Percorso();
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Intent risposta;
                    switch (action) {
                        case Keys.PERCORSO_PARZIALE_TRACKER:
                            risposta = new Intent(Keys.PERCORSO_PARZIALE_SERVICE);
                            risposta.putExtra(Keys.PERCORSO, aPercorso);
                            sendBroadcast(risposta);
                            break;
                        case Keys.SHOW_NOTIFICA:
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notification = new TrackingNotification(notificationManager, getApplicationContext());
                            break;
                        case Keys.REMOVE_NOTIFICA:
                            if (notification != null)
                                notification.remove();
                            break;
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Keys.PERCORSO_PARZIALE_TRACKER);
            filter.addAction(Keys.SHOW_NOTIFICA);
            filter.addAction(Keys.REMOVE_NOTIFICA);
            registerReceiver(receiver, filter);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        sendPercorso();
        unregisterReceiver(receiver);
        if (notification != null)
            notification.remove();
        aPercorso = null;
    }

    private void sendPercorso() {
        if (aPercorso != null && aPercorso.size() > 0) {
            Intent intent = new Intent(Keys.PERCORSO_COMPLETO_SERVICE);
            intent.putExtra(Keys.PERCORSO_COMPLETO, aPercorso);
            sendBroadcast(intent);
        }
    }

    private Location oldPos;

    @Override
    public void onLocationChanged(Location location) {
        if (oldPos == null) {
            sendLocation(location);
        } else if (oldPos.getLatitude() != location.getLatitude() || oldPos.getLongitude() != location.getLongitude()) {
            sendLocation(location);
        }
        oldPos = location;
    }

    public void sendLocation(Location location) {
        Posizione posizione = new Posizione(location);
        aPercorso.addPosizione(posizione);
        int puntiGuadagnati = aPercorso.puntiGuadagnati;
        double distanza = aPercorso.distanzaTotale;
        Intent intent = new Intent(Keys.POSIZIONE_GPS);
        intent.putExtra(Keys.POSIZIONE, posizione);
        intent.putExtra(Keys.PUNTI_GUADAGNATI, puntiGuadagnati);
        intent.putExtra(Keys.DISTANZA_PARZIALE, distanza);
        sendBroadcast(intent);
    }

    private class TrackingNotification {

        private static final int NOTIFICA_TRACKING = 23;
        private NotificationManager notificationManager;
        private Notification notification;

        TrackingNotification(NotificationManager nM, Context applicationContext) {
            notificationManager = nM;
            Intent riprendiActivity = new Intent(getApplication(), MainActivity.class);
            riprendiActivity.putExtra(Keys.PERCORSO, aPercorso);
            riprendiActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent tapNotification = PendingIntent.getActivity(applicationContext, 0, riprendiActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            Intent endTracking = new Intent(getApplicationContext(), SavePercorsoActivity.class);
            endTracking.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pulsanteNotifica = PendingIntent.getActivity(applicationContext, 0, endTracking, PendingIntent.FLAG_CANCEL_CURRENT);
            notification = new NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setContentIntent(tapNotification)
                    .addAction(R.drawable.ic_menu_tracker, getString(R.string.end_tracking), pulsanteNotifica)
                    .setContentTitle(getString(R.string.notification_message))
                    .build();
            notificationManager.notify(NOTIFICA_TRACKING, notification);
        }

        void remove() {
            notificationManager.cancel(NOTIFICA_TRACKING);
        }
    }

}
