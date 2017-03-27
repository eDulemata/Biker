package com.dulemata.emiliano.biker.views;

import android.view.View;
import android.widget.TextView;

import com.dulemata.emiliano.biker.R;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by Emiliano on 14/03/2017.
 */

public class TrackerProperty {

    private TextView value;
    private Proprietà propietà;

    public enum Proprietà {
        velocità, velocitàMedia, distanza, distanzaTotale, punti, puntiGuadagnati, altitudine, altituidineMedia
    }

    private static HashMap<Proprietà, Integer> titoli = new HashMap<>();

    static {
        titoli.put(Proprietà.velocità, R.string.speed);
        titoli.put(Proprietà.velocitàMedia, R.string.average_speed);
        titoli.put(Proprietà.altitudine, R.string.altitude);
        titoli.put(Proprietà.altituidineMedia, R.string.average_altitude);
        titoli.put(Proprietà.distanza, R.string.distance);
        titoli.put(Proprietà.distanzaTotale, R.string.total_distance);
        titoli.put(Proprietà.punti, R.string.points);
        titoli.put(Proprietà.puntiGuadagnati, R.string.points_gained);
    }


    public TrackerProperty(View layout, Proprietà property, double doubleValue) {
        TextView aProperty = (TextView) layout.findViewById(R.id.description);
        this.value = (TextView) layout.findViewById(R.id.value);
        aProperty.setText(titoli.get(property));
        setText(property, doubleValue);
    }

    private void setText(Proprietà property, double doubleValue) {
        String value = null;
        DecimalFormat form = new DecimalFormat("0.00");
        propietà = property;
        switch (titoli.get(property)) {
            case R.string.speed:
            case R.string.average_speed:
                value = form.format(doubleValue) + " Km/h";
                break;
            case R.string.distance:
            case R.string.total_distance:
            case R.string.average_altitude:
            case R.string.altitude:
                if (doubleValue < 1000) {
                    value = form.format(doubleValue) + " m";
                } else {
                    value = form.format(doubleValue / 1000) + " Km";
                }
                break;
            case R.string.points:
            case R.string.points_gained:
                value = String.valueOf(doubleValue);
                break;
        }
        this.value.setText(value);
    }

    public void update(double value) {
        setText(propietà, value);
    }
}
