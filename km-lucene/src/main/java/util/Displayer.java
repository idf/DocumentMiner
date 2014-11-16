package util;

import java.lang.reflect.Field;

/**
 * “Measuring programming progress by lines of code is like measuring aircraft building progress by weight.”
 * - Bill Gates
 * User: Danyang
 * Date: 11/16/14
 * Time: 3:39 PM
 */
public class Displayer {
    public <E> String display(Object obj, E attribute) {
        for(Field field : obj.getClass().getFields()) {
            try {
                if(field.get(this).equals(attribute)) {
                    return field.getName()+": "+attribute.toString();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return "Error accessing attribute";
    }
}
