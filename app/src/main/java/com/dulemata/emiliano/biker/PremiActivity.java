package com.dulemata.emiliano.biker;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Negozio;
import com.dulemata.emiliano.biker.data.Premio;
import com.dulemata.emiliano.biker.fragment.PremiFragment;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import static com.dulemata.emiliano.biker.util.Dialog.showAlert;

public class PremiActivity extends AppCompatActivity implements AsyncResponse, PremiFragment.OnListFragmentInteractionListener {

    private ArrayList<Premio> premi = new ArrayList<>();
    private AlertDialog dialog;
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
                        dialog = showAlert(PremiActivity.this, dialog, "", "Acquisto effettuato", false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .create();
                        dialog.show();
                    } else {
                        dialog = showAlert(PremiActivity.this, dialog, "ERRORE", "Acquisto non effettuato. Riprovare", true)
                                .setPositiveButton(android.R.string.ok, null)
                                .create();
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                dialog = showAlert(PremiActivity.this, dialog, "ERRORE", "Acquisto non effettuato. Riprovare", true)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
                dialog.show();
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
        if (punti > 0) {
            if (premio.valorePremio <= punti) {
                dialog = showAlert(this, dialog, "ATTENDERE", "Acquisto in corso", false).create();
                dialog.show();
                BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(acquistaPremioResponse);
                String url = Keys.URL_SERVER +
                        "ottieni_premio.php?id_premio=" + premio.idPremio +
                        "&nome_premio=" + premio.nomePremio +
                        "&id_utente=" + preferences.getInt(Keys.ID, -1) +
                        "&id_negozio=" + negozio.idNegozio +
                        "&nome_negozio=" + negozio.nomeNegozio +
                        "&prezzo=" + premio.valorePremio +
                        "&data=" + SystemClock.elapsedRealtimeNanos();
                url = url.replaceAll(" ","%20");;
                requestGet.execute(url, Keys.JSON_PREMI);
            } else {
                dialog = showAlert(this, dialog, "IMPOSSIBILE RISCATTARE PREMIO", "Punti posseduti non sufficenti", true)
                        .setPositiveButton(android.R.string.ok, null).create();
                dialog.show();
            }
        } else {
            dialog = showAlert(this, dialog, "ERRORE", "Impossibile ottenere il punteggio. Effettuare nuovamente il login", true)
                    .setPositiveButton(android.R.string.ok, null).create();
            dialog.show();
        }
    }
}
