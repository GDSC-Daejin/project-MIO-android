package com.example.mio.adapter
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.model.DateData
import com.example.mio.R
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



    inner class CalendarViewHolder(binding : CalendarCellBinding ) : RecyclerView.ViewHolder(binding.root) {

        private var position : Int? = null
        var dateTV = binding.dateCell
        private var dayTV = binding.dayCell
        var containerLL = binding.calendarLl

        fun bind(calendarData: DateData, position : Int) {
            this.position = position
            dateTV.text = calendarData.date
            //dateTV.setTextColor(ContextCompat.getColor(context , R.color.mio_gray_7))

            dayTV.text = calendarData.day


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarAdapter.CalendarViewHolder {
        context = parent.context
        binding = CalendarCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarAdapter.CalendarViewHolder, position: Int) {
        holder.bind(calendarItemData[holder.adapterPosition]!!, holder.adapterPosition)
        val day = calendarItemData[holder.adapterPosition]
        val dateNow: Date = Calendar.getInstance().time
        val format = SimpleDateFormat("d", Locale.getDefault())
        format.format(dateNow)
        if (day!!.day == format.format(dateNow)) {
            holder.itemView.setBackgroundColor(Color.DKGRAY)
        }


        if (selectedPostion == holder.adapterPosition) {
            //holder.containerLL.setBackgroundColor(Color.BLUE)
            holder.containerLL.setBackgroundResource(R.drawable.round_update_background)
            holder.dateTV.setTextColor(ContextCompat.getColor(context , R.color.mio_gray_1))
        } else {
            holder.containerLL.setBackgroundColor(Color.TRANSPARENT)
            holder.containerLL.setBackgroundResource(R.drawable.round_background)
            holder.dateTV.setTextColor(ContextCompat.getColor(context , R.color.mio_gray_11))
        }

        holder.itemView.setOnClickListener {
            oldSelectedPostion = selectedPostion
            selectedPostion = holder.adapterPosition
            notifyItemChanged(oldSelectedPostion)
            notifyItemChanged(selectedPostion)

            itemClickListener.onClick(it, holder.adapterPosition, "${calendarItemData[holder.adapterPosition]!!.year}-"+
                    if (calendarItemData[holder.adapterPosition]!!.month.toInt() < 10) {
                        "0${calendarItemData[holder.adapterPosition]!!.month}-"
                    } else {
                        calendarItemData[holder.adapterPosition]!!.month+"-"
                    } +
                    if (calendarItemData[holder.adapterPosition]!!.date.toInt() < 10) {
                        "0${calendarItemData[holder.adapterPosition]!!.date}"
                    } else {
                        calendarItemData[holder.adapterPosition]!!.date
                    })
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
        fun onClick(view: View, position: Int, itemId: String)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}