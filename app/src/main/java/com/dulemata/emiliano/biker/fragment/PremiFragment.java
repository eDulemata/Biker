package com.dulemata.emiliano.biker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dulemata.emiliano.biker.views.viewAdapter.PremiViewAdapter;
import com.dulemata.emiliano.biker.R;
import com.dulemata.emiliano.biker.data.Premio;
import com.dulemata.emiliano.biker.util.Keys;
import com.dulemata.emiliano.biker.util.LayoutDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PremiFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private List<Premio> mPremi;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PremiFragment() {
    }

    public static PremiFragment newInstance(List<Premio> premi) {
        PremiFragment fragment = new PremiFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Keys.PREMI, (ArrayList<? extends Parcelable>) premi);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPremi = getArguments().getParcelableArrayList(Keys.PREMI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.premi_list, container, false);

        Context context = view.getContext();
        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        LayoutDecoration layoutDecoration = new LayoutDecoration((int) getResources().getDimension(R.dimen.items_margin));
        recyclerView.addItemDecoration(layoutDecoration);
        if (savedInstanceState != null)
            mPremi = savedInstanceState.getParcelableArrayList(Keys.PREMI);
        setAdapter();
        return view;
    }

    public void setAdapter() {
        recyclerView.setAdapter(new PremiViewAdapter(mPremi, mListener));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Keys.PREMI, (ArrayList<? extends Parcelable>) mPremi);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        void onListFragmentInteraction(Premio premio);
    }
}
