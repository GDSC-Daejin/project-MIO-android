package com.gdsc.mio.bottomsheetfragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdsc.mio.R
import com.gdsc.mio.databinding.FragmentNearPostBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class NearPostBottomSheetFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var abBinding : FragmentNearPostBottomSheetBinding
    //필터로 선택한 데이터들을 외부로 전송하기 위한 리스너
    private var listener: OnSendFromBottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        abBinding = FragmentNearPostBottomSheetBinding.inflate(inflater, container, false)

        abBinding.filterNewest.setOnClickListener {
            if (listener == null) return@setOnClickListener
            listener?.sendValue("최신 순")
            dismiss()
        }

        /*abBinding.filterCloseDistance.setOnClickListener {
            Toast.makeText(requireActivity(), "가까운 순", Toast.LENGTH_SHORT).show()
            if (listener == null) return@setOnClickListener
            listener?.sendValue("가까운 순")
            dismiss()
        }*/

        abBinding.filterLowestPrice.setOnClickListener {
            if (listener == null) return@setOnClickListener
            listener?.sendValue("낮은 가격 순")
            dismiss()
        }

        abBinding.filterNearingEnd.setOnClickListener {
            if (listener == null) return@setOnClickListener
            listener?.sendValue("마감 임박 순")
            dismiss()
        }

        /*abBinding.filterClosest.setOnClickListener {
            Toast.makeText(requireActivity(), "가까운 순", Toast.LENGTH_SHORT).show()
            if (listener == null) return@setOnClickListener
            listener?.sendValue("가까운 순")
            dismiss()
        }*/

        /*
        * if (listener == null) return@setOnClickListener
            listener?.sendValue("$selectTargetDate, ${participateNumberOfPeople}, $isCheckSmoke, $isCheckGender, $isCheckSchool")
            dismiss()
            * */

        return abBinding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    }

    interface OnSendFromBottomSheetDialog {
        fun sendValue(value: String)
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }
}