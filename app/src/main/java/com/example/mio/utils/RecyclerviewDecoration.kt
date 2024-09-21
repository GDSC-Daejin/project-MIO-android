package com.example.mio.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerviewDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val pos = parent.getChildAdapterPosition(view) //get item index
        val cnt = state.itemCount
        val offset = 1

        when (pos) {
            0 -> {
                outRect.left = offset
            }
            cnt-1 -> {
                outRect.right = offset
            }
            else -> {
                outRect.left = offset
                outRect.right = offset
            }
        }
    }
}