package com.example.mio.bottomsheetfragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TimePicker
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
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    private var sendSelectTime = ""
    //필터로 선택한 데이터들을 외부로 전송하기 위한 리스너
    private var listener: OnSendFromBottomSheetDialog? = null

    //버튼 체크 변수들
    //체크리스트 펴기 체크
    private var isCheckListClicked = false // true 닫힌거 아래로열림 , false 열린거 위로열린모양
    //등하교 버튼 체크
    private var isCheckSchool = ""
    //흡연여부 체크
    private var isCheckSmoke = ""
    //성별 체크
    private var isCheckGender = ""
    //참여 인원 수
    private var participateNumberOfPeople = 0
    //초기화 체크
    private var isReset = "false"

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
        bsBinding = FragmentBottomSheetBinding.inflate(inflater, container, false)

        //initSetting()

        bsBinding.filterCalendar.setOnClickListener {
            val cal = Calendar.getInstance()
            val data = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                // SimpleDateFormat을 사용하여 날짜를 원하는 형식으로 포맷
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectTargetDate = dateFormat.format(selectedDate.time)  // 2024-01-01 형식

                bsBinding.selectDateTv.text = requireActivity().getString(R.string.setDateText3, "$year", "${month+1}", "$day")//"${year}년/${month+1}월/${day}일"
                bsBinding.selectDateTv.setTextColor(Color.BLACK)
                bsBinding.filterCalendar.setImageResource(R.drawable.filter_calendar_update_icon)
            }
            DatePickerDialog(requireActivity(),
                R.style.MySpinnerDatePickerStyle, data, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                Calendar.DAY_OF_MONTH)).show()
        }

        bsBinding.filterTime.setOnClickListener {
            showHourPicker()
        }

        bsBinding.filterChecklistIv.setOnClickListener {
            // ScrollView의 특정 위치로 부드럽게 스크롤
            bsBinding.checklistDetailView.smoothScrollTo(0, bsBinding.checklistContainer.top)
            // 포커스를 이동
            bsBinding.checklistContainer.requestFocus()
            if (isCheckListClicked) {
                bsBinding.filterChecklistIv.animate().apply {
                    duration = 300
                    rotation(0f)
                }
                isCheckListClicked = !isCheckListClicked
                bsBinding.checklistDetailView.visibility = View.VISIBLE

            } else {
                bsBinding.filterChecklistIv.animate().apply {
                    duration = 300
                    rotation(180f)
                }
                isCheckListClicked = !isCheckListClicked
                bsBinding.checklistDetailView.visibility = View.GONE

            }
        }

        bsBinding.filterMinus.setOnClickListener {
            participateNumberOfPeople -= 1
            if (participateNumberOfPeople > 0) {
                bsBinding.filterParticipateTv.text = participateNumberOfPeople.toString()
            } else {
                bsBinding.filterParticipateTv.text = "0"
                participateNumberOfPeople = 0
            }
        }

        bsBinding.filterPlus.setOnClickListener {
            participateNumberOfPeople += 1
            if (participateNumberOfPeople < 11) {
                bsBinding.filterParticipateTv.text = participateNumberOfPeople.toString()
            } else {
                bsBinding.filterParticipateTv.text = "0"
                participateNumberOfPeople = 0
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }

        bsBinding.filterResetTvBtn.setOnClickListener {
            isReset = "true"
            //뷰 초기화
            CoroutineScope(Dispatchers.Main).launch {
                bsBinding.gtschoolBtn.apply {
                    setBackgroundResource(R.drawable.edit_check_btn)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.aschoolBtn.apply {
                    setBackgroundResource(R.drawable.edit_check_btn)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.manBtn.apply {
                    setBackgroundResource(R.drawable.edit_check_btn)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.womanBtn.apply {
                    setBackgroundResource(R.drawable.edit_check_btn)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.smokerBtn.apply {
                    setBackgroundResource(R.drawable.edit_check_btn)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
                bsBinding.nsmokerBtn.apply {
                    setBackgroundResource(R.drawable.edit_check_btn)
                    setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
                }
            }



            /*값 초기화*/

            //등하교 버튼 체크
            isCheckSchool = " "
            //흡연여부 체크
            isCheckSmoke = " "
            //성별 체크
            isCheckGender = " "
            //참여 인원 수
            participateNumberOfPeople = 1
            bsBinding.filterParticipateTv.text = "1"

            bsBinding.selectDateTv.text = requireActivity().getString(R.string.setInitDateText)
            bsBinding.selectDateTv.setTextColor(ContextCompat.getColor(requireActivity() ,
                R.color.mio_gray_7
            ))

            bsBinding.selectTime.text = requireActivity().getString(R.string.setInitTimeText)
            bsBinding.selectTime.setTextColor(ContextCompat.getColor(requireActivity() ,
                R.color.mio_gray_7
            ))
        }

        bsBinding.bottomSheetDismissIv.setOnClickListener {
            dismiss()
        }

        bsBinding.filterSearchBtn.setOnClickListener {
            if (listener == null) return@setOnClickListener
            listener?.sendValue("${selectTargetDate},${sendSelectTime},${participateNumberOfPeople},${isCheckSchool},${isCheckGender},${isCheckSmoke}")
            dismiss()
        }



        return bsBinding.root
    }

   /* private fun showHourPicker() {
        val myCalender = Calendar.getInstance()
        val hour = myCalender[Calendar.HOUR_OF_DAY]
        val minute = myCalender[Calendar.MINUTE]
        val myTimeListener =
            OnTimeSetListener { view, hourOfDay, _ ->
                if (view.isShown) {
                    myCalender[Calendar.HOUR_OF_DAY] = hourOfDay
                    myCalender[Calendar.MINUTE] = minute
                    sendSelectTime = String.format("%02d:%02d:00", hourOfDay, minute)
                    selectTime = if (hourOfDay > 12) {
                        val pm = hourOfDay - 12
                        "오후 " + pm + "시 " + minute + "분 선택"
                    } else {
                        "오전 " + hour + "시 " + minute + "분 선택"
                    }
                    //selectTime = "${hourOfDay} 시 ${minute} 분"

                    bsBinding.selectTime.text = selectTime
                    bsBinding.selectTime.setTextColor(Color.BLACK)
                    bsBinding.filterTime.setImageResource(R.drawable.filter_time_update_icon)
                }
            }
        val timePickerDialog = TimePickerDialog(
            activity,
            //여기서 테마 설정해서 커스텀하기
            android.R.style.Theme_Material_Light_Dialog_NoActionBar,
            myTimeListener,
            hour,
            minute,
            true
        )
        timePickerDialog.setTitle("시간 선택 :")
        timePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        timePickerDialog.show()
    }*/
    private fun showHourPicker() {
        val myCalender = Calendar.getInstance()
        val hour = myCalender[Calendar.HOUR_OF_DAY]
        val minute = myCalender[Calendar.MINUTE]

        // 커스텀 다이얼로그 레이아웃 인플레이션
        val dialogView = layoutInflater.inflate(R.layout.time_picker_layout, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        timePicker.hour = hour // API 23 이상
        timePicker.minute = minute // API 23 이상

        // 다이얼로그 생성
        val timePickerDialog = AlertDialog.Builder(requireContext())
            .setTitle("시간 선택 :")
            .setView(dialogView)
        val alertDialog = timePickerDialog.create()

         btnOk.setOnClickListener {
                // 시간 선택 완료 시 처리
                val selectedHour = timePicker.hour // API 23 이상
                val selectedMinute = timePicker.minute // API 23 이상

                // 선택한 시간과 분을 이용해 문자열 생성
                val tempS = "${selectedHour}시 ${selectedMinute}분"
                sendSelectTime = String.format("%02d:%02d:00", selectedHour, selectedMinute)

                // 오전/오후 표시 처리
                selectTime = if (selectedHour >= 12) {
                    val pm = if (selectedHour == 12) selectedHour else selectedHour - 12
                    "오후 $pm 시 $selectedMinute 분"
                } else {
                    "오전 $selectedHour 시 $selectedMinute 분"
                }

             bsBinding.selectTime.text = selectTime
             bsBinding.selectTime.setTextColor(Color.BLACK)
             bsBinding.filterTime.setImageResource(R.drawable.filter_time_update_icon)
             alertDialog.dismiss()
         }


        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        // 다이얼로그 배경 설정
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.white)
        alertDialog.show()
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
            parentLayout?.let {pl ->
                val behaviour = BottomSheetBehavior.from(pl)
                setupFullHeight(pl)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
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

    /*private fun initSetting() {
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
    }*/
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // 여기서 초기화 로직을 수행
        resetViews()
    }

    private fun resetViews() {
        // 뷰 초기화 로직 작성
        isReset = "true"
        //뷰 초기화
        CoroutineScope(Dispatchers.IO).launch {
            bsBinding.gtschoolBtn.apply {
                setBackgroundResource(R.drawable.edit_check_btn)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
            bsBinding.aschoolBtn.apply {
                setBackgroundResource(R.drawable.edit_check_btn)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
            bsBinding.manBtn.apply {
                setBackgroundResource(R.drawable.edit_check_btn)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
            bsBinding.womanBtn.apply {
                setBackgroundResource(R.drawable.edit_check_btn)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
            bsBinding.smokerBtn.apply {
                setBackgroundResource(R.drawable.edit_check_btn)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
            bsBinding.nsmokerBtn.apply {
                setBackgroundResource(R.drawable.edit_check_btn)
                setTextColor(ContextCompat.getColor(requireActivity() , R.color.mio_gray_11))
            }
        }



        /*값 초기화*/

        //등하교 버튼 체크
        isCheckSchool = " "
        //흡연여부 체크
        isCheckSmoke = " "
        //성별 체크
        isCheckGender = " "
        //참여 인원 수
        participateNumberOfPeople = 1
        bsBinding.filterParticipateTv.text = "1"

        bsBinding.selectDateTv.text = requireActivity().getString(R.string.setInitDateText)
        bsBinding.selectDateTv.setTextColor(ContextCompat.getColor(requireActivity() ,
            R.color.mio_gray_7
        ))

        bsBinding.selectTime.text =  requireActivity().getString(R.string.setInitTimeText)
        bsBinding.selectTime.setTextColor(ContextCompat.getColor(requireActivity() ,
            R.color.mio_gray_7
        ))
    }

}