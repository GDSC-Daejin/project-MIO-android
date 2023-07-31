package com.example.mio

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class CalendarUtil {
    companion object{
        var selectedDate : LocalDate = LocalDate.now()
    }
}