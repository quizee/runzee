package com.example.myrunzeeapp;

import android.support.v7.widget.RecyclerView;

public interface ItemTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
