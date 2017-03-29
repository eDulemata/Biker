package com.dulemata.emiliano.biker.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.activity.MainActivity;
import com.dulemata.emiliano.biker.activity.SavePercorsoActivity;
import com.dulemata.emiliano.biker.data.FuoriPercorsoException;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.service.ServiceLocation;
import com.dulemata.emiliano.biker.util.Keys;
import com.dulemata.emiliano.biker.views.TrackerProperty;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.ref.WeakReference;

public class TrackerFragment extends FragmentBiker implements OnMapReadyCallback {

    private static boolean isTracking;
    private static final int ZOOM_GPS = 17;
    private static final int TILT_GPS = 60;
    private static final int ZOOM_NETWORK = 18;
    private static final int TILT_NETWORK = 0;
    private GoogleMap mGoogleMap;
    private boolean firstFix = true;
    private Posizione posizioneGPS;
    private PolylineOptions polylineOptions;
    private WeakReference<MainActivity> reference;
    private TrackerProperty velocità, altitudine, distanza, punti;
    private Button tracking_button;
    private View.OnClickListener button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isTracking) {
                Intent intent = new Intent(Keys.PERCORSO_COMPLETO_SERVICE);
                reference.get().sendBroadcast(intent);
                startNetwork();
            } else {
                if (checkGPS())
                    startGps();
            }
            setButton();
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Posizione posizione;
            Percorso percorso;
            Float bearing;
            String action = intent.getAction();
            switch (action) {
                case Keys.POSIZIONE_NETWORK:
                    posizione = intent.getParcelableExtra(Keys.POSIZIONE);
                    bearing = intent.getFloatExtra(Keys.BEARING, 0);
                    posizioneNework(posizione, bearing);
                    break;
                case Keys.POSIZIONE_GPS:
                    posizioneGPS = intent.getParcelableExtra(Keys.POSIZIONE);
                    bearing = intent.getFloatExtra(Keys.BEARING, 0);
                    percorsoGPS(bearing);
                    break;
                case Keys.PERCORSO_COMPLETO:
                    if ((percorso = intent.getParcelableExtra(Keys.PERCORSO)) != null)
                        savePercorso(percorso);
                    else {
                        alertDialog = setAlert("PERCORSO", "Percorso troppo corto. Non verrà salvato", false)
                                .setPositiveButton(android.R.string.ok, null).create();
                        alertDialog.show();
                    }
                    mGoogleMap.clear();
                    isTracking = false;
                    break;
                case Keys.PERCORSO_PARZIALE:
                    if ((percorso = intent.getParcelableExtra(Keys.PERCORSO)) != null)
                        redrawLine(percorso);
                    break;
            }
        }

    };

    private void savePercorso(Percorso percorso) {
        Intent intent = new Intent(reference.get(), SavePercorsoActivity.class);
        intent.putExtra(Keys.PERCORSO, percorso);
        startActivity(intent);
    }

    private void redrawLine(Percorso percorso) {
        Posizione posizione = null;
        try {
            for (int i = 0; i < percorso.size(); i++) {
                posizione = percorso.getPosizione(i);
                polylineOptions.add(new LatLng(posizione.latitude, posizione.longitude));
            }
        } catch (FuoriPercorsoException e) {
            e.printStackTrace();
        }
        if (posizione != null)
            getMarker(new LatLng(posizione.latitude, posizione.longitude), 0);
        mGoogleMap.addPolyline(polylineOptions);
    }

    private void percorsoGPS(Float bearing) {
        mGoogleMap.clear();
        LatLng latLng = new LatLng(posizioneGPS.latitude, posizioneGPS.longitude);
        MarkerOptions markerOptions = getMarker(latLng, bearing);
        if (polylineOptions == null) {
            polylineOptions = new PolylineOptions()
                    .color(Keys.VERDE)
                    .width(Keys.SPESSORE_LINEA);
        }
        polylineOptions.add(latLng);
        mGoogleMap.addPolyline(polylineOptions);
        mGoogleMap.addMarker(markerOptions);
        cameraGPS(latLng, bearing);
    }

    private void cameraGPS(LatLng latLng, Float bearing) {
        CameraPosition cameraPosition = new CameraPosition(
                latLng,
                ZOOM_GPS,
                TILT_GPS,
                bearing);
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private MarkerOptions getMarker(LatLng posizione, float direction) {
        int icon_dimen = getView().findViewById(R.id.map).getWidth() / 10;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_icon);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, icon_dimen, icon_dimen, false);
        Paint paint = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(Keys.VERDE, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(resized);
        canvas.drawBitmap(resized, 0, 0, paint);
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resized))
                .position(posizione)
                .draggable(false)
                .rotation(-45 + direction)
                .flat(true)
                .anchor(0.5f, 0.5f);
    }

    private void posizioneNework(Posizione posizione, Float bearing) {
        mGoogleMap.clear();
        LatLng latLng = new LatLng(posizione.latitude, posizione.longitude);
        MarkerOptions markerOptions = getMarker(latLng, bearing);
        mGoogleMap.addMarker(markerOptions);
        CameraPosition cameraPosition = new CameraPosition(
                latLng,
                ZOOM_NETWORK,
                TILT_NETWORK,
                bearing);
        if (firstFix) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            firstFix = false;
        } else
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void startGps() {
        Intent intent = new Intent(reference.get(), ServiceLocation.class);
        intent.setAction(Keys.LOCATION_GPS);
        reference.get().startService(intent);
        isTracking = true;
    }

    private void startNetwork() {
        Intent intent = new Intent(reference.get(), ServiceLocation.class);
        intent.setAction(Keys.LOCATION_NETWORK);
        reference.get().startService(intent);
        isTracking = false;
    }

    private boolean checkGPS() {
        final LocationManager manager = (LocationManager) reference.get().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertDialog = setAlert("PRECISIONE MAGGIORE", "Attivando il GPS la qualità del tracking sarà maggiore", false)
                    .setPositiveButton("attiva", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent gpsOptionsIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsOptionsIntent);
                            startGps();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("continua", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startGps();
                            dialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    public TrackerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        reference = new WeakReference<>((MainActivity) context);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Keys.POSIZIONE_NETWORK);
        filter.addAction(Keys.POSIZIONE_GPS);
        filter.addAction(Keys.PERCORSO_COMPLETO);
        filter.addAction(Keys.PERCORSO_PARZIALE);
        reference.get().registerReceiver(receiver, filter);
        Intent intent = new Intent(Keys.PERCORSO_PARZIALE_SERVICE);
        reference.get().sendBroadcast(intent);
    }

    private void setButton() {
        if (isTracking) {
            tracking_button.setBackgroundColor(Keys.ROSSO);
            tracking_button.setText(getString(R.string.end_tracking));
        } else {
            tracking_button.setBackgroundColor(Keys.VERDE);
            tracking_button.setText(getString(R.string.start_tracking));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        reference.get().unregisterReceiver(receiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tracker, container, false);
        ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        velocità = new TrackerProperty(v.findViewById(R.id.velocità), TrackerProperty.Proprietà.velocità, 0);
        distanza = new TrackerProperty(v.findViewById(R.id.distanza), TrackerProperty.Proprietà.distanza, 0);
        altitudine = new TrackerProperty(v.findViewById(R.id.altitudine), TrackerProperty.Proprietà.altitudine, 0);
        punti = new TrackerProperty(v.findViewById(R.id.punti), TrackerProperty.Proprietà.punti, 0);
        tracking_button = (Button) v.findViewById(R.id.button_tracking);
        tracking_button.setOnClickListener(button_listener);
        setButton();
        if (!isTracking)
            startNetwork();
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

}
