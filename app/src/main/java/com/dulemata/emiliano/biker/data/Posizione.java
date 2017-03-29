package com.dulemata.emiliano.biker.data;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Emiliano on 11/03/2017.
 */

public class Posizione implements Parcelable, Comparable<Posizione> {

    public final static Creator<Posizione> CREATOR = new Creator<Posizione>() {
        @Override
        public Posizione createFromParcel(Parcel source) {
            return new Posizione(source);
        }

        @Override
        public Posizione[] newArray(int size) {
            return new Posizione[size];
        }
    };
    public double latitude;
    public double longitude;
    public double altitudine;
    public double velocitàIstantanea;
    public long data;

    private Posizione(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        altitudine = in.readDouble();
        velocitàIstantanea = in.readDouble();
        data = in.readLong();
    }

    public Posizione(JSONObject jsonObject) throws JSONException {
        this.latitude = jsonObject.getDouble(Keys.LATITUDE);
        this.longitude = jsonObject.getDouble(Keys.LONGITUDINE);
        this.data = jsonObject.getLong(Keys.DATA);
        this.velocitàIstantanea = jsonObject.getDouble(Keys.VELOCITA_ISTANTANEA);
        this.altitudine = jsonObject.getDouble(Keys.ALTITUDINE);
    }

    public Posizione(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitudine = location.getAltitude();
        this.velocitàIstantanea = toKmH(location.getSpeed());
        this.data = location.getElapsedRealtimeNanos();
    }


    private double toKmH(double velocità) {
        return velocità * 3.6;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Keys.LATITUDE, latitude);
        jsonObject.put(Keys.LONGITUDINE, longitude);
        jsonObject.put(Keys.ALTITUDINE, altitudine);
        jsonObject.put(Keys.VELOCITA_ISTANTANEA, velocitàIstantanea);
        jsonObject.put(Keys.DATA, data);
        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(altitudine);
        dest.writeDouble(velocitàIstantanea);
        dest.writeLong(data);
    }

    public String getDataString() {
        Locale.getDefault().getDisplayLanguage();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM, yyyy");
        Date fixDate = new Date(
                System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos() / 1000000) + (data / 1000000)
        );
        return dateFormat.format(fixDate);
    }

    public String getOraString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date fixDate = new Date(
                System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos() / 1000000) + (data / 1000000)
        );
        return dateFormat.format(fixDate);
    }

    @Override
    public int compareTo(@NonNull Posizione posizione) {
        if (this.latitude == posizione.latitude && this.longitude == posizione.longitude) {
            return 0;
        }
        return 1;
    }
}
