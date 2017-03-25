package com.dulemata.emiliano.biker.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Emiliano on 11/03/2017.
 */

public class Percorso implements Parcelable, Iterable {

    public static final Creator<Percorso> CREATOR = new Creator<Percorso>() {
        @Override
        public Percorso createFromParcel(Parcel source) {
            return new Percorso(source);
        }

        @Override
        public Percorso[] newArray(int size) {
            return new Percorso[size];
        }
    };
    private static final double PUNTI_AL_METRO = 0.1;
    public int idPercorso;
    public double distanzaTotale;
    public int puntiGuadagnati;
    public double velocitàMedia;
    public double altitudineMedia;
    private List<Posizione> mPosizioni = new LinkedList<>();
    private int size;
    private double sommaAltitudini;
    private double sommaVelocità;

    public Percorso() {
    }

    public Percorso(JSONObject jsonObject) throws JSONException {
        mPosizioni = new LinkedList<>();
        altitudineMedia = jsonObject.getDouble(Keys.ALTITUDINE_MEDIA);
        distanzaTotale = jsonObject.getDouble(Keys.DISTANZA_TOTALE);
        puntiGuadagnati = jsonObject.getInt(Keys.PUNTI_GUADAGNATI);
        velocitàMedia = jsonObject.getDouble(Keys.VELOCITA_MEDIA);
        idPercorso = jsonObject.getInt(Keys.ID_PERCORSO);
        JSONArray array = jsonObject.getJSONArray(Keys.POSIZIONI);
        size = array.length();
        for (int i = 0; i < size; i++) {
            mPosizioni.add(new Posizione(array.getJSONObject(i)));
        }
    }

    private Percorso(Parcel in) {
        mPosizioni = new LinkedList<>();
        idPercorso = in.readInt();
        distanzaTotale = in.readDouble();
        puntiGuadagnati = in.readInt();
        velocitàMedia = in.readDouble();
        altitudineMedia = in.readDouble();
        size = in.readInt();
        in.readTypedList(mPosizioni, Posizione.CREATOR);
    }

    public void addPosizione(Object posizione) {
        Posizione p = (Posizione) posizione;
        size++;
        if (size == 1) {
            distanzaTotale = 0;
            altitudineMedia = p.altitudine;
            sommaAltitudini = p.altitudine;
            puntiGuadagnati = 0;
            velocitàMedia = toKmH(p.velocitàIstantanea);
        } else {
            distanzaTotale = distanzaTotale + calcolaDistanza(mPosizioni.get(size - 2), p);
            sommaAltitudini = sommaAltitudini + p.altitudine;
            sommaVelocità = sommaVelocità + toKmH(p.velocitàIstantanea);
            altitudineMedia = sommaAltitudini / size;
            velocitàMedia = sommaVelocità / size;
            puntiGuadagnati = (int) (distanzaTotale * PUNTI_AL_METRO);
        }
        mPosizioni.add(p);
    }

    private double toKmH(double velocità) {
        return velocità * 3.6;
    }

    public Posizione getPosizione(int i) throws FuoriPercorsoException {
        if (i > size()) {
            throw new FuoriPercorsoException();
        }
        return mPosizioni.get(i);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public JSONObject toJsonObject(int id_utente, int id_percorso) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Keys.ID_PERCORSO, id_percorso);
        object.put(Keys.DISTANZA_TOTALE, distanzaTotale);
        object.put(Keys.PUNTI_GUADAGNATI, puntiGuadagnati);
        object.put(Keys.ID, id_utente);
        object.put(Keys.VELOCITA_MEDIA, velocitàMedia);
        object.put(Keys.ALTITUDINE_MEDIA, altitudineMedia);
        JSONArray array = new JSONArray();
        for (Posizione posizione : mPosizioni) {
            array.put(posizione.toJsonObject());
        }
        object.put("json", array);
        return object;
    }

    public int size() {
        return size;
    }

    private double calcolaDistanza(Posizione prev, Posizione succ) {
        double distanza = 0;
        double lat1 = toRadiante(succ.latitude);
        double lat2 = toRadiante(prev.latitude);
        double lng1 = toRadiante(succ.longitude);
        double lng2 = toRadiante(prev.longitude);
        double fi = Math.abs(lng1 - lng2);
        double p = Math.acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2) * Math.cos(lat1) * Math.cos(fi));
        int RAGGIO_TERRA = 6371 * 1000;
        distanza = p * RAGGIO_TERRA;
        return distanza;
    }

    private double toRadiante(double grado) {
        return (grado * Math.PI) / 180;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idPercorso);
        dest.writeDouble(distanzaTotale);
        dest.writeInt(puntiGuadagnati);
        dest.writeDouble(velocitàMedia);
        dest.writeDouble(altitudineMedia);
        dest.writeInt(size);
        dest.writeTypedList(mPosizioni);
    }

    @Override
    public Iterator<Posizione> iterator() {
        return (mPosizioni).iterator();
    }
}
