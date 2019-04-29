package vng.zalo.tdtai.zalo.zalo.utils;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    private static String TAG = Utils.class.getSimpleName();
    public static void formatTextOnOneLine(final TextView tv){
        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(tv.getLayout().getLineCount() > 1){
                    int end = Math.max(tv.getLayout().getLineEnd(0)-3,0);
                    tv.setText(String.format("%s...",tv.getText().subSequence(0,end)));
                }
            }
        });
    }

    public static String getTimeDiffOrFormatTime(Date date){
        Date curDate = new Date();
        long diffByMillisecond = timeDiffInMillisecond(date, curDate);
        if(diffByMillisecond <= Constants.SEVEN_DAYS_IN_MILLISECOND){
            if(diffByMillisecond < Constants.ONE_MIN_IN_MILLISECOND){
                return Constants.JUST_NOW;
            } else if(diffByMillisecond < Constants.ONE_HOUR_IN_MILLISECOND){
                return formatTime(diffByMillisecond/(long)Constants.ONE_MIN_IN_MILLISECOND, Constants.MINUTE);
            } else if(diffByMillisecond < Constants.ONE_DAY_IN_MILLISECOND){
                return formatTime(diffByMillisecond/(long)Constants.ONE_HOUR_IN_MILLISECOND, Constants.HOUR);
            } else {
                return formatTime(diffByMillisecond/(long)Constants.ONE_DAY_IN_MILLISECOND, Constants.DAY);
            }
        } else {
            return SimpleDateFormat.getDateTimeInstance().format(date);
        }
    }

    private static String formatTime(long num, String unit){
        return num + " " +unit;
    }

    public static long timeDiffInMillisecond(Date start, Date end){
        return end.getTime() - start.getTime();
    }

    public static boolean areInSameDay(Date date1, Date date2){
        String formatDate1 = SimpleDateFormat.getDateInstance().format(date1);
        String formatDate2 = SimpleDateFormat.getDateInstance().format(date2);
        return formatDate1.equals(formatDate2);
    }
}