import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;

import java.util.Scanner;
import java.util.List;

public class test {
    public static void addConditionally(Document doc, Element element, String field, String attribute) {
        if (element.hasAttribute(attribute)) {
            doc.add(new Field(field, element.getAttribute(attribute), TextField.TYPE_STORED));
            doc.add(new Field("All", element.getAttribute(attribute), TextField.TYPE_NOT_STORED));
        }
    }

    public static void addDocuments(IndexWriter iwriter) throws Exception {
        for (int n = 1; n <= 100000; n++) {
            String filePath = String.format("./dataset/docs/%d.xml", n);
            File docFile = new File(filePath);
            if (docFile.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setValidating(false);
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = new Document();

                BufferedReader reader = new BufferedReader(new FileReader(docFile));
                String line = reader.readLine();
                while (line != null) {
                    org.w3c.dom.Document xmlDocument = dBuilder.parse(new InputSource(new StringReader(line)));
                    NodeList nodeList = xmlDocument.getElementsByTagName("*");
                    Node node = nodeList.item(0);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        if (element.hasAttribute("ParentId")) {
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id"), "Body");
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id")+" Creation Date", "CreationDate");
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id")+"Last Edit Date", "LastEditDate");
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id")+"Last Editor", "LastEditorDisplayName");
                        }
                        else {
                            addConditionally(doc, element, "Question", "Body");
                            addConditionally(doc, element, "Title", "Title");
                            addConditionally(doc, element, "Question Creation Date", "CreationDate");
                            addConditionally(doc, element, "Question Last Edit Date", "LastEditDate");
                            addConditionally(doc, element, "Question Last Editor", "LastEditorDisplayName");
                            addConditionally(doc, element, "Question Tags", "Tags");
                        }
                    }
                    line = reader.readLine();
                }
                reader.close();
                iwriter.addDocument(doc);
            }
        }
    }
    
    public static void main(String[] args) throws IOException, ParseException, Exception {

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

        addDocuments(iwriter);
        //iwriter.addDocument(doc);
        iwriter.close(); // Closes all open resources and releases the write lock.

        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory); // deze klasse kan indexes lezen in een Directory
        IndexSearcher isearcher = new IndexSearcher(ireader); // Implements search over a single IndexReader  (DirectoryReader is een subklasse van IndexReader)
        // Applications usually need only call the inherited search(Query, int) method. For performance reasons, if your index is unchanging,
        // you should share a single IndexSearcher instance across multiple searches instead of creating a new one per-search.

        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("All", analyzer); // parset queries

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
                    System.out.println("Document" + Integer.toString(i) + ": " + hitDoc.get("Title") + " [" + hitDoc.get("Question Creation Date") + "]");
                    /*List<IndexableField> fields = hitDoc.getFields();
                    for (IndexableField field : fields) {
                        System.out.println(field.name()+": "+hitDoc.get(field.name()));
                    }*/
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
