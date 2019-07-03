package vng.zalo.tdtai.zalo.zalo.utils

import android.widget.TextView

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.max

object Utils {
    private val TAG = Utils::class.java.simpleName

    fun formatTextOnNumberOfLines(tv: TextView, lineNum: Int) {
        tv.viewTreeObserver.addOnGlobalLayoutListener {
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
}