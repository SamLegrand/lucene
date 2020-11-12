// Source: https://www.baeldung.com/lucene-analyzers

public class MyCustomAnalyzer extends Analyzer {
 
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        TokenStream result = new StandardFilter(src);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result,  StandardAnalyzer.STOP_WORDS_SET);
        result = new PorterStemFilter(result);
        result = new CapitalizationFilter(result);
        return new TokenStreamComponents(src, result);
    }
}