package com.gdsc.mio

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdsc.mio.databinding.FragmentAccountSettingBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountSettingBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountSettingBottomSheetFragment(check : Boolean) : BottomSheetDialogFragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var asBinding : FragmentAccountSettingBottomSheetBinding
    private var listener : OnSendFromBottomSheetDialog? = null
    private var isType = check //true면 성별 체크용, false면 흡연 여부용

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
        asBinding = FragmentAccountSettingBottomSheetBinding.inflate(inflater, container, false)

        if (isType) {
            asBinding.accountSetting1.text = "남성"
            asBinding.accountSetting2.text = "여성"

            asBinding.accountSetting1.setOnClickListener {
                //Toast.makeText(requireActivity(), "최신 순", Toast.LENGTH_SHORT).show()
                if (listener == null) return@setOnClickListener
                listener?.sendValue("남성")
                dismiss()
            }

            asBinding.accountSetting2.setOnClickListener {
                //Toast.makeText(requireActivity(), "최신 순", Toast.LENGTH_SHORT).show()
                if (listener == null) return@setOnClickListener
                listener?.sendValue("여성")
                dismiss()
            }
        } else {
            asBinding.accountSetting1.text = "o"
            asBinding.accountSetting2.text = "x"

            asBinding.accountSetting1.setOnClickListener {
                //Toast.makeText(requireActivity(), "최신 순", Toast.LENGTH_SHORT).show()
                if (listener == null) return@setOnClickListener
                listener?.sendValue("o")
                dismiss()
            }

            asBinding.accountSetting2.setOnClickListener {
                //Toast.makeText(requireActivity(), "최신 순", Toast.LENGTH_SHORT).show()
                if (listener == null) return@setOnClickListener
                listener?.sendValue("x")
                dismiss()
            }
        }

        return asBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /*val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }*/

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
         * @return A new instance of fragment AccountSettingBottomSheetFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountSettingBottomSheetFragment(check = false).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}