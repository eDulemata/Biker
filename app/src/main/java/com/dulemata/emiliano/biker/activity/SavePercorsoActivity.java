package com.dulemata.emiliano.biker.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestPost;
import com.dulemata.emiliano.biker.data.FuoriPercorsoException;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.data.Posizione;
import com.dulemata.emiliano.biker.data.Utente;
import com.dulemata.emiliano.biker.fragment.TrackerFragment;
import com.dulemata.emiliano.biker.util.Keys;
import com.dulemata.emiliano.biker.views.TrackerProperty;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

public class SavePercorsoActivity extends ActivityDialogInteraction implements OnMapReadyCallback {

    private static final String SALVA_PERCORSO = "salva_percorso.php";
    private static final String AGGIORNA_UTENTE = "aggiorna_utente.php";
    private static final String PRIMA_VOLTA = "prima_volta";
    private SupportMapFragment mappaPercorso;
    Utente utente;
    TextView data, oraInizio, oraFine;
    TrackerProperty trackerProperty1, trackerProperty2, trackerProperty3, trackerProperty4;
    Button save, discard;
    Percorso aPercorso;
    private SharedPreferences preferences;
    private AsyncResponse aggiornaUtenteResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            try {
                if (result != null && result.getJSONObject(0).getString(Keys.JSON_RESULT).equals(Keys.JSON_OK)) {
                    alertDialog = setAlert("SALVATAGGIO COMPLETATO", "Il percorso è stato salvato e il profilo aggiornato", false)
                            .setPositiveButton("Torna al tracking", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .create();
                    alertDialog.show();
                } else {
                    alertDialog = setAlert("ERRORE SALVATAGGIO", "C'è stato un errore durante l'aggiornamento del profilo. Annullando l'operazione il percorso non verrà salvato.", false)
                            .setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    aggiornaUtente();
                                }
                            })
                            .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO annullare salvataggio percorso
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private AsyncResponse savePercorsoResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            if (result != null && result.length() == 1) {
                aggiornaUtente();
            } else {
                alertDialog = setAlert("ERRORE SALVATAGGIO", "C'è stato un errore durante il salvataggio del percorso. Annullando l'operazione il percorso non verrà salvato.", false)
                        .setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                savePercorso();
                            }
                        })
                        .setNegativeButton("Annulla", null)
                        .create();
                alertDialog.show();
            }
        }
    };
    private boolean primaVolta = true;

    public void aggiornaUtente() {
        if (primaVolta) {
            SharedPreferences.Editor editor = preferences.edit();
            utente.punteggioUtente = utente.punteggioUtente + aPercorso.puntiGuadagnati;
            utente.percorsiUtente = utente.percorsiUtente + 1;
            editor.putInt(Keys.PUNTEGGIO, utente.punteggioUtente);
            editor.putInt(Keys.NUMERO_PERCORSI, utente.percorsiUtente);
            editor.apply();
            primaVolta = false;
        }
        BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(aggiornaUtenteResponse);
        request.execute(Keys.URL_SERVER + AGGIORNA_UTENTE +
                "?id_utente=" + utente.idUtente +
                "&punteggio=" + utente.punteggioUtente +
                "&percorsi=" + utente.percorsiUtente, Keys.JSON_RESULT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_percorso);
        //TrackerFragment.isTracking = false;
        preferences = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE);
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
        if (savedInstanceState == null) {
            utente = getIntent().getParcelableExtra(Keys.UTENTE);
        } else {
            utente = savedInstanceState.getParcelable(Keys.UTENTE);
            primaVolta = savedInstanceState.getBoolean(PRIMA_VOLTA);
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePercorso();
            }
        });
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void savePercorso() {
        showProgressDialog("SALVATAGGIO IN CORSO", "attendere...", false);
        BackgroundHTTPRequestPost request = new BackgroundHTTPRequestPost(savePercorsoResponse);
        utente = new Utente(preferences);
        int idUtente = utente.idUtente;
        int numPercorso = utente.percorsiUtente;
        try {
            String body = aPercorso.toJsonObject(idUtente, numPercorso).toString();
            body = body.trim();
            String url = Keys.URL_SERVER + SALVA_PERCORSO;
            request.execute(url, body, Keys.JSON_RESULT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Keys.UTENTE, utente);
        outState.putBoolean(PRIMA_VOLTA, primaVolta);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        PolylineOptions options = new PolylineOptions().color(Keys.VERDE).width(Keys.SPESSORE_LINEA_RIDOTTO);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Object posizione : aPercorso) {
            LatLng ll = new LatLng(((Posizione) posizione).latitude, ((Posizione) posizione).longitude);
            builder.include(ll);
            options.add(ll);
        }
        LatLngBounds bounds = builder.build();
        googleMap.addPolyline(options);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0));
    }


}
