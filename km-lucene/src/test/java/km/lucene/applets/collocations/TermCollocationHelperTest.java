package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.Displayer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TermCollocationHelperTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testFilterCollocationCount() throws Exception {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        Map<String, Integer> collect = map.entrySet()
                .parallelStream()
                .filter(e -> e.getValue() > 1)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

        System.out.println(Displayer.display(collect));


        class Counter {
            int cnt;
            public Counter(int cnt) {
                this.cnt = cnt;
            }
        }

        Map<String, Counter> map1 = new HashMap<>();
        map1.put("1", new Counter(1));
        map1.put("2", new Counter(2));
        map1 = map1.entrySet()
                .parallelStream()
                .filter(e->e.getValue().cnt>1)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println(map1);
    }
}