package com.dulemata.emiliano.biker.data;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Emiliano on 11/03/2017.
 */

public class Utente implements Parcelable {

    public static final Creator<Utente> CREATOR = new Creator<Utente>() {
        @Override
        public Utente createFromParcel(Parcel in) {
            return new Utente(in);
        }

        @Override
        public Utente[] newArray(int size) {
            return new Utente[size];
        }
    };
    public int idUtente;
    public String emailUtente;
    public String passwordUtente;
    public int punteggioUtente;
    public int percorsiUtente;
    boolean cambiato = false;

    public Utente(SharedPreferences preferences) {
        idUtente = preferences.getInt(Keys.ID, -1);
        emailUtente = preferences.getString(Keys.EMAIL, "");
        passwordUtente = preferences.getString(Keys.PASSWORD, "");
        punteggioUtente = preferences.getInt(Keys.PUNTEGGIO, -1);
        percorsiUtente = preferences.getInt(Keys.NUMERO_PERCORSI, -1);
    }

    public Utente(JSONObject utente) throws JSONException {
        idUtente = utente.getInt(Keys.ID);
        emailUtente = utente.getString(Keys.EMAIL);
        passwordUtente = utente.getString(Keys.PASSWORD);
        punteggioUtente = utente.getInt(Keys.PUNTEGGIO);
        percorsiUtente = utente.getInt(Keys.PERCORSI);
    }

    protected Utente(Parcel in) {
        idUtente = in.readInt();
        emailUtente = in.readString();
        passwordUtente = in.readString();
        punteggioUtente = in.readInt();
        percorsiUtente = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idUtente);
        dest.writeString(emailUtente);
        dest.writeString(passwordUtente);
        dest.writeInt(punteggioUtente);
        dest.writeInt(percorsiUtente);
    }
}
