package km.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
	public static double round(double num, int digits) {
		BigDecimal bd = new BigDecimal(num).setScale(digits, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
