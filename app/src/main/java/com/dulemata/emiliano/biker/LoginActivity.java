package com.dulemata.emiliano.biker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequest;
import com.dulemata.emiliano.biker.data.Utente;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;

public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    public static final String LOGIN = "login.php";
    private static final int SUBSCRIBE_INTENT = 1;
    private EditText email, password;
    private String emailText, passwordText;
    private AlertDialog dialog;
    private CheckBox saveCredentials;
    private boolean autologin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        autologin = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE).getBoolean(Keys.AUTO_LOGIN, false);
        if (autologin) {
            dialog = showAlert("", getString(R.string.logging_in), false).create();
            dialog.show();
            //TODO prendere i dati dal db;
            //moveToMain(new Utente());
            emailText = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE).getString(Keys.EMAIL, "");
            passwordText = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE).getString(Keys.PASSWORD, "");
            sendRequest();
        }
        email = (EditText) findViewById(R.id.email_input);
        password = (EditText) findViewById(R.id.password_input);
        saveCredentials = (CheckBox) findViewById(R.id.save_credentials);

        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputsAreEmpty()) {
                    emailText = email.getText().toString();
                    passwordText = password.getText().toString();
                    sendRequest();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SUBSCRIBE_INTENT:
                if (resultCode == RESULT_OK) {
                    Utente utente = data.getParcelableExtra(Keys.UTENTE);
                    moveToMain(utente);
                }
        }
    }

    private void sendRequest() {
        BackgroundHTTPRequest request = new BackgroundHTTPRequest(this);
        request.execute(Keys.URL_SERVER + LOGIN + "?email=" + emailText + "&pwd=" + passwordText, Keys.JSON_UTENTE);
        dialog = showAlert("", getString(R.string.logging_in), false).create();
        dialog.show();
    }

    private boolean inputsAreEmpty() {
        return (email.getText().toString().equals("") || password.getText().toString().equals(""));
    }

    private void moveToMain(Utente utente) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra(Keys.UTENTE, utente);
        startActivity(intent);
        finish();
    }

    @Override
    public void processResult(JSONArray result) {
        try {
            Utente utente = new Utente(result.getJSONObject(0));
            SharedPreferences preferences = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            if (saveCredentials.isChecked()) {
                editor.putBoolean(Keys.AUTO_LOGIN, true);
                editor.putString(Keys.EMAIL, emailText);
                editor.putString(Keys.PASSWORD, passwordText);
            } else {
                editor.putBoolean(Keys.AUTO_LOGIN, false);
            }
            //TODO salvare l'utente nel db interno
            editor.apply();
            moveToMain(utente);
        } catch (JSONException e) {
            dialog = showAlert(getString(R.string.login), getString(R.string.user_not_subscribed), true).setPositiveButton(android.R.string.ok, null).create();
            dialog.show();
        }
    }

    private AlertDialog.Builder showAlert(String title, String message, boolean isCanellable) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(isCanellable);
        return builder;
    }

}
