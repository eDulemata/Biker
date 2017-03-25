package com.dulemata.emiliano.biker.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dulemata.emiliano.biker.LoginActivity;
import com.dulemata.emiliano.biker.MainActivity;
import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.TrackerProperty;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Utente;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

import static com.dulemata.emiliano.biker.util.Dialog.showAlert;


public class ProfileFragment extends Fragment implements FragmentInt {

    TextView email;
    EditText password;
    TrackerProperty punti, distanza;
    Button disiscriviti, passwordButton;
    private WeakReference<MainActivity> reference;
    private AlertDialog dialog;
    private String CAMBIA_PASSWORD = "cambia_password.php";
    private Utente utente;
    private AsyncResponse passwordResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            try {
                if (result.length() > 0 && result.getJSONObject(0).getString(Keys.JSON_RESULT).equals(Keys.JSON_OK)) {
                    dialog = showAlert(getContext(), dialog,"", "Password modificata", true).setPositiveButton(android.R.string.ok, null).create();
                } else {
                    dialog = showAlert(getContext(), dialog,"", "Errore modifica password. Per favore riprovare", true).setPositiveButton(android.R.string.ok, null).create();
                }
                dialog.show();
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
                    dialog = showAlert(getContext(), dialog,"", "Utente disiscritto", false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().getSharedPreferences(Keys.SHARED_PREFERENCIES, Context.MODE_PRIVATE).edit().remove(Keys.AUTO_LOGIN).apply();
                            Intent intent = new Intent(reference.get(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }).create();
                } else {
                    dialog = showAlert(getContext(), dialog,"", "Errore disiscrizione. Per favore riprova", true).setPositiveButton(android.R.string.ok, null).create();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.show();
        }
    };
    private String DISISCRIVITI = "unsubscribe.php";

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
                dialog = showAlert(getContext(), dialog, "Modifica Password", "Inserisci nuova password", true)
                        .setView(password)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!password.getText().toString().equals(utente.passwordUtente)) {
                                    utente.passwordUtente = password.getText().toString();
                                    BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(ProfileFragment.this.passwordResponse);
                                    request.execute(Keys.URL_SERVER
                                            + CAMBIA_PASSWORD
                                            + "?id=" + utente.idUtente
                                            + "&pwd=" + utente.passwordUtente, Keys.JSON_RESULT);
                                } else {
                                    Toast.makeText(ProfileFragment.this.getActivity(), "Inserita stessa password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .create();
                dialog.show();
            }
        });
        disiscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = showAlert(ProfileFragment.this.getContext(), dialog, "Disiscrizione", "Sei sicuro di volerti disiscrivere?", false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(ProfileFragment.this.disiscrivitiResponse);
                                request.execute(Keys.URL_SERVER + DISISCRIVITI + "?id=" + utente.idUtente, Keys.JSON_RESULT);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                dialog.show();
            }
        });
        punti = new TrackerProperty(view.findViewById(R.id.punti_totali), TrackerProperty.Proprietà.punti, utente.punteggioUtente);
        distanza = new TrackerProperty(view.findViewById(R.id.distanza_totale), TrackerProperty.Proprietà.distanzaTotale, 420);
    }

}
