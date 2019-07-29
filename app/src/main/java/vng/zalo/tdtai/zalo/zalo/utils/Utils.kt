package vng.zalo.tdtai.zalo.zalo.utils

import android.util.Log
import android.widget.TextView
import com.google.android.gms.tasks.Task

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.max
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService



object Utils {
    fun formatTextOnNumberOfLines(tv: TextView, lineNum: Int) {
        tv.viewTreeObserver.addOnGlobalLayoutListener {
            Log.d(getTag(object {}), "tv.layout.lineCount: " + tv.layout.lineCount.toString())
            if (tv.layout.lineCount > lineNum) {
                // end is offset of last character
                val end = max(tv.layout.getLineEnd(lineNum - 1) - 3, 0)
                tv.text = String.format("%s...", tv.text.subSequence(0, end))
            }
        }
    }

    fun getTimeDiffOrFormatTime(date: Date): String {
        val curDate = Date()
        val diffByMillisecond = timeGapInMillisecond(date, curDate)
        return when {
            diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND -> Constants.JUST_NOW
            diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_MIN_IN_MILLISECOND.toLong(), Constants.MINUTE)
            diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_HOUR_IN_MILLISECOND.toLong(), Constants.HOUR)
            diffByMillisecond < Constants.SEVEN_DAYS_IN_MILLISECOND -> formatTime(diffByMillisecond / Constants.ONE_DAY_IN_MILLISECOND.toLong(), Constants.DAY)
            else -> SimpleDateFormat.getDateTimeInstance().format(date)
        }
    }

    private fun formatTime(num: Long, unit: String): String {
        return "$num $unit"
    }

    fun timeGapInMillisecond(start: Date, end: Date): Long {
        return end.time - start.time
    }

    fun areInDifferentDay(date1: Date, date2: Date): Boolean {
        val formatDate1 = SimpleDateFormat.getDateInstance().format(date1)
        val formatDate2 = SimpleDateFormat.getDateInstance().format(date2)
        return formatDate1 != formatDate2
    }

    fun getTag(obj: Any): String {
        return obj.javaClass.enclosingClass?.simpleName + "." + obj.javaClass.enclosingMethod?.name
    }

    fun assertTaskSuccessAndResultNotNull(tag: String, task: Task<*>, callback: () -> Unit) {
        if (task.isSuccessful) {
            if (task.result != null) {
                callback.invoke()
            } else {
                Log.d(tag, "task.result == null")
            }
        } else {
            Log.d(tag, "task fail")
        }
    }

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}