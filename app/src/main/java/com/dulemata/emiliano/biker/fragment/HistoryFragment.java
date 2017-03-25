package com.dulemata.emiliano.biker.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dulemata.emiliano.biker.MainActivity;
import com.dulemata.emiliano.biker.PercorsoViewAdapter;
import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.util.Keys;
import com.dulemata.emiliano.biker.util.LayoutDecoration;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListPercorsoInteractionListener}
 * interface.
 */
public class HistoryFragment extends Fragment implements FragmentInt, AsyncResponse {

    private OnListPercorsoInteractionListener mListener;
    private ArrayList<Percorso> mPercorsi;
    private static final String GET_PERCORSI_UTENTE = "get_percorsi_utente.php";
    private WeakReference<MainActivity> reference;
    private RecyclerView recyclerView;
    private AlertDialog dialog;
    private SharedPreferences preferences;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Keys.PERCORSI, mPercorsi);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_percorso_list, container, false);
        if (view instanceof RecyclerView) {
            recyclerView = (RecyclerView) view;
        }
        preferences = getActivity().getSharedPreferences(Keys.SHARED_PREFERENCIES, Context.MODE_PRIVATE);
        BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(this);
        if (savedInstanceState == null) {
            mPercorsi = new ArrayList<>();
            String url = Keys.URL_SERVER + GET_PERCORSI_UTENTE + "?id_utente=" + preferences.getInt(Keys.ID, -1);
            request.execute(url, Keys.JSON_PERCORSI);
        } else {
            mPercorsi = savedInstanceState.getParcelableArrayList(Keys.PERCORSI);
            createView();
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        reference = new WeakReference<>((MainActivity) context);
        if (context != null) {
            mListener = (OnListPercorsoInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListNegozioInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        reference = null;
    }

    @Override
    public void processResult(JSONArray result) {
        try {
            if (result != null && result.length() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    mPercorsi.add(new Percorso(result.getJSONObject(i)));
                }
                createView();
            } else {
                dialog = showAlert("", "Nessuna attività ancora seguita", true).create();
                dialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createView() {
        PercorsoViewAdapter adapter = new PercorsoViewAdapter(mPercorsi, mListener);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        LayoutDecoration layoutDecoration = new LayoutDecoration((int) getResources().getDimension(R.dimen.items_margin));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(layoutDecoration);
    }

    private AlertDialog.Builder showAlert(String title, String message, boolean isCanellable) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(isCanellable);
        return builder;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListPercorsoInteractionListener {
        // TODO: Update argument type and name
        void onListPercorsoInteraction(Percorso item);
    }

}
