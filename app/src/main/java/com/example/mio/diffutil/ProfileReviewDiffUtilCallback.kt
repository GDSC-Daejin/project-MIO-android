package com.example.mio.diffutil

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.mio.Model.MyAccountReviewData


object ProfileReviewDiffUtilCallback : DiffUtil.ItemCallback<MyAccountReviewData>() {

    override fun areItemsTheSame(oldItem: MyAccountReviewData, newItem: MyAccountReviewData): Boolean {
        val result = oldItem.id == newItem.id // Assuming 'id' is unique for each notification
        Log.d("NotificationDiffUtil", "areItemsTheSame: Comparing oldItem.id = ${oldItem.id} with newItem.id = ${newItem.id}, result: $result")
        return result
    }

    override fun areContentsTheSame(oldItem: MyAccountReviewData, newItem: MyAccountReviewData): Boolean {
        val result = oldItem == newItem // This checks if all fields are the same
        Log.d("NotificationDiffUtil", "areContentsTheSame: Comparing oldItem = $oldItem with newItem = $newItem, result: $result")
        return result
    }
}