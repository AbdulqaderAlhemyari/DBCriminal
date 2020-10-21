package com.example.dbacriminal

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_TIME = "time"
class TimePickerFragment : DialogFragment(){


    interface Callbacks{
        fun onTimeSelected(time: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val time = arguments?.getSerializable(ARG_TIME) as Date
        val calendar = Calendar.getInstance()
        calendar.time = time
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timeListener =
            TimePickerDialog.OnTimeSetListener { view: TimePicker, hourOfDay: Int, minute: Int ->
                val calendar1 = Calendar.getInstance()
                calendar1.time = time
                calendar1.set(Calendar.SECOND, 0)
                calendar1.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar1.set(Calendar.MINUTE, minute)
                val resultTime = calendar1.time as Date
                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onTimeSelected(resultTime)
                }
            }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            hour,
            minute,
            true

        )
    }

    companion object{
        fun newInstance(time: Date) : TimePickerFragment{
            val args = Bundle().apply{
                putSerializable(ARG_TIME, time)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }

}