package com.keemsa.seasonify.features.palettes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.keemsa.colorpalette.ColorPalette;
import com.keemsa.seasonify.R;

/**
 * Created by sebastian on 22/07/17.
 */

public class PaletteViewHolder extends RecyclerView.ViewHolder {

    private ColorPalette plt_item;
    private LinearLayout ll_palette_container;
    private ImageView imv_delete_item;

    public PaletteViewHolder(View itemView) {
        super(itemView);

        this.plt_item = (ColorPalette) itemView.findViewById(R.id.plt_item);
        this.ll_palette_container = (LinearLayout) itemView.findViewById(R.id.ll_palette_container);
        this.imv_delete_item = (ImageView) itemView.findViewById(R.id.imv_delete_item);
    }

    public void setColors(int[] colors) {
        plt_item.setColors(colors);
    }

    public void setVisibility(int visibility) {
        ll_palette_container.setVisibility(visibility);
        plt_item.setVisibility(visibility);
        imv_delete_item.setVisibility(visibility);
    }

    public void addDeleteListener(OnClickListener listener) {
        imv_delete_item.setOnClickListener(listener);
    }
}
