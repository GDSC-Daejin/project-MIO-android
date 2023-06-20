package com.example.mio

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mio.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BottomSheetFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var bsBinding : FragmentBottomSheetBinding
    private var selectTargetDate = ""
    private var listener: OnSendFromBottomSheetDialog? = null

    //버튼 체크 변수들
    //체크리스트 펴기 체크
    private var isCheckListClicked = false
    //등하교 버튼 체크
    private var isCheckSchool = false
    //흡연여부 체크
    private var isCheckSmoke = false
    //성별 체크
    private var isCheckGender = false

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
    ): View? {
        bsBinding = FragmentBottomSheetBinding.inflate(inflater, container, false)

        bsBinding.filterCalendar.setOnClickListener {
            val cal = Calendar.getInstance()
            val data = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                selectTargetDate = "${year}년/${month+1}월/${day}일"
                bsBinding.selectDateTv.text = selectTargetDate
                bsBinding.selectDateTv.setTextColor(Color.BLACK)
            }
            DatePickerDialog(requireActivity(), R.style.MySpinnerDatePickerStyle, data, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                Calendar.DAY_OF_MONTH)).show()
        }

        bsBinding.filterChecklistIv.setOnClickListener {
            isCheckListClicked = !isCheckListClicked
            if (isCheckListClicked) {
                CoroutineScope(Dispatchers.Main).launch {
                    bsBinding.filterChecklistIv.setImageResource(R.drawable.filter_checklist_update_icon)
                    bsBinding.checklistLl.visibility = View.VISIBLE
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    bsBinding.filterChecklistIv.setImageResource(R.drawable.filter_checklist_icon)
                    bsBinding.checklistLl.visibility = View.GONE
                }
            }
        }

        bsBinding.filterPlus.setOnClickListener {
            println("plis")
        }

        bsBinding.gtschoolBtn.setOnClickListener {
            isCheckSchool = true //등교
            println("sdag")
            bsBinding.gtschoolBtn.setBackgroundColor(
                ContextCompat.getColor(requireActivity(),
                R.color.mio_blue_4))

            bsBinding.aschoolBtn.setBackgroundColor(
                ContextCompat.getColor(requireActivity(),
                    R.color.mio_gray_1))
        }

        bsBinding.aschoolBtn.setOnClickListener {
            isCheckSchool = false //하교
            println("gl")
            bsBinding.gtschoolBtn.setBackgroundColor(
                ContextCompat.getColor(requireActivity(),
                    R.color.mio_gray_1))

            bsBinding.aschoolBtn.setBackgroundColor(
                ContextCompat.getColor(requireActivity(),
                    R.color.mio_blue_4))
        }

        bsBinding.manBtn.setOnClickListener {
            println("ma")
        }

        bsBinding.womanBtn.setOnClickListener {
            println("wo")
        }

        bsBinding.filterResetTvBtn.setOnClickListener {

        }

        bsBinding.bottomSheetDismissIv.setOnClickListener {
            dismiss()
        }

        bsBinding.filterSearchBtn.setOnClickListener {
            if (listener == null) return@setOnClickListener
            listener?.sendValue("BottomSheetDialog에서 검색 버튼 클릭함!")
            dismiss()
        }

        return bsBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /*val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }*/
        val dialog = BottomSheetDialog(requireContext(),  R.style.BottomSheetDialogTheme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    interface OnSendFromBottomSheetDialog {
        fun sendValue(value: String)
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }


}