package km.common.util;

public class StringUtil {

    public static String toFixLengh(String str, int len) {
        if (str.length() > len) {
            return str.subSequence(0, len - 3) + "...";
        }
        return str;
    }
    
    public static int[] toIntArray(String str, String delimiter) {
        String[] strs = str.split(delimiter);
        int[] ints = new int[strs.length];
        for (int i=0; i<strs.length; i++) {
            ints[i] = Integer.parseInt(strs[i]);
        }
        return ints;
    }
}
