package com.dulemata.emiliano.biker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dulemata.emiliano.biker.data.FuoriPercorsoException;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.fragment.HistoryFragment.OnListPercorsoInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Percorso} and makes a call to the
 * specified {@link OnListPercorsoInteractionListener}.
 */
public class PercorsoViewAdapter extends RecyclerView.Adapter<PercorsoViewAdapter.ViewHolder> {

    private List<Percorso> mPercorsi;
    private final OnListPercorsoInteractionListener mListener;

    public PercorsoViewAdapter(List<Percorso> items, OnListPercorsoInteractionListener listener) {
        mPercorsi = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_percorso_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Percorso percorso = mPercorsi.get(position);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListPercorsoInteraction(percorso);
                }
            }
        });
        holder.bind(percorso);
    }

    @Override
    public int getItemCount() {
        if (mPercorsi != null)
            return mPercorsi.size();
        return 0;
    }

    final static class ViewHolder extends RecyclerView.ViewHolder {

        private final Button button;
        private TextView data, oraInizio, oraFine;
        private TrackerProperty trackerProperty1, trackerProperty2, trackerProperty3, trackerProperty4;

        ViewHolder(View view) {
            super(view);
            button = (Button) view.findViewById(R.id.button_see_more);
            data = (TextView) view.findViewById(R.id.data);
            oraInizio = (TextView) view.findViewById(R.id.oraInizio);
            oraFine = (TextView) view.findViewById(R.id.ora_fine);
            trackerProperty1 = new TrackerProperty(view.findViewById(R.id.track_1), TrackerProperty.Proprietà.velocitàMedia, 0);
            trackerProperty2 = new TrackerProperty(view.findViewById(R.id.track_2), TrackerProperty.Proprietà.altituidineMedia, 0);
            trackerProperty3 = new TrackerProperty(view.findViewById(R.id.track_3), TrackerProperty.Proprietà.puntiGuadagnati, 0);
            trackerProperty4 = new TrackerProperty(view.findViewById(R.id.track_4), TrackerProperty.Proprietà.distanzaTotale, 0);
        }

        void bind(Percorso percorso) {
            try {
                data.setText(percorso.getPosizione(0).getDataString());
                oraFine.setText(percorso.getPosizione(0).getOraString());
                oraInizio.setText(percorso.getPosizione(percorso.size() - 1).getOraString());
                trackerProperty1.update(percorso.velocitàMedia);
                trackerProperty2.update(percorso.altitudineMedia);
                trackerProperty3.update(percorso.puntiGuadagnati);
                trackerProperty4.update(percorso.distanzaTotale);
            } catch (FuoriPercorsoException e) {
                e.printStackTrace();
            }
        }


    }
}
