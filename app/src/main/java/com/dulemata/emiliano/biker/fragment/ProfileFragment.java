package com.dulemata.emiliano.biker.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.activity.MainActivity;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Utente;
import com.dulemata.emiliano.biker.util.Keys;
import com.dulemata.emiliano.biker.views.TrackerProperty;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;


public class ProfileFragment extends FragmentBiker {

    private static final String JSON_DISTANZA = "distanza";
    TextView email;
    EditText password;
    TrackerProperty punti, distanza;
    Button disiscriviti, passwordButton;
    private WeakReference<MainActivity> reference;
    private String CAMBIA_PASSWORD = "cambia_password.php";
    private String DISISCRIVITI = "unsubscribe.php";
    private AsyncResponse passwordResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            try {
                if (result.length() > 0 && result.getJSONObject(0).getString(Keys.JSON_RESULT).equals(Keys.JSON_OK)) {
                    alertDialog = setAlert("MODIFICA PASSWORD", "Password modificata con successo", true)
                            .setPositiveButton(android.R.string.ok, null)
                            .create();
                    alertDialog.show();
                } else {
                    alertDialog = setAlert("ERRORE MODIFICA PASSWORD", "C'è stato un errore durante la modifica della password", true)
                            .setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

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
    private AsyncResponse disiscrivitiResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            try {
                if (result.length() > 0 && result.getJSONObject(0).getString(Keys.JSON_RESULT).equals(Keys.JSON_OK)) {
                    alertDialog = setAlert("DISISCRIZIONE COMPLETATA", "Utente disiscitto con successo", false)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    alertDialog.show();
                } else {
                    alertDialog = setAlert("ERRORE DISISCRIZIONE", "C'è stato un errore durante la disiscrizione", false)
                            .setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton("Annulla", null)
                            .create();
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Utente utente;
    private double distanzaValue;
    private AsyncResponse distanzaResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            if (result != null) {
                if (result.length() > 0) {
                    try {
                        distanzaValue = result.getJSONObject(0).getDouble(JSON_DISTANZA);
                        distanza.update(distanzaValue);
                    } catch (JSONException e) {
                        distanza.update(-1);
                    }
                }
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        reference = new WeakReference<>((MainActivity) context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        email = (TextView) v.findViewById(R.id.email_input);
        passwordButton = (Button) v.findViewById(R.id.password_button);
        disiscriviti = (Button) v.findViewById(R.id.disiscriviti);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences preferences = getActivity().getSharedPreferences(Keys.SHARED_PREFERENCIES, Context.MODE_PRIVATE);
        utente = new Utente(preferences);
        email.setText(utente.emailUtente);
        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = new EditText(getActivity());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("INSERISCI NUOVA PASSWORD");
                builder.setView(password);
                builder.setCancelable(false);
                builder.setPositiveButton("Salva", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!password.getText().toString().equals("")) {
                            cambiaPassword();
                        }
                    }
                })
                        .setNegativeButton("Annulla", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        disiscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = setAlert("CONFERMA DISISCRIZIONE", "Disiscrivendoti perderai tutti i tuoi dati. Continuare?", false)
                        .setPositiveButton("Continua", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                disiscriviti();
                            }
                        })
                        .setNegativeButton("Annulla", null)
                        .create();
                alertDialog.show();
            }
        });
        punti = new TrackerProperty(view.findViewById(R.id.punti_totali), TrackerProperty.Proprietà.punti, utente.punteggioUtente);
        if (savedInstanceState == null) {
            BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(distanzaResponse);
            requestGet.execute(Keys.URL_SERVER + "get_distanza_utente.php?id=" + utente.idUtente, Keys.JSON_RESULT);
        } else {
            distanzaValue = savedInstanceState.getDouble(Keys.DISTANZA_TOTALE);
            distanza = new TrackerProperty(view.findViewById(R.id.distanza_totale), TrackerProperty.Proprietà.distanzaTotale, distanzaValue);
        }
        distanza = new TrackerProperty(view.findViewById(R.id.distanza_totale), TrackerProperty.Proprietà.distanzaTotale, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putDouble(Keys.DISTANZA_TOTALE, distanzaValue);
        super.onSaveInstanceState(outState);
    }

    public void cambiaPassword() {
        BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(passwordResponse);
        requestGet.execute(Keys.URL_SERVER + CAMBIA_PASSWORD + "?id=" + utente.idUtente + "&pwd=" + password.getText().toString(), Keys.JSON_RESULT);
        showProgressAlert("Modifica password in corso", "Attendere...", false);
    }

    public void disiscriviti() {
        BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(disiscrivitiResponse);
        requestGet.execute(Keys.URL_SERVER + DISISCRIVITI + "?id=" + utente.idUtente, Keys.JSON_RESULT);
        showProgressAlert("Disiscrizione in corso", "Attendere...", false);
    }

}
