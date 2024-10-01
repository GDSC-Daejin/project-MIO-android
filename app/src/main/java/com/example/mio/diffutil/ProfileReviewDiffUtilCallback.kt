package com.example.mio.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.example.mio.model.MyAccountReviewData


object ProfileReviewDiffUtilCallback : DiffUtil.ItemCallback<MyAccountReviewData>() {

    override fun areItemsTheSame(
        oldItem: MyAccountReviewData,
        newItem: MyAccountReviewData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: MyAccountReviewData,
        newItem: MyAccountReviewData
    ): Boolean {
        return oldItem == newItem
    }
}