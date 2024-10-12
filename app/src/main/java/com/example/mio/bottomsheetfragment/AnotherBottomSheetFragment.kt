package com.example.mio.bottomsheetfragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mio.R
import com.example.mio.databinding.FragmentAnotherBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnotherBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnotherBottomSheetFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var abBinding : FragmentAnotherBottomSheetBinding
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
        abBinding = FragmentAnotherBottomSheetBinding.inflate(inflater, container, false)

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AnotherBottomSheetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AnotherBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}