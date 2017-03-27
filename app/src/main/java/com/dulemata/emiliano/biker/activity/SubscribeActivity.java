package com.dulemata.emiliano.biker.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;

public class SubscribeActivity extends ActivityDialogInteraction implements AsyncResponse {

    EditText emailInput, passwordInput;
    Button subscribeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        emailInput = (EditText) findViewById(R.id.email_input);
        passwordInput = (EditText) findViewById(R.id.password_input);
        subscribeButton = (Button) findViewById(R.id.subscribe_button);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailInput.setHint("");
                passwordInput.setHint("");
                if (checkInputs())
                    subscribe();
                else {
                    emailInput.setHintTextColor(Color.RED);
                    passwordInput.setHintTextColor(Color.RED);
                    emailInput.setHint(R.string.email_not_inserted);
                    passwordInput.setHint(R.string.password_not_inserted);
                }
            }

        });
        if (savedInstanceState != null) {
            emailInput.setText(savedInstanceState.getString(Keys.EMAIL));
            passwordInput.setText(savedInstanceState.getString(Keys.PASSWORD));
        }
    }

    private boolean checkInputs() {
        return (!emailInput.getText().toString().equals("") && !passwordInput.getText().toString().equals(""));
    }

    private void subscribe() {
        showProgressDialog("ISCRIZIONE IN CORSO", "attendere...", false);
        BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(this);
        String SUBSCRIBE = "subscribe.php";
        request.execute(Keys.URL_SERVER + SUBSCRIBE + "?email=" + emailInput.getText().toString() + "&pwd=" + passwordInput.getText().toString(), Keys.JSON_RESULT);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Keys.EMAIL, emailInput.getText().toString());
        outState.putString(Keys.PASSWORD, passwordInput.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void processResult(JSONArray result) {
        try {
            if (result.getJSONObject(0).getString(Keys.JSON_RESULT).equals(Keys.JSON_OK)) {
                Intent data = new Intent();
                data.putExtra(Keys.EMAIL, emailInput.getText().toString());
                data.putExtra(Keys.PASSWORD, passwordInput.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            } else {
                alertDialog = setAlert("ISCRIZIONE ANNULLATA", "Utente già iscritto", true)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
                alertDialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
