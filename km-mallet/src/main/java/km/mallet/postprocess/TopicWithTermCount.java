package km.mallet.postprocess;

public class TopicWithTermCount implements Comparable<TopicWithTermCount> {

    private int id;
    private int count;

    public TopicWithTermCount(int id, int count) {
        this.id = id;
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(TopicWithTermCount t) {
        if (this.count >= t.count) {
            return -1;
        } else {
            return 1;
        }
    }
}
