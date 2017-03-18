package com.dulemata.emiliano.biker.data;

import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Emiliano on 11/03/2017.
 */

public interface PercorsoInt extends Parcelable, Iterable {

    void addPosizione(Object posizione);

    Posizione getPosizione(int i) throws FuoriPercorsoException;

    boolean isEmpty();

    JSONArray toJsonArray() throws JSONException;

    int size();

}
