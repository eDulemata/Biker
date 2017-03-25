package com.dulemata.emiliano.biker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dulemata.emiliano.biker.fragment.PremiFragment.OnListFragmentInteractionListener;
import com.dulemata.emiliano.biker.data.Premio;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Premio} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PremiViewAdapter extends RecyclerView.Adapter<PremiViewAdapter.ViewHolder> {

    private final List<Premio> mValues;
    private final OnListFragmentInteractionListener mListener;

    public PremiViewAdapter(List<Premio> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_premio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.nome.setText(holder.mItem.nomePremio);
        holder.quantità.setText(Integer.toString(holder.mItem.quantità));
        holder.prezzo.setText(Integer.toString(holder.mItem.valorePremio));
        holder.compra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nome, quantità, prezzo;
        public final Button compra;
        public Premio mItem;

        public ViewHolder(View view) {
            super(view);
            compra = (Button) view.findViewById(R.id.button_compra_premio);
            nome = (TextView) view.findViewById(R.id.nome_acquisto);
            quantità = (TextView) view.findViewById(R.id.quantità_premio);
            prezzo = (TextView) view.findViewById(R.id.prezzo_premio);
        }
    }
}
