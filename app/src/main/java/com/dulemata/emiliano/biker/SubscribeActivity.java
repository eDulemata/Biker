package com.dulemata.emiliano.biker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;

public class SubscribeActivity extends AppCompatActivity implements AsyncResponse {

    private final String SUBSCRIBE = "subscribe.php";
    EditText emailInput, passwordInput;
    Button subscribeButton;
    private AlertDialog dialog;

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
    }

    private boolean checkInputs() {
        return (!emailInput.getText().toString().equals("") && !passwordInput.getText().toString().equals(""));
    }

    private void subscribe() {
        dialog = showAlert("", getString(R.string.subscribing), false).create();
        dialog.show();
        BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(this);
        request.execute(Keys.URL_SERVER + SUBSCRIBE + "?email=" + emailInput.getText().toString() + "&pwd=" + passwordInput.getText().toString(), Keys.JSON_RESULT);
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
                dialog = showAlert(getString(R.string.generic_error), getString(R.string.user_already_subscribed), false).setPositiveButton(android.R.string.ok, null).create();
                dialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
