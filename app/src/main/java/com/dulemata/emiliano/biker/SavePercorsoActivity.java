package com.dulemata.emiliano.biker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequest;
import com.dulemata.emiliano.biker.data.FuoriPercorsoException;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.data.Utente;
import com.dulemata.emiliano.biker.util.Keys;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

public class SavePercorsoActivity extends AppCompatActivity implements AsyncResponse, OnMapReadyCallback {

    private static final String SALVA_PERCORSO = "salva_percorso.php";
    Utente utente;
    private int punteggioPercorso;
    TextView data, oraInizio, oraFine;
    TrackerProperty trackerProperty1, trackerProperty2, trackerProperty3, trackerProperty4;
    Button save, discard;
    Percorso aPercorso;
    private GoogleMap mGoogleMap;
    private SupportMapFragment mappaPercorso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_percorso);
        mappaPercorso = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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
        save = (Button) findViewById(R.id.save);
        discard = (Button) findViewById(R.id.cancel);
        utente = getIntent().getParcelableExtra(Keys.UTENTE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePercorso();
            }
        });
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void savePercorso() {
        BackgroundHTTPRequest request = new BackgroundHTTPRequest(this);
        utente = getIntent().getParcelableExtra(Keys.UTENTE);
        int numUtente = utente.idUtente;
        int numPercorso = utente.percorsiUtente;
        utente.punteggioUtente = utente.punteggioUtente + punteggioPercorso;
        try {
            String json = "{\"posizioni\":" + aPercorso.toJsonArray().toString() + "}";
            json = json.trim();
            String url = Keys.URL_SERVER + SALVA_PERCORSO +
                    "?id_percorso=" + numPercorso +
                    "&id_utente=" + numUtente +
                    "&distanza_totale=" + aPercorso.distanzaTotale +
                    "&velocita_media=" + aPercorso.velocitàMedia +
                    "&altitudine_media=" + aPercorso.altitudineMedia +
                    "&punti_guadagnati=" + punteggioPercorso +
                    "&json=" + json;
            request.execute(url, Keys.JSON_RESULT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResult(JSONArray result) {
        Intent data = new Intent();
        if (result != null && result.length() == 1) {
            utente.punteggioUtente = utente.punteggioUtente + punteggioPercorso;
            utente.percorsiUtente = utente.percorsiUtente + 1;
            data.putExtra(Keys.UTENTE, utente);
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_FIRST_USER, data);
        }
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        PolylineOptions options = new PolylineOptions().color(Keys.VERDE).width(Keys.SPESSORE_LINEA);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Object posizione : aPercorso) {
            LatLng ll = new LatLng(((Posizione) posizione).latitude, ((Posizione) posizione).longitude);
            builder.include(ll);
            options.add(ll);
        }
        LatLngBounds bounds = builder.build();
        mGoogleMap.addPolyline(options);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
    }
}
