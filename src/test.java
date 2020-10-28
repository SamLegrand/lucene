import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import java.util.Scanner;

public class test {
    public static void main(String[] args) throws IOException, ParseException {

        System.out.println("Hello, World!");
        System.out.println("CL Arguments: "+Integer.toString(args.length));
        for(int i = 0; i < args.length; i++)
        {
            System.out.println(Integer.toString(i) + " : " + args[i]);
        }

        Analyzer analyzer = new StandardAnalyzer(); // An Analyzer builds TokenStreams, which analyze text. It thus represents a policy for extracting index terms from text.

        Path indexPath = Files.createTempDirectory("tempIndex"); // dit staat in %temp%/tempIndexNNNNNN/  met NNNNNN veel getalletjes
        Directory directory = FSDirectory.open(indexPath);
        // Directory is een klasse van Lucene, Path is van Java zelf - op het internet vond ik ook RAMDirectory maar is deprecated blijkbaar

        IndexWriterConfig config = new IndexWriterConfig(analyzer); // bevat configuration data voor een IndexWriter
        IndexWriter iwriter = new IndexWriter(directory, config);
        // indexwriter: creates and maintains an index

        // testdocument toevoegen aan de indexwriter
        Document doc = new Document();
        String text = "This is the text to be indexed.";
        doc.add(new Field("fieldname", text, TextField.TYPE_STORED)); // er is ook een TYPE_NOT_STORED "Indexed, tokenized, not stored"

        iwriter.addDocument(doc);
        iwriter.close(); // Closes all open resources and releases the write lock.

        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory); // deze klasse kan indexes lezen in een Directory
        IndexSearcher isearcher = new IndexSearcher(ireader); // Implements search over a single IndexReader  (DirectoryReader is een subklasse van IndexReader)
        // Applications usually need only call the inherited search(Query, int) method. For performance reasons, if your index is unchanging,
        // you should share a single IndexSearcher instance across multiple searches instead of creating a new one per-search.

        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("fieldname", analyzer); // parset queries

        Scanner in = new Scanner(System.in); // voor input

        while (true)
        {
            System.out.println("Give query (or \"q\" to stop)");
            String s = in.next();
            if (s.equals("q"))   // s == "q" werkt niet :)
            {
                break;
            }

            Query query = parser.parse(s); // query waarop we zoeken

            ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs; // eerste 10 resultaten

            // Iterate through the results:
            System.out.println("Results found: "+Integer.toString(hits.length));
            if (hits.length > 0)
            {
                System.out.println("Query results:");
                for (int i = 0; i < hits.length; i++)
                {
                    Document hitDoc = isearcher.doc(hits[i].doc);
                    System.out.println(Integer.toString(i)+": "+hitDoc.get("fieldname"));
                }
            }
            System.out.println("");
        }

        in.close();

        ireader.close(); // Closes files associated with this index.    komt van superklasse IndexReader
        directory.close(); // Closes the directory.
        IOUtils.rm(indexPath); // temp foldertje verwijderen
    }
}
