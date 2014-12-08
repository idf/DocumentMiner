package km.lucene.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;

/*
 * StandardAnalyzer is Lucene's most sophisticated core analyzer.
 * It has quite a bit of logic to identify certain kinds of tokens,
 * such as company names, email addresses, and hostnames.
 * It also lowercaes each token and
 * removes stop words and punctuation.
 * CustomAnalyzer = StandardAnalyzer + PorterStemFilter
 */
public class CustomAnalyzer extends StopwordAnalyzerBase {

    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
    public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

    public CustomAnalyzer(Version matchVersion, CharArraySet stopWords) {
        super(matchVersion, stopWords);
    }

    public CustomAnalyzer(Version matchVersion) {
        this(matchVersion, STOP_WORDS_SET);
    }

    public CustomAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
        this(matchVersion, loadStopwordSet(stopwords, matchVersion));
    }

    public void setMaxTokenLength(int length) {
        maxTokenLength = length;
    }

    public int getMaxTokenLength() {
        return maxTokenLength;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
        src.setMaxTokenLength(maxTokenLength);
        TokenStream tok = new StandardFilter(matchVersion, src);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok, stopwords);
        // tok = new PorterStemFilter(tok);
        tok = new KStemFilter(tok);  // got get nus rather than nu
        tok = new ShingleFilter(tok, 5, 10);

        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) throws IOException {
                src.setMaxTokenLength(CustomAnalyzer.this.maxTokenLength);
                super.setReader(reader);
            }
        };
    }
}
