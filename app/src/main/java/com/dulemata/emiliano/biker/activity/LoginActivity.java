package com.dulemata.emiliano.biker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Utente;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;

public class LoginActivity extends ActivityDialogInteraction implements AsyncResponse {

    public static final String LOGIN = "login.php";
    private static final int SUBSCRIBE_INTENT = 1;
    private EditText emailInput, passwordInput;
    private CheckBox saveCredentials;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE);
        emailInput = (EditText) findViewById(R.id.email_input);
        passwordInput = (EditText) findViewById(R.id.password_input);
        saveCredentials = (CheckBox) findViewById(R.id.save_credentials);
        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputsAreEmpty()) {
                    showProgressDialog("", getString(R.string.logging_in), false);
                    String email = emailInput.getText().toString();
                    String password = passwordInput.getText().toString();
                    sendRequest(email, password);
                }
            }
        });
        Button subscribe = (Button) findViewById(R.id.subscribe_button);
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SubscribeActivity.class);
                startActivityForResult(intent, SUBSCRIBE_INTENT);
            }
        });
        if (savedInstanceState != null) {
            emailInput.setText(savedInstanceState.getString(Keys.EMAIL));
            passwordInput.setText(savedInstanceState.getString(Keys.PASSWORD));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SUBSCRIBE_INTENT:
                if (resultCode == RESULT_OK) {
                    LoginActivity.this.sendRequest(data.getStringExtra(Keys.EMAIL), data.getStringExtra(Keys.PASSWORD));
                }
        }
    }

    private void sendRequest(String email, String password) {
        BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(this);
        request.execute(Keys.URL_SERVER + LOGIN + "?email=" + email + "&pwd=" + password, Keys.JSON_UTENTE);
    }

    private boolean inputsAreEmpty() {
        return (emailInput.getText().toString().equals("") || passwordInput.getText().toString().equals(""));
    }

    private void moveToMain(Utente utente) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        editor = preferences.edit();
        editor.putString(Keys.EMAIL, utente.emailUtente);
        editor.putString(Keys.PASSWORD, utente.passwordUtente);
        editor.putInt(Keys.PUNTEGGIO, utente.punteggioUtente);
        editor.putInt(Keys.ID, utente.idUtente);
        editor.putInt(Keys.NUMERO_PERCORSI, utente.percorsiUtente);
        editor.apply();
        intent.putExtra(Keys.UTENTE, utente);
        startActivity(intent);
        finish();
    }

    @Override
    public void processResult(JSONArray result) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        restoreScreenRotation();
        try {
            if (result != null) {
                Utente utente = new Utente(result.getJSONObject(0));
                editor = preferences.edit();
                if (saveCredentials.isChecked()) {
                    editor.putBoolean(Keys.AUTO_LOGIN, true);
                } else {
                    editor.putBoolean(Keys.AUTO_LOGIN, false);
                }
                editor.apply();
                moveToMain(utente);
            } else {
                alertDialog = setAlert("ERRORE LOGIN", "credenziali inserite non valide", true)
                        .setPositiveButton(android.R.string.ok, null).create();
                alertDialog.show();
            }
        } catch (JSONException e) {
            alertDialog = setAlert("ERRORE RETE", "problema durante la connessione", true)
                    .setPositiveButton(android.R.string.ok, null).create();
            alertDialog.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Keys.EMAIL, emailInput.getText().toString());
        outState.putString(Keys.PASSWORD, passwordInput.getText().toString());
        super.onSaveInstanceState(outState);
    }

}
