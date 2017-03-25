package com.dulemata.emiliano.biker.connectivity;

import android.os.AsyncTask;

import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Emiliano on 21/03/2017.
 */

public class BackgroundHTTPRequestPost extends AsyncTask<String, Void, JSONArray> {
    private AsyncResponse delegate;
    private HttpURLConnection connection = null;

    public BackgroundHTTPRequestPost(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONArray jsonArray = null;
        try {
            URL aUrl = new URL(params[Keys.URL_STRING]);
            String body = params[Keys.BODY_POST];
            JSONObject json = getJsonObject(aUrl, body);
            if (json != null)
                jsonArray = json.getJSONArray(params[2]);
        } catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        }
        connection.disconnect();
        return jsonArray;
    }

    private JSONObject getJsonObject(URL url, String json) {
        JSONObject object = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded ; charset=utf-8");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(json);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
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
