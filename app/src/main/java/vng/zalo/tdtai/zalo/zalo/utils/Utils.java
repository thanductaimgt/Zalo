package vng.zalo.tdtai.zalo.zalo.utils;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    private static String TAG = Utils.class.getSimpleName();

    public static void formatTextOnNumberOfLines(final TextView tv, int lineNum) {
        tv.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (tv.getLayout().getLineCount() > lineNum) {
                // end is offset of last character
                int end = Math.max(tv.getLayout().getLineEnd(lineNum - 1) - 3, 0);
                tv.setText(String.format("%s...", tv.getText().subSequence(0, end)));
            }
        });
    }

    public static String getTimeDiffOrFormatTime(Date date) {
        Date curDate = new Date();
        long diffByMillisecond = timeGapInMillisecond(date, curDate);
        if (diffByMillisecond <= Constants.SEVEN_DAYS_IN_MILLISECOND) {
            if (diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND) {
                return Constants.JUST_NOW;
            } else if (diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND) {
                return formatTime(diffByMillisecond / (long) Constants.ONE_MIN_IN_MILLISECOND, Constants.MINUTE);
            } else if (diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND) {
                return formatTime(diffByMillisecond / (long) Constants.ONE_HOUR_IN_MILLISECOND, Constants.HOUR);
            } else {
                return formatTime(diffByMillisecond / (long) Constants.ONE_DAY_IN_MILLISECOND, Constants.DAY);
            }
        } else {
            return SimpleDateFormat.getDateTimeInstance().format(date);
        }
    }

    private static String formatTime(long num, String unit) {
        return num + " " + unit;
    }

    public static long timeGapInMillisecond(Date start, Date end) {
        return end.getTime() - start.getTime();
    }

    public static boolean areInDifferentDay(Date date1, Date date2) {
        String formatDate1 = SimpleDateFormat.getDateInstance().format(date1);
        String formatDate2 = SimpleDateFormat.getDateInstance().format(date2);
        return !formatDate1.equals(formatDate2);
    }
}