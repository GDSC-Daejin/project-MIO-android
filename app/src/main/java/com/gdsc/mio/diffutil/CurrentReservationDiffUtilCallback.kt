package com.gdsc.mio.diffutil

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.gdsc.mio.model.PostData

class CurrentReservationDiffUtilCallback(
    private val mOldList: List<PostData?>,
    private val mNewList: List<PostData?>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldList.size
    }

    override fun getNewListSize(): Int {
        return mNewList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldList[oldItemPosition]
        val newItem = mNewList[newItemPosition]
        val result = oldItem?.postID == newItem?.postID
        Log.d("DiffUtil", "areItemsTheSame: oldPostID=${oldItem?.postID}, newPostID=${newItem?.postID}, result=$result")
        return result
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldList[oldItemPosition]
        val newItem = mNewList[newItemPosition]
        val result = oldItem?.postID == newItem?.postID &&
                oldItem?.postTitle == newItem?.postTitle &&
                oldItem?.postContent == newItem?.postContent &&
                oldItem?.postLocation == newItem?.postLocation &&
                oldItem?.postVerifyGoReturn == newItem?.postVerifyGoReturn &&
                oldItem?.postTargetTime == newItem?.postTargetTime &&
                oldItem?.postCreateDate == newItem?.postCreateDate &&
                oldItem?.postTargetDate == newItem?.postTargetDate &&
                oldItem?.postlatitude == newItem?.postlatitude &&
                oldItem?.postlongitude == newItem?.postlongitude &&
                oldItem?.accountID == newItem?.accountID &&
                oldItem?.postCost == newItem?.postCost &&
                oldItem?.postParticipation == newItem?.postParticipation &&
                oldItem?.postParticipationTotal == newItem?.postParticipationTotal

        Log.d("DiffUtil", "areContentsTheSame: oldItem=$oldItem, newItem=$newItem, result=$result")
        return result
    }

    /*override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // 여기서 필요한 경우 변경된 필드에 대한 정보를 추가로 전달할 수 있습니다.
        //Log.d("DiffUtil", "getChangePayload: oldItemPosition=$oldItemPosition, newItemPosition=$newItemPosition")
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }*/
}
