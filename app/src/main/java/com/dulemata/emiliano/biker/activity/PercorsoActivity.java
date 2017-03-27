package com.dulemata.emiliano.biker.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.widget.TextView;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.views.TrackerProperty;
import com.dulemata.emiliano.biker.data.FuoriPercorsoException;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.util.Keys;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Emiliano on 16/03/2017.
 */

public class PercorsoActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView data, oraInizio, oraFine;
    TrackerProperty trackerProperty1, trackerProperty2, trackerProperty3, trackerProperty4;
    Percorso aPercorso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percorso);
        SupportMapFragment mappaPercorso = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mappaPercorso != null)
            mappaPercorso.getMapAsync(this);
        aPercorso = getIntent().getParcelableExtra(Keys.PERCORSO);
        data = (TextView) findViewById(R.id.data);
        oraInizio = (TextView) findViewById(R.id.ora_inizio);
        oraFine = (TextView) findViewById(R.id.ora_fine);
        try {
            Posizione inizio, fine;
            inizio = aPercorso.getPosizione(0);
            fine = aPercorso.getPosizione(aPercorso.size() - 1);
            data.setText(inizio.getDataString());
            oraInizio.setText(inizio.getOraString());
            oraFine.setText(fine.getOraString());
        } catch (FuoriPercorsoException e) {
            e.printStackTrace();
        }
        trackerProperty1 = new TrackerProperty(findViewById(R.id.track_1), TrackerProperty.Proprietà.velocitàMedia, aPercorso.velocitàMedia);
        trackerProperty2 = new TrackerProperty(findViewById(R.id.track_2), TrackerProperty.Proprietà.altituidineMedia, aPercorso.altitudineMedia);
        trackerProperty3 = new TrackerProperty(findViewById(R.id.track_3), TrackerProperty.Proprietà.puntiGuadagnati, aPercorso.puntiGuadagnati);
        trackerProperty4 = new TrackerProperty(findViewById(R.id.track_4), TrackerProperty.Proprietà.distanzaTotale, aPercorso.distanzaTotale);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        PolylineOptions options = new PolylineOptions().color(Keys.VERDE).width(Keys.SPESSORE_LINEA/3);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Object posizione : aPercorso) {
            LatLng ll = new LatLng(((Posizione) posizione).latitude, ((Posizione) posizione).longitude);
            builder.include(ll);
            options.add(ll);
        }
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        LatLngBounds bounds = builder.build();
        googleMap.addPolyline(options);
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 15));
    }
}
