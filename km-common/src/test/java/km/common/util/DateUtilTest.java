package km.common.util;

import io.deepreader.java.commons.util.DateUtils;
import org.junit.*;

import java.util.Calendar;
import java.util.Date;

public class DateUtilTest {

    public DateUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParse() {
        System.out.println("testParse");
        String dateStr = "Apr 2010";
        String format = "MMM yyyy";
        Date date = DateUtils.parse(dateStr, format);
        System.out.println(date);
    }
    
    @Test
    public void testAddMonth() {
        System.out.println("testAddMonth");
        Calendar c = Calendar.getInstance();
        c.set(2014, 11, 1);
        Date date1 = c.getTime();
        Date date2 = DateUtils.addMonth(date1, 1);
        System.out.println(date1 + " - " + date2);
    }
    
    @Test
    public void testAddDay() {
        System.out.println("testAddDay");
        Calendar c = Calendar.getInstance();
        c.set(2014, 11, 31);
        Date date1 = c.getTime();
        Date date2 = DateUtils.addDay(date1, 1);
        System.out.println(date1 + " - " + date2);
    }
}