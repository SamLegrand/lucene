import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;

public class IndexGen {
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
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id")+" Text", "Body");
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id")+" Creation Date", "CreationDate");
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id")+" Last Edit Date", "LastEditDate");
                            addConditionally(doc, element, "Comment"+element.getAttribute("Id")+" Last Editor", "LastEditorDisplayName");
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
    
    // Based on lucene reference code
    public static void main(String[] args) throws IOException, ParseException, Exception {
        System.out.println("Loading documents, please wait...");

        //Analyzer analyzer = new CustomAnalyzer(); // Alternative analyzer for case-sensitive search
        Analyzer analyzer = new StandardAnalyzer(); // An Analyzer builds TokenStreams, which analyze text. It thus represents a policy for extracting index terms from text.

        Path indexPath = Paths.get("./index"); // Lucene index wordt opgeslagen in ./index
        Files.createDirectory(indexPath);
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
    }
}
