package com.dulemata.emiliano.biker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Negozio;
import com.dulemata.emiliano.biker.util.Keys;
import com.dulemata.emiliano.biker.util.LayoutDecoration;
import com.dulemata.emiliano.biker.views.viewAdapter.NegozioViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class NegozioFragment extends FragmentBiker implements AsyncResponse {

    private WeakReference<OnListFragmentInteractionListener> mListener;
    private ArrayList<Negozio> negozi;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NegozioFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        negozi = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.negozi_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            LayoutDecoration layoutDecoration = new LayoutDecoration((int) getResources().getDimension(R.dimen.items_margin));
            recyclerView.addItemDecoration(layoutDecoration);
            if (savedInstanceState == null) {
                BackgroundHTTPRequestGet requestGet = new BackgroundHTTPRequestGet(this);
                requestGet.execute(Keys.URL_SERVER + "get_negozi.php", Keys.JSON_NEGOZI);
            } else {
                negozi = savedInstanceState.getParcelableArrayList(Keys.NEGOZI);
                setView();
            }

        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = new WeakReference<>((OnListFragmentInteractionListener) context);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Keys.NEGOZI, negozi);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void processResult(JSONArray result) {
        if (result != null) {
            int size = result.length();
            if(size>0) {
                for (int i = 0; i < size; i++) {
                    try {
                        negozi.add(new Negozio(result.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                setView();
            } else {
                alertDialog = setAlert("NESSUN PERCORSO", "Non è stato salvato nessun percorso", true)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
                alertDialog.show();
            }
        } else {
            alertDialog = setAlert("ERRORE RETE", "Non è stato possibile ottenere i percorsi", true)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            alertDialog.show();
        }
    }

    private void setView() {
        recyclerView.setAdapter(new NegozioViewAdapter(negozi, mListener.get()));
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Negozio idNegozio);
    }
}
