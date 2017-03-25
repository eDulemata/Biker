package com.dulemata.emiliano.biker.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dulemata.emiliano.biker.MainActivity;
import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.SavePercorsoActivity;
import com.dulemata.emiliano.biker.TrackerProperty;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.service.ServiceGPS;
import com.dulemata.emiliano.biker.service.ServiceNetwork;
import com.dulemata.emiliano.biker.util.Keys;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;

import java.lang.ref.WeakReference;

import static com.dulemata.emiliano.biker.util.Dialog.showAlert;

public class TrackerFragment extends Fragment implements FragmentInt, OnMapReadyCallback, AsyncResponse {

    private static final int SALVATAGGIO_UTENTE = 23;
    public static final int ZOOM = 18;
    public static final int TILT = 55;
    private GoogleMap mGoogleMap;
    public static boolean isTracking;
    private WeakReference<MainActivity> reference;
    private AlertDialog dialog;
    private TrackerProperty velocità, altitudine, distanza, punti;
    private PolylineOptions options;
    private Button tracking_button;
    private BroadcastReceiver receiver;
    private Intent intentGps, intentNetwork, getPercorso;
    private LatLng oldPos;
    private MarkerOptions mMarkerOption;

    public TrackerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null)
            oldPos = savedInstanceState.getParcelable(Keys.OLD_POS);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (oldPos != null) {
            outState.putParcelable(Keys.OLD_POS, oldPos);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (intentGps == null)
            intentGps = new Intent(getActivity(), ServiceGPS.class);
        if (intentNetwork == null)
            intentNetwork = new Intent(getActivity(), ServiceNetwork.class);
        getActivity().startService(intentNetwork);
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Percorso percorso;
                    Posizione posizione;
                    switch (action) {
                        case Keys.PERCORSO_PARZIALE_SERVICE:
                            percorso = intent.getParcelableExtra(Keys.PERCORSO);
                            drawPercorso(percorso);
                            isTracking = true;
                            setButton();
                            rimuoviNotifica();
                            getActivity().stopService(intentNetwork);
                            break;
                        case Keys.POSIZIONE_NETWORK:
                            posizione = intent.getParcelableExtra(Keys.POSIZIONE);
                            LatLng latLng = new LatLng(posizione.latitude, posizione.longitude);
                            setMarker(latLng, muoviCamera(latLng));
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
                            break;
                        case Keys.PERCORSO_COMPLETO_SERVICE:
                            percorso = intent.getParcelableExtra(Keys.PERCORSO_COMPLETO);
                            startSavePercorso(percorso);
                            break;
                        case Keys.POSIZIONE_GPS:
                            setOptions();
                            posizione = intent.getParcelableExtra(Keys.POSIZIONE);
                            int punti = intent.getIntExtra(Keys.PUNTI_GUADAGNATI, -1);
                            double distanza = intent.getDoubleExtra(Keys.DISTANZA_PARZIALE, -1);
                            aggiornaTracking(posizione, distanza, punti);
                            break;
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Keys.PERCORSO_PARZIALE_SERVICE);
        filter.addAction(Keys.POSIZIONE_NETWORK);
        filter.addAction(Keys.PERCORSO_COMPLETO_SERVICE);
        filter.addAction(Keys.POSIZIONE_GPS);
        getActivity().registerReceiver(receiver, filter);
        if (getPercorso == null)
            getPercorso = new Intent(Keys.PERCORSO_PARZIALE_TRACKER);
        getActivity().sendBroadcast(getPercorso);
    }

    private void setMarker(LatLng posizione, float v) {
        mGoogleMap.clear();
        int icon_dimen = getView().findViewById(R.id.map).getWidth() / 10;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_icon);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, icon_dimen, icon_dimen, false);
        Paint paint = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(Keys.VERDE, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(resized);
        canvas.drawBitmap(resized, 0, 0, paint);
        mMarkerOption = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resized))
                .position(posizione)
                .draggable(false)
                .rotation(-45 + v)
                .flat(true)
                .anchor(0.5f, 0.5f);
        mGoogleMap.addMarker(mMarkerOption);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        reference = new WeakReference<>((MainActivity) context);
    }

    private void rimuoviNotifica() {
        Intent intent = new Intent(Keys.REMOVE_NOTIFICA);
        getActivity().sendBroadcast(intent);
    }

    private void aggiornaTracking(Posizione posizione, double distanza, int punti) {
        velocità.update(posizione.velocitàIstantanea);
        this.distanza.update(distanza);
        this.punti.update(punti);
        altitudine.update(posizione.altitudine);
        addPosizione(posizione);
    }

    private void startSavePercorso(Percorso percorso) {
        velocità.update(0);
        altitudine.update(0);
        punti.update(0);
        distanza.update(0);
        Intent intent = new Intent(getActivity(), SavePercorsoActivity.class);
        intent.putExtra(Keys.PERCORSO, percorso);
        startActivityForResult(intent, SALVATAGGIO_UTENTE);
    }

    private float muoviCamera(LatLng posizione) {
        float angleF = 0;
        CameraPosition cameraPosition;
        if (oldPos != null) {
            Double angle = Math.atan2((posizione.longitude - oldPos.longitude), posizione.latitude - oldPos.latitude);
            angleF = (float) Math.toDegrees(angle);
            cameraPosition = CameraPosition.builder().target(posizione).bearing(angleF).tilt(TILT).zoom(ZOOM).build();
        } else {
            cameraPosition = CameraPosition.builder().target(posizione).tilt(TILT).zoom(ZOOM).build();
            oldPos = posizione;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mGoogleMap.animateCamera(cameraUpdate);
        return angleF;
    }

    private void setOptions() {
        if (options == null)
            options = new PolylineOptions().color(Keys.VERDE).width(15).geodesic(true);
    }

    private void addPosizione(Posizione posizione) {
        LatLng latLng = new LatLng(posizione.latitude, posizione.longitude);
        setMarker(latLng, muoviCamera(latLng));
        options.add(latLng);
        mGoogleMap.addPolyline(options);
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

    private void drawPercorso(Percorso percorso) {
        mGoogleMap.clear();
        setOptions();
        for (Object elem : percorso) {
            addPosizione((Posizione) elem);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
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
        tracking_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking) {
                    isTracking = false;
                    stopTracking();
                    setButton();
                    mGoogleMap.clear();
                    options = null;
                } else {
                    isTracking = true;
                    startTracking();
                    setButton();
                }
            }

        });
        setButton();
        return v;
    }

    private void stopTracking() {
        getActivity().stopService(intentGps);
    }

    private void startTracking() {
        getActivity().stopService(intentNetwork);
        getActivity().startService(intentGps);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    @Override
    public void processResult(JSONArray result) {
        if (result != null && result.length() == 1) {
            dialog = showAlert(getActivity(), dialog, "Salvataggio effettauto", "Il suo percorso è stato salvato", true).create();
            dialog.show();
        } else {
            dialog = showAlert(getActivity(), dialog, "Errore salvataggio", "C'è stato un errore durante il salvataggio del percorso", true).create();
            dialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopService(intentNetwork);
    }
}
