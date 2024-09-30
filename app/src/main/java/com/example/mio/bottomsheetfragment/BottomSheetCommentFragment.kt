package com.example.mio.bottomsheetfragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.example.mio.model.CommentData
import com.example.mio.R
import com.example.mio.databinding.FragmentBottomCommentSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [AnotherBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomSheetCommentFragment(setEditText : CommentData?, parentId : String?) : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var abBinding : FragmentBottomCommentSheetBinding
    //필터로 선택한 데이터들을 외부로 전송하기 위한 리스너
    private var listener: OnSendFromBottomSheetDialog? = null
    private var setCommentData : CommentData? = setEditText
    private var commentEditText = ""
    private var getBottomSheetCommentData = ""
    private var parentCommentId : String? = parentId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        abBinding = FragmentBottomCommentSheetBinding.inflate(inflater, container, false)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        if (setCommentData != null) {
            abBinding.readCommentEt.setText(setCommentData?.content)
        }

        if (parentCommentId != null) {
            abBinding.bottomCommentUserTv.text = "${parentCommentId}님에게"
        } else {
            abBinding.bottomCommentUserTv.visibility = View.GONE
        }

        abBinding.readCommentEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                abBinding.readSendComment.visibility = View.GONE
                abBinding.readEditSendComment.visibility = View.GONE
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // Nothing to do in this method
            }

            override fun afterTextChanged(editable: Editable) {
                val newText = editable.toString()
                Log.e("commentTest", "After text changed: $newText")

                if (newText.isNotEmpty()) {
                    commentEditText = newText
                }

                if (newText.isEmpty() && getBottomSheetCommentData != "수정") {
                    Log.e("commentTest", "비어있고 수정아닐때")
                    //commentEditText = newText
                } else if (newText.isNotEmpty() && getBottomSheetCommentData != "수정") {
                    Log.e("commentTest", "일반 댓글")
                    commentEditText = newText
                    abBinding.readSendComment.visibility = View.VISIBLE
                    abBinding.readEditSendComment.visibility = View.GONE
                } else {
                    Log.e("commentTest", "댓글 수정할때")
                    commentEditText = newText
                    abBinding.readSendComment.visibility = View.GONE
                    abBinding.readEditSendComment.visibility = View.VISIBLE
                }
            }
        })

        abBinding.readSendComment.setOnClickListener {
            if (listener == null && commentEditText == "") return@setOnClickListener
            listener?.sendValue(commentEditText)
            dismiss()
        }

        return abBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        bottomSheet.layoutParams = layoutParams
    }

    /*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        *//*val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }*//*
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                //setupFullHeight(it)
                //behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        return dialog
    }*/

    override fun onResume() {
        super.onResume()
        // EditText에 포커스 및 가상 키보드 표시
        Log.d("bottomsheet", "onresume")
        abBinding.readCommentEt.requestFocus()
        val manager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        manager!!.showSoftInput( abBinding.readCommentEt, InputMethodManager.SHOW_IMPLICIT)
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