package com.dulemata.emiliano.biker.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequest;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.util.Keys;

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
public class HistoryFragment extends Fragment implements FragmentInt, AsyncResponse {

    private OnListFragmentInteractionListener mListener;
    private ArrayList<Percorso> mPercorsi;
    private static final String GET_PERCORSI_UTENTE = "get_percorsi_utente.php";
    private WeakReference<MainActivity> reference;
    private RecyclerView recyclerView;
    private AlertDialog dialog;

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
        BackgroundHTTPRequest request = new BackgroundHTTPRequest(this);
        if (savedInstanceState == null) {
            mPercorsi = new ArrayList<>();
            String url = Keys.URL_SERVER + GET_PERCORSI_UTENTE + "?id_utente=" + reference.get().utente.idUtente;
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
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Percorso item);
    }

    private class LayoutDecoration extends RecyclerView.ItemDecoration {
        private int margin;

        /**
         * constructor
         *
         * @param margin desirable margin size in px between the views in the recyclerView
         */
        public LayoutDecoration(int margin) {
            this.margin = margin;

        }

        /**
         * Set different margins for the items inside the recyclerView: no top margin for the first row
         * and no left margin for the first column.
         */
        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.top = margin;
        }
    }
}
