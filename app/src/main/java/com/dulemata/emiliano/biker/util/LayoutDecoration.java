package com.dulemata.emiliano.biker.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Emiliano on 24/03/2017.
 */

public class LayoutDecoration extends RecyclerView.ItemDecoration {
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