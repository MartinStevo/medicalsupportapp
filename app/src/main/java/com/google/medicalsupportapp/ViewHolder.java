package com.google.medicalsupportapp;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        mView = itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mClickListener.onItemClick(view, getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                mClickListener.onItemLongClick(view, getAdapterPosition());
                return true;
            }
        });
    }

    public void setDetails(Context ctx, String meno, String priezvisko, String adresa) {

        TextView mName = mView.findViewById(R.id.name_text);
        TextView mAddress = mView.findViewById(R.id.address_text);

        String name = "";
        if (meno.equals("*")) {
            name = priezvisko;
        } else {
            name = meno + " " + priezvisko;
        }
        mName.setText(name);
        mAddress.setText(adresa);

    }

    private ViewHolder.ClickListener mClickListener;

    public interface ClickListener {

        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);

    }

    public void setOnClickListener(ViewHolder.ClickListener clickListener) {

        mClickListener = clickListener;

    }
}
