package util;

import java.sql.Timestamp;
import java.util.Date;

/**
 * “Measuring programming progress by lines of code is like measuring aircraft building progress by weight.”
 * - Bill Gates
 * User: Danyang
 * Date: 11/21/14
 * Time: 2:13 PM
 */
public class Timestamper {
    long start;
    long end;
    public void start() {
        this.start = new Date().getTime();
        System.out.println(new Timestamp(this.start));
    }
    public void end() {
        this.end = new Date().getTime();
        System.out.println(new Timestamp(this.end));
        System.out.println("Elapsed: "+(this.end-this.start)+"ms");
    }

}
