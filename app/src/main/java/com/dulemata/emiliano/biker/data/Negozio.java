package com.dulemata.emiliano.biker.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emiliano on 23/03/2017.
 */

public class Negozio implements Parcelable {

    public int idNegozio;
    public String nomeNegozio;

    public Negozio(JSONObject object) {
        try {
            idNegozio = object.getInt(Keys.ID_NEGOZIO);
            nomeNegozio = object.getString(Keys.NOME_NEGOZIO);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Negozio(Parcel in) {
        idNegozio = in.readInt();
        nomeNegozio = in.readString();
    }

    public static final Creator<Negozio> CREATOR = new Creator<Negozio>() {
        @Override
        public Negozio createFromParcel(Parcel in) {
            return new Negozio(in);
        }

        @Override
        public Negozio[] newArray(int size) {
            return new Negozio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idNegozio);
        dest.writeString(nomeNegozio);
    }
}
