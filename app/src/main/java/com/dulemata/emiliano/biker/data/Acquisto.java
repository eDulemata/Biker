package com.dulemata.emiliano.biker.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Emiliano on 25/03/2017.
 */

public class Acquisto implements Parcelable {

    public String nomeAcquisto;
    public String nomeNegozio;
    public int valorePremio;
    public long dataAcquisto;

    public Acquisto(JSONObject acquisto) {
        try {
            nomeAcquisto = acquisto.getString(Keys.NOME_PREMIO);
            nomeNegozio = acquisto.getString(Keys.NOME_NEGOZIO);
            valorePremio = acquisto.getInt(Keys.VALORE_ACQUISTO);
            dataAcquisto = acquisto.getLong(Keys.DATA);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    protected Acquisto(Parcel in) {
        nomeAcquisto = in.readString();
        nomeNegozio = in.readString();
        valorePremio = in.readInt();
        dataAcquisto = in.readLong();
    }

    public static final Creator<Acquisto> CREATOR = new Creator<Acquisto>() {
        @Override
        public Acquisto createFromParcel(Parcel in) {
            return new Acquisto(in);
        }

        @Override
        public Acquisto[] newArray(int size) {
            return new Acquisto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nomeAcquisto);
        dest.writeString(nomeNegozio);
        dest.writeInt(valorePremio);
        dest.writeLong(dataAcquisto);
    }

    public String getData() {
        Locale.getDefault().getDisplayLanguage();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM, yyyy");
        Date fixDate = new Date(
                System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos() / 1000000) + (dataAcquisto / 1000000)
        );
        return dateFormat.format(fixDate);
    }
}
