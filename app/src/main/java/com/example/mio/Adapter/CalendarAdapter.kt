package com.example.mio.Adapter
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.CalendarUtil
import com.example.mio.Model.DateData
import com.example.mio.databinding.CalendarCellBinding
import java.text.SimpleDateFormat
import java.util.*


class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>(){
    private lateinit var binding : CalendarCellBinding
    var calendarItemData = mutableListOf<DateData?>()
    private lateinit var context : Context
    //리사이클러뷰 특정 아이템 선택
    private var oldSelectedPostion = -1
    private var selectedPostion = -1
    var crDate = ""



    inner class CalendarViewHolder(private val binding : CalendarCellBinding ) : RecyclerView.ViewHolder(binding.root) {

        private var position : Int? = null
        private var dateTV = binding.dateCell
        private var dayTV = binding.dayCell
        var containerLL = binding.calendarLl

        fun bind(calendarData: DateData, position : Int) {
            this.position = position
            dateTV.text = calendarData.date
            dayTV.text = calendarData.day


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarAdapter.CalendarViewHolder {
        context = parent.context
        binding = CalendarCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CalendarAdapter.CalendarViewHolder, position: Int) {
        holder.bind(calendarItemData[holder.adapterPosition]!!, holder.adapterPosition)
        var day = calendarItemData[holder.adapterPosition]
        val dateNow: Date = Calendar.getInstance().time
        val format = SimpleDateFormat("d", Locale.getDefault())
        format.format(dateNow)
        if (day!!.day == format.format(dateNow)) {
            holder.itemView.setBackgroundColor(Color.DKGRAY)
        }


        if (selectedPostion == holder.adapterPosition) {
            holder.containerLL.setBackgroundColor(Color.BLUE)
        } else {
            holder.containerLL.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            oldSelectedPostion = selectedPostion
            selectedPostion = holder.adapterPosition
            notifyItemChanged(oldSelectedPostion)
            notifyItemChanged(selectedPostion)

            itemClickListener.onClick(it, holder.adapterPosition, calendarItemData[holder.adapterPosition]!!.date.toInt())
        }
    }


    override fun getItemCount(): Int {
        return calendarItemData.size
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        calendarItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }


    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}