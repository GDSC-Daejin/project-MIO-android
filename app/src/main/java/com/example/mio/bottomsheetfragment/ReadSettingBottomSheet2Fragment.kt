package com.example.mio.bottomsheetfragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mio.R
import com.example.mio.ReadSettingBottomSheetFragment
import com.example.mio.databinding.FragmentReadSettingBottomSheet2Binding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReadSettingBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReadSettingBottomSheet2Fragment : BottomSheetDialogFragment() { //유저용
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rsBinding : FragmentReadSettingBottomSheet2Binding
    private var listener : OnSendFromBottomSheetDialog? = null

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
        rsBinding = FragmentReadSettingBottomSheet2Binding.inflate(inflater, container, false)

        /*rsBinding.readSettingReport.setOnClickListener {
            //Toast.makeText(requireActivity(), "최신 순", Toast.LENGTH_SHORT).show()
            if (listener == null) return@setOnClickListener
            listener?.sendValue("신고")
            dismiss()
        }*/

        rsBinding.readSettingBookmark.setOnClickListener {
            //Toast.makeText(requireActivity(), "최신 순", Toast.LENGTH_SHORT).show()
            if (listener == null) return@setOnClickListener
            listener?.sendValue("북마크")
            dismiss()
        }

        return rsBinding.root
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
         * @return A new instance of fragment ReadSettingBottomSheetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReadSettingBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}