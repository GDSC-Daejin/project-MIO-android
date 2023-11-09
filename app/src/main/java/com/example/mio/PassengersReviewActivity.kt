package com.example.mio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.example.mio.Model.Participants
import com.example.mio.Model.User
import com.example.mio.databinding.ActivityPassengersReviewBinding

class PassengersReviewActivity : AppCompatActivity() {
    private lateinit var prBinding : ActivityPassengersReviewBinding
    //edittext
    private var reviewEditText = ""

    private var mannerCount = ""

    private var type = ""
    private var passengersData = ArrayList<Participants>()
    private var driverData : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prBinding = ActivityPassengersReviewBinding.inflate(layoutInflater)

        setIcon()

        type = intent.getStringExtra("type") as String

        if (type == "PASSENGER") { //내가 손님일때
            driverData = intent.getSerializableExtra("postDriver") as User
        } else if (type == "DRIVER") { //내가 운전자일때
            passengersData = intent.getSerializableExtra("postPassengers") as ArrayList<Participants>
        }

        //여기에 인원 수 만큼 chip? 추가하기 Todo
        prBinding.passengersReviewEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                reviewEditText = editable.toString()
                /*if (editable.isEmpty()) {
                    nbrBinding.readSendComment.visibility = View.GONE
                    nbrBinding.readEditSendComment.visibility = View.GONE
                } else if (getBottomSheetCommentData != "수정"){
                    nbrBinding.readSendComment.visibility = View.VISIBLE
                    nbrBinding.readEditSendComment.visibility = View.GONE
                } else {
                    nbrBinding.readSendComment.visibility = View.GONE
                    nbrBinding.readEditSendComment.visibility = View.VISIBLE
                }*/
            }
        })

        prBinding.passengersReviewRegistrationBtn.setOnClickListener {
            //후기 보내기 Todo
        }


        prBinding.backArrow.setOnClickListener {
            this.finish()
        }

        setContentView(prBinding.root)
    }

    private fun setIcon() {
        prBinding.passengersSatisfactionIv.setOnClickListener {
            mannerCount = "good"

            prBinding.passengersSatisfactionIv.apply {
                setImageResource(R.drawable.review_satisfaction_update_icon)
            }
            prBinding.passengersSatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_9))
            }

            prBinding.passengersCommonlyIv.apply {
                setImageResource(R.drawable.review_commonly_icon)
            }
            prBinding.passengersCommonlyTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }

            prBinding.passengersDissatisfactionIv.apply {
                setImageResource(R.drawable.review_dissatisfaction_icon)
            }
            prBinding.passengersDissatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }
        }

        prBinding.passengersCommonlyIv.setOnClickListener {
            mannerCount = "normal"

            prBinding.passengersCommonlyIv.apply {
                setImageResource(R.drawable.review_commonly_update_icon)
            }
            prBinding.passengersCommonlyTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_9))
            }

            prBinding.passengersDissatisfactionIv.apply {
                setImageResource(R.drawable.review_dissatisfaction_icon)
            }
            prBinding.passengersDissatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }

            prBinding.passengersSatisfactionIv.apply {
                setImageResource(R.drawable.review_satisfaction_icon)
            }
            prBinding.passengersSatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }
        }

        prBinding.passengersDissatisfactionIv.setOnClickListener {
            mannerCount = "bad"

            prBinding.passengersDissatisfactionIv.apply {
                setImageResource(R.drawable.review_dissatisfaction_update_icon)
            }
            prBinding.passengersDissatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_9))
            }

            prBinding.passengersSatisfactionIv.apply {
                setImageResource(R.drawable.review_satisfaction_icon)
            }
            prBinding.passengersSatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }

            prBinding.passengersCommonlyIv.apply {
                setImageResource(R.drawable.review_commonly_icon)
            }
            prBinding.passengersCommonlyTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }
        }

    }
}