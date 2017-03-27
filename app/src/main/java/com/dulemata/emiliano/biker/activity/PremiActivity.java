package com.dulemata.emiliano.biker.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Negozio;
import com.dulemata.emiliano.biker.data.Premio;
import com.dulemata.emiliano.biker.fragment.PremiFragment;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PremiActivity extends ActivityDialogInteraction implements AsyncResponse, PremiFragment.OnListFragmentInteractionListener {

    private ArrayList<Premio> premi = new ArrayList<>();
    private Negozio negozio;
    private AsyncResponse acquistaPremioResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            if (result != null) {
                try {
                    if (result.getJSONObject(0).getString(Keys.JSON_RESULT).equals(Keys.JSON_OK)) {
                        SharedPreferences.Editor editor = preferences.edit();
                        int nuovoPunteggio = preferences.getInt(Keys.PUNTEGGIO, -1) - premio.valorePremio;
                        editor.putInt(Keys.PUNTEGGIO, nuovoPunteggio);
                        editor.apply();
                        alertDialog = setAlert("OPERAZIONE COMPLETA", "Premio acqistato", false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).create();
                        alertDialog.show();
                    } else {
                        alertDialog = setAlert("ERRORE OPERAZIONE", "C'è stato un errore durante l'acquisto", false)
                                .setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onListFragmentInteraction(premio);
                                    }
                                })
                                .setNegativeButton("Annulla", null)
                                .create();
                        alertDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                alertDialog = setAlert("ERRORE CONNESSIONE", "C'è stato un errore durante l'acquisto", false)
                        .setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onListFragmentInteraction(premio);
                            }
                        })
                        .setNegativeButton("Annulla", null)
                        .create();
                alertDialog.show();
            }
        }
    };
    private SharedPreferences preferences;
    private Premio premio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            negozio = getIntent().getParcelableExtra(Keys.NEGOZIO);
            BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(this);
            requestGet.execute(Keys.URL_SERVER + "get_premi.php?id_negozio=" + negozio.idNegozio, Keys.PREMI);
        } else {
            premi = savedInstanceState.getParcelableArrayList(Keys.PREMI);
            negozio = savedInstanceState.getParcelable(Keys.NEGOZIO);
            setFragment();
        }

    }

    private void setFragment() {
        Fragment fragment = PremiFragment.newInstance(premi);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void processResult(JSONArray result) {
        if (result != null) {
            int size = result.length();
            for (int i = 0; i < size; i++) {
                try {
                    premi.add(new Premio(result.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        setFragment();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Keys.PREMI, premi);
        outState.putParcelable(Keys.NEGOZIO, negozio);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onListFragmentInteraction(Premio premio) {
        this.premio = premio;
        preferences = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE);
        int punti = preferences.getInt(Keys.PUNTEGGIO, -1);
        if (premio.valorePremio <= punti) {
            showProgressDialog("ACQUISTO IN CORSO", "attentdere...", false);
            BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(acquistaPremioResponse);
            String url = Keys.URL_SERVER +
                    "ottieni_premio.php?id_premio=" + premio.idPremio +
                    "&nome_premio=" + premio.nomePremio +
                    "&id_utente=" + preferences.getInt(Keys.ID, -1) +
                    "&id_negozio=" + negozio.idNegozio +
                    "&nome_negozio=" + negozio.nomeNegozio +
                    "&prezzo=" + premio.valorePremio +
                    "&data=" + SystemClock.elapsedRealtimeNanos();
            url = url.replaceAll(" ", "%20");
            requestGet.execute(url, Keys.JSON_PREMI);
        } else {
            alertDialog = setAlert("IMPOSSIBILE CONTINUARE", "Punti non sufficienti per riscattare il premio", true)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            alertDialog.show();
        }

    }
}
