import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ngram.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

public class CustomAnalyzer extends Analyzer {
 
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        TokenStream result = src;
        // TokenStream result = new StopFilter(src);
        // TokenStream result = new LowerCaseFilter(src);
        // CharArraySet stop_words = new CharArraySet(0, true);
        // stop_words.add("Why");
        // TokenStream result = new StopFilter(src, stop_words);
        // result = new PorterStemFilter(result);
        // result = new CapitalizationFilter(result);
        return new TokenStreamComponents(src, result);
    }
}