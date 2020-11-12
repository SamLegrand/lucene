// Based on analyzer reference code

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;

public class CustomAnalyzer extends Analyzer {
 
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        TokenStream result = src;
        return new TokenStreamComponents(src, result);
    }
}