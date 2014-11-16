package km.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static Date parse(String dateStr, String format) {
        DateFormat df = new SimpleDateFormat(format);
        try {
            Date date = df.parse(dateStr);
            return date;
        } catch (ParseException pe) {
            return null;
        }
    }

    public static String format(Date date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        String dateStr = df.format(date);
        return dateStr;
    }
    
    public static Date addMonth(Date date, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONDAY, num);
        return c.getTime();
    }
    
    public static Date addDay(Date date, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR, num);
        return c.getTime();
    }
}
