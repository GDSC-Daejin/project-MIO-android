package com.example.mio.Adapter
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.DateData
import com.example.mio.Model.PostData
import com.example.mio.databinding.CalendarCellBinding
import com.example.mio.databinding.PostItemBinding


class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>(){
    private lateinit var binding : CalendarCellBinding
    var calendarItemData = mutableListOf<DateData?>()
    private lateinit var context : Context
    //리사이클러뷰 특정 아이템 선택
    private var oldSelectedPostion = -1
    private var selectedPostion = -1
    var crDate = 0

    inner class CalendarViewHolder(private val binding : CalendarCellBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var dateTV = binding.dateCell
        private var dayTV = binding.dayCell
        var containerLL = binding.calendarLl

        fun bind(calendarData: DateData, position : Int) {
            this.position = position
            dateTV.text = calendarData.date
            dayTV.text = calendarData.day


            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, calendarItemData[layoutPosition]!!.date.toInt())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarAdapter.CalendarViewHolder {
        context = parent.context
        binding = CalendarCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarAdapter.CalendarViewHolder, position: Int) {
        holder.bind(calendarItemData[holder.adapterPosition]!!, holder.adapterPosition)
        if (crDate != 0) {

        }


        if (selectedPostion == holder.adapterPosition) {
            holder.containerLL.setBackgroundColor(Color.BLUE)
        } else {
            holder.containerLL.setBackgroundColor(Color.TRANSPARENT)
        }
        holder.containerLL.setOnClickListener {
            oldSelectedPostion = selectedPostion
            selectedPostion = holder.adapterPosition

            notifyItemChanged(oldSelectedPostion)
            notifyItemChanged(selectedPostion)
        }

        /*binding.homeRemoveIv.setOnClickListener {
            val builder : AlertDialog.Builder = AlertDialog.Builder(context)
            val ad : AlertDialog = builder.create()
            var deleteData = pillItemData[holder.adapterPosition]!!.pillName
            builder.setTitle(deleteData)
            builder.setMessage("정말로 삭제하시겠습니까?")
            builder.setNegativeButton("예",
                DialogInterface.OnClickListener { dialog, which ->
                    ad.dismiss()
                    //temp = listData[holder.adapterPosition]!!
                    //extraditeData()
                    //testData.add(temp)
                    //deleteServerData = tempServerData[holder.adapterPosition]!!.api_id
                    removeData(holder.adapterPosition)
                    //removeServerData(deleteServerData!!)
                    //println(deleteServerData)
                })

            builder.setPositiveButton("아니오",
                DialogInterface.OnClickListener { dialog, which ->
                    ad.dismiss()
                })
            builder.show()
        }*/
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