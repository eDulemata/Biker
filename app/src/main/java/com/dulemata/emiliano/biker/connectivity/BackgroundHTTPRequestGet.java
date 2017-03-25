package com.dulemata.emiliano.biker.connectivity;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//import com.google.gson.JsonObject;

/**
 * Created by Emiliano on 06/03/2017.
 */

public class BackgroundHTTPRequestGet extends AsyncTask<String, Integer, JSONArray> {

    private AsyncResponse delegate;
    private HttpURLConnection connection = null;

    public BackgroundHTTPRequestGet(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONArray jsonArray = null;
        try {
            URL aUrl = new URL(params[0]);
            JSONObject json = getJsonObject(aUrl);
            if (json != null)
                jsonArray = json.getJSONArray(params[1]);
        } catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        }
        connection.disconnect();
        return jsonArray;
    }

    private JSONObject getJsonObject(URL url) {
        JSONObject object = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");
            connection.connect();
            int status = connection.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    inputStream.close();
                    String json_string = sb.toString().trim();
                    object = new JSONObject(json_string);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    protected void onPostExecute(JSONArray utente) {
        delegate.processResult(utente);
    }
}

