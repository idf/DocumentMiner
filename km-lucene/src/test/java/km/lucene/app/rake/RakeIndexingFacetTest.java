package km.lucene.app.rake;

import io.deepreader.java.commons.util.Displayer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RakeIndexingFacetTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGrouping() throws Exception {
        Stream<Integer> nums = Stream.of(1, 1, 1, 2, 3, 3, 4);
        // list to map by index
        int[] ind = {0};  // capture array, effectively final
        class Pair {
            int left;
            int right;

            public Pair(int left, int right) {
                this.left = left;
                this.right = right;
            }
        }

        Map<Integer, List<Integer>> map = nums.map(e -> new Pair(ind[0]++, e))
                .collect(Collectors.groupingBy(e -> e.right))
                .entrySet().parallelStream()
                .collect(Collectors.toConcurrentMap(
                        Map.Entry::getKey,
                        e -> e.getValue().parallelStream().map(ee -> ee.left).collect(Collectors.toList())
                ));
    }

    @Test
    public void testGrouping2() throws Exception {
        Stream<Integer> nums = Stream.of(1, 1, 1, 2, 3, 3, 4);
        List<Integer> list = nums.collect(Collectors.toList());
        //Grouping process
        Map<Integer, List<Integer>> result =
                IntStream.range(0, list.size())
                        .boxed()
                        .collect(Collectors.groupingBy(list::get,
                                Collectors.mapping(element -> element,
                                        Collectors.toList())
                                )
                        );
        System.out.println(Displayer.display(result));
    }
}