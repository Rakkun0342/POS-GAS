package com.example.adminkogas.utils

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object Utils {

    fun formatRupiah():NumberFormat{
        val localeID = Locale("in", "ID")
        return NumberFormat.getCurrencyInstance(localeID)
    }

    fun getCurrentDate(format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun showText(context: Context, text: String){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormaterDate(date: String, format: String): String{
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateTicket = LocalDateTime.parse(date, formatter)
        return dateTicket.format(DateTimeFormatter.ofPattern(format))
    }

}