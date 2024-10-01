package com.example.mio.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.example.mio.model.PostData


class ReviewWriteableDiffUtilCallback(
    private val mOldList: List<PostData?>,
    private val mNewList: List<PostData?>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return mOldList.size
    }

    override fun getNewListSize(): Int {
        return mNewList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldList[oldItemPosition]?.postID == mNewList[newItemPosition]?.postID
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldList[oldItemPosition]
        val newItem = mNewList[newItemPosition]
        return oldItem == newItem
    }

   /* override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement this method if you're going to use item change payloads.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }*/
}