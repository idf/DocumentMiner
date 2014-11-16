package km.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Statistics recorder
 */
public class SR {

    private static Map<String, Long> startingTimes = new HashMap<String, Long>();
    private static Map<String, Long> elapsedTimes = new LinkedHashMap<String, Long>();
    private static Map<String, Integer> record = new LinkedHashMap<String, Integer>();

    public static synchronized void start(String recorder) {
        startingTimes.put(recorder, System.currentTimeMillis());
    }

    public static synchronized void stop(String recorder) {
        if (startingTimes.containsKey(recorder)) {
            long elapsed = System.currentTimeMillis() - startingTimes.get(recorder);
            if (elapsedTimes.containsKey(recorder)) {
                elapsedTimes.put(recorder, elapsedTimes.get(recorder) + elapsed);
            } else {
                elapsedTimes.put(recorder, elapsed);
            }
        } else {
            System.out.println("The recorder [" + recorder + "] is not started.");
        }
    }

    public static synchronized void set(String recorder, int value) {
        record.put(recorder, value);
    }

    public static synchronized void add(String recorder, int value) {
        if (record.containsKey(recorder)) {
            record.put(recorder, record.get(recorder) + value);
        } else {
            record.put(recorder, value);
        }
    }

    public static void report() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = df.format(new Date());

        System.out.println("+-----------------------------------------+");
        System.out.println("| Statistics report: " + date + " |");
        System.out.println("+-----------------------------------------+");
        for (Map.Entry<String, Integer> entry : record.entrySet()) {
            System.out.println(entry.getKey() + ":\t" + entry.getValue());
        }

        System.out.println("+-----------------------------------------+");
        System.out.println("| Performance report: " + date + " |");
        System.out.println("+-----------------------------------------+");
        for (Map.Entry<String, Long> entry : elapsedTimes.entrySet()) {
            System.out.println(entry.getKey() + ":\t" + entry.getValue());
        }
    }
}
