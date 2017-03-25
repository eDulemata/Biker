package com.dulemata.emiliano.biker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dulemata.emiliano.biker.data.Acquisto;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Acquisto}
 * TODO: Replace the implementation with code for your data type.
 */
public class AcquistiViewAdapter extends RecyclerView.Adapter<AcquistiViewAdapter.ViewHolder> {

    private final List<Acquisto> mValues;

    public AcquistiViewAdapter(List<Acquisto> acquisti) {
        mValues = acquisti;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.acquisto_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Acquisto acquisto = mValues.get(position);
        holder.nomeAcquisto.setText(acquisto.nomeAcquisto);
        holder.valorePremio.setText(Integer.toString(acquisto.valorePremio));
        holder.dataAcquisto.setText(acquisto.getData());
        holder.nomeNegozio.setText(acquisto.nomeNegozio);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nomeAcquisto,valorePremio,dataAcquisto,nomeNegozio;

        public ViewHolder(View view) {
            super(view);
            nomeAcquisto = (TextView) view.findViewById(R.id.nome_acquisto);
            valorePremio = (TextView) view.findViewById(R.id.valore_premio);
            dataAcquisto = (TextView) view.findViewById(R.id.data_acquisto);
            nomeNegozio = (TextView) view.findViewById(R.id.negozio_acquisto);
        }

    }
}
