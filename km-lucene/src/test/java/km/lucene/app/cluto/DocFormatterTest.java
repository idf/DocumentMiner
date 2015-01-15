package km.lucene.app.cluto;

import km.common.Settings;
import km.lucene.constants.FieldName;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class DocFormatterTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void info() throws Exception {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Settings.THINDEX_PATH)));
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(FieldName.CONTENT);
        long m = terms.size();  // -1
        TermsEnum te = terms.iterator(null);
        while(te.next()!=null)
            m ++;



        System.out.println(m);
    }

    @Test
    public void testIndex2matrix() throws Exception {
        new DocFormatter().index2matrix();
    }
}