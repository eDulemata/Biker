package com.dulemata.emiliano.biker.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dulemata.emiliano.biker.AcquistiViewAdapter;
import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Acquisto;
import com.dulemata.emiliano.biker.util.Keys;
import com.dulemata.emiliano.biker.util.LayoutDecoration;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.dulemata.emiliano.biker.util.Dialog.showAlert;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class AcquistiFragment extends Fragment implements AsyncResponse, FragmentInt {

    private RecyclerView recyclerView;
    private AlertDialog dialog;
    private List<Acquisto> acquisti = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AcquistiFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.acquisti_list, container, false);

        Context context = view.getContext();
        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        LayoutDecoration layoutDecoration = new LayoutDecoration((int) getResources().getDimension(R.dimen.items_margin));
        recyclerView.addItemDecoration(layoutDecoration);
        if (savedInstanceState == null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(Keys.SHARED_PREFERENCIES, Context.MODE_PRIVATE);
            int id = preferences.getInt(Keys.ID, -1);
            BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(this);
            requestGet.execute(Keys.URL_SERVER + "get_acquisti.php?&id_utente=" + id, Keys.JSON_ACQUISTI);
        } else {
            acquisti = savedInstanceState.getParcelableArrayList(Keys.ACQUISTI);
            setAdapter();
        }
        return view;
    }

    public void setAdapter() {
        recyclerView.setAdapter(new AcquistiViewAdapter(acquisti));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Keys.ACQUISTI, (ArrayList<? extends Parcelable>) acquisti);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void processResult(JSONArray result) {
        if (result != null) {
            int size = result.length();
            try {
                if (!result.getJSONObject(0).getString("nome_premio").equals("null")) {
                    for (int i = 0; i < size; i++) {
                        acquisti.add(new Acquisto(result.getJSONObject(i)));

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setAdapter();
        } else {
            dialog = showAlert(getContext(), dialog, "ERRORE", "C'è stato un errore durante l'acquisizione degli acquisti. Riprovare", true)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            dialog.show();
        }
    }

}
