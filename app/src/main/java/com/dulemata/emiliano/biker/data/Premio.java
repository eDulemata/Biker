package com.dulemata.emiliano.biker.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.dulemata.emiliano.biker.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Emiliano on 23/03/2017.
 */

public class Premio implements Parcelable {

    public int idNegozio;
    public int idPremio;
    public String nomePremio;
    public int valorePremio;
    public int quantità;

    public Premio(JSONObject object) {
        if (object != null) {
            try {
                idNegozio = object.getInt(Keys.ID_NEGOZIO);
                idPremio = object.getInt(Keys.ID_PREMIO);
                nomePremio = object.getString(Keys.NOME_PREMIO);
                valorePremio = object.getInt(Keys.VALORE_PREMIO);
                quantità = object.getInt(Keys.QUANTITA_PREMIO);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected Premio(Parcel in) {
        idNegozio = in.readInt();
        idPremio = in.readInt();
        nomePremio = in.readString();
        valorePremio = in.readInt();
        quantità = in.readInt();
    }

    public static final Creator<Premio> CREATOR = new Creator<Premio>() {
        @Override
        public Premio createFromParcel(Parcel in) {
            return new Premio(in);
        }

        @Override
        public Premio[] newArray(int size) {
            return new Premio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idNegozio);
        dest.writeInt(idPremio);
        dest.writeString(nomePremio);
        dest.writeInt(valorePremio);
        dest.writeInt(quantità);
    }
}
