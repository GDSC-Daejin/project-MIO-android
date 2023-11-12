package com.example.mio.BottomSheetFragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
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


//이 바텀시트는 필터 검색 시 필요한 바텀시트
class BottomSheetFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var bsBinding : FragmentBottomSheetBinding
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    //선택한 날짜
    private var selectTargetDate = ""
    //선택한 시간
    private var selectTime = ""

    //필터로 선택한 데이터들을 외부로 전송하기 위한 리스너
    private var listener: OnSendFromBottomSheetDialog? = null

    //버튼 체크 변수들
    //체크리스트 펴기 체크
    private var isCheckListClicked = false
    //등하교 버튼 체크
    private var isCheckSchool = ""
    //흡연여부 체크
    private var isCheckSmoke = ""
    //성별 체크
    private var isCheckGender = ""
    //참여 인원 수
    private var participateNumberOfPeople = 1
    //초기화 체크
    private var isReset = false

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

        initSetting()

        bsBinding.filterCalendar.setOnClickListener {
            val cal = Calendar.getInstance()
            val data = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                selectTargetDate = "${year}년/${month+1}월/${day}일"
                bsBinding.selectDateTv.text = selectTargetDate
                bsBinding.selectDateTv.setTextColor(Color.BLACK)
            }
            DatePickerDialog(requireActivity(),
                R.style.MySpinnerDatePickerStyle, data, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                Calendar.DAY_OF_MONTH)).show()
        }

        bsBinding.filterTime.setOnClickListener {
            showHourPicker()
        /*val time = TimePickerDialog.OnTimeSetListener { _, i, i2 ->
                selectTime = "${i} 시 ${i2} 분?"

                bsBinding.selectTime.text = selectTime
                bsBinding.selectDateTv.setTextColor(Color.BLACK)
            }

            val timePickerDialog = TimePickerDialog(
                activity,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                time,
                hour,
                minute,
                true
            )

            TimePickerDialog(requireActivity(), time, 15, 24, false)
            timePickerDialog.setTitle("Choose hour:")
            timePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            timePickerDialog.show()*/
        }

        bsBinding.filterChecklistIv.setOnClickListener {
            isCheckListClicked = !isCheckListClicked
            if (isCheckListClicked) {
                bsBinding.filterChecklistIv.animate().apply {
                    duration = 300
                    rotation(180f)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    //bsBinding.filterChecklistIv.setImageResource(R.drawable.filter_checklist_update_icon)
                    bsBinding.checklistDetailView.visibility = View.VISIBLE
                }
            } else {
                bsBinding.filterChecklistIv.animate().apply {
                    duration = 300
                    rotation(0f)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    //bsBinding.filterChecklistIv.setImageResource(R.drawable.filter_checklist_icon)
                    bsBinding.checklistDetailView.visibility = View.GONE
                }
            }
        }

        bsBinding.filterMinus.setOnClickListener {
            participateNumberOfPeople -= 1
            if (participateNumberOfPeople > 0) {
                bsBinding.filterParticipateTv.text = participateNumberOfPeople.toString()
            } else {
                bsBinding.filterParticipateTv.text = "1"
                participateNumberOfPeople = 1
            }
        }

        bsBinding.filterPlus.setOnClickListener {
            participateNumberOfPeople += 1
            if (participateNumberOfPeople < 11) {
                bsBinding.filterParticipateTv.text = participateNumberOfPeople.toString()
            } else {
                bsBinding.filterParticipateTv.text = "1"
                participateNumberOfPeople = 1
            }
        }

        bsBinding.gtschoolBtn.setOnClickListener {
            isCheckSchool = "등교" //등교
            saveSharedPreferenceGoogleLogin.setSchool(context, isCheckSchool)

            bsBinding.gtschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
            }

            bsBinding.aschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }

        bsBinding.aschoolBtn.setOnClickListener {
            isCheckSchool = "하교" //하교
            saveSharedPreferenceGoogleLogin.setSchool(context, isCheckSchool)

            bsBinding.aschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
            }
            bsBinding.gtschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }

        bsBinding.manBtn.setOnClickListener {
            isCheckGender = "남성" //남성
            saveSharedPreferenceGoogleLogin.setGender(context, isCheckGender)

            bsBinding.manBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
            }
            bsBinding.womanBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }

        bsBinding.womanBtn.setOnClickListener {
            isCheckGender = "여성" //여성
            saveSharedPreferenceGoogleLogin.setGender(context, isCheckSchool)

            bsBinding.womanBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
            }
            bsBinding.manBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }

        bsBinding.smokerBtn.setOnClickListener {
            isCheckSmoke = "흡연O" //흡연 O
            saveSharedPreferenceGoogleLogin.setSmoke(context, isCheckSmoke)
            bsBinding.smokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
            }
            bsBinding.nsmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }

        bsBinding.nsmokerBtn.setOnClickListener {
            isCheckSmoke = "흡연x" //흡연 X
            saveSharedPreferenceGoogleLogin.setSmoke(context, isCheckSmoke)
            bsBinding.nsmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
            }
            bsBinding.smokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }

        bsBinding.filterResetTvBtn.setOnClickListener {
            isReset = true
            //뷰 초기화
            CoroutineScope(Dispatchers.Main).launch {
                bsBinding.gtschoolBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_layout)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.aschoolBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_layout)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.manBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_layout)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.womanBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_layout)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.smokerBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_layout)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.nsmokerBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_layout)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
            }

            /*값 초기화*/

            //등하교 버튼 체크
            isCheckSchool = ""
            //흡연여부 체크
            isCheckSmoke = ""
            //성별 체크
            isCheckGender = ""
            //참여 인원 수
            participateNumberOfPeople = 1
            bsBinding.filterParticipateTv.text = "1"

            bsBinding.selectDateTv.text = "yyyy/mm/dd"
            bsBinding.selectDateTv.setTextColor(ContextCompat.getColor(requireActivity() ,
                R.color.mio_gray_7
            ))

            bsBinding.selectTime.text = "오전 00:00"
            bsBinding.selectTime.setTextColor(ContextCompat.getColor(requireActivity() ,
                R.color.mio_gray_7
            ))
        }

        bsBinding.bottomSheetDismissIv.setOnClickListener {
            dismiss()
        }

        bsBinding.filterSearchBtn.setOnClickListener {
            if (listener == null) return@setOnClickListener
            listener?.sendValue("${selectTargetDate} ${participateNumberOfPeople} ${isCheckSchool} ${isCheckGender} ${isCheckSmoke} $isReset")
            dismiss()
        }



        return bsBinding.root
    }

    private fun showHourPicker() {
        val myCalender = Calendar.getInstance()
        val hour = myCalender[Calendar.HOUR_OF_DAY]
        val minute = myCalender[Calendar.MINUTE]
        val myTimeListener =
            OnTimeSetListener { view, hourOfDay, minute ->
                if (view.isShown) {
                    myCalender[Calendar.HOUR_OF_DAY] = hourOfDay
                    myCalender[Calendar.MINUTE] = minute
                    selectTime = if (hourOfDay > 12) {
                        val pm = hourOfDay - 12;
                        "오후 " + pm + "시 " + minute + "분 선택"
                    } else {
                        "오전 " + hour + "시 " + minute + "분 선택"
                    }
                    //selectTime = "${hourOfDay} 시 ${minute} 분"

                    bsBinding.selectTime.text = selectTime
                    bsBinding.selectDateTv.setTextColor(Color.BLACK)
                }
            }
        val timePickerDialog = TimePickerDialog(
            activity,
            //여기서 테마 설정해서 커스텀하기
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            myTimeListener,
            hour,
            minute,
            true
        )
        timePickerDialog.setTitle("시간 선택 :")
        timePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        timePickerDialog.show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /*val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }*/
        val dialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
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

    private fun initSetting() {
        if (saveSharedPreferenceGoogleLogin.getSharedSchool(context) != null) {
            isCheckSchool = saveSharedPreferenceGoogleLogin.getSharedSchool(context)!!

            if (isCheckSchool == "등교") {
               CoroutineScope(Dispatchers.Main).launch {
                   bsBinding.gtschoolBtn.apply {
                       setBackgroundResource(R.drawable.round_btn_update_layout)
                       setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
                   }
               }
            } else if (isCheckSchool == "히교") {
                CoroutineScope(Dispatchers.Main).launch {
                    bsBinding.aschoolBtn.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
                    }
                }
            }
        }


        if (saveSharedPreferenceGoogleLogin.getSharedGender(context) != null) {
            isCheckGender = saveSharedPreferenceGoogleLogin.getSharedGender(context)!!

            if (isCheckGender == "남성") {
                CoroutineScope(Dispatchers.Main).launch {
                    bsBinding.manBtn.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
                    }
                }
            } else if (isCheckGender == "여성") {
                CoroutineScope(Dispatchers.Main).launch {
                    bsBinding.womanBtn.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
                    }
                }
            }
        }

        if (saveSharedPreferenceGoogleLogin.getSharedSmoke(context) != null) {
            isCheckSmoke = saveSharedPreferenceGoogleLogin.getSharedSmoke(context)!!

            if (isCheckSmoke == "흡연O") {
                CoroutineScope(Dispatchers.Main).launch {
                    bsBinding.smokerBtn.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
                    }
                }
            } else if (isCheckSmoke == "흡연x") {
                CoroutineScope(Dispatchers.Main).launch {
                    bsBinding.nsmokerBtn.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_1))
                    }
                }
            }
        }
    }


}