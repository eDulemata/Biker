package com.dulemata.emiliano.biker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dulemata.emiliano.biker.fragment.NegozioFragment.OnListFragmentInteractionListener;
import com.dulemata.emiliano.biker.data.Negozio;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Negozio} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class NegozioViewAdapter extends RecyclerView.Adapter<NegozioViewAdapter.ViewHolder> {

    private final List<Negozio> mValues;
    private final OnListFragmentInteractionListener mListener;

    public NegozioViewAdapter(List<Negozio> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.negozio_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNomeNegozio.setText(mValues.get(position).nomeNegozio);

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mNomeNegozio;
        public final Button mButton;
        public Negozio mItem;

        public ViewHolder(View view) {
            super(view);
            mNomeNegozio = (TextView) view.findViewById(R.id.nome_negozio);
            mButton = (Button) view.findViewById(R.id.go_to_premi);
        }

    }
}
