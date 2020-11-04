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
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import java.awt.Desktop;

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

    public static void printHelp()
    {
        System.out.println("Available commands:");
        System.out.println("  help             shows this menu");
        System.out.println("  search {query}   search on a given query");
        System.out.println("  pagesize {n}     selects the number of results shown per page (default: 10)");
        System.out.println("  next             go to the next page of results");
        System.out.println("  prev             go to the previous page of results");
        System.out.println("  seek {n}         go to the page containing result n");
        System.out.println("  view             show current results again");
        System.out.println("  select {n}       show result n in detail");
        System.out.println("  quit             exits the program");
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

        printHelp();
        System.out.println("");

        int pagesize = 10;
        int current_page = 0;
        ScoreDoc[] hits = {};

        while (true)
        {
            System.out.println("Enter a command");
            String input_str = in.nextLine();
            String[] parts = input_str.split(" "); // split on space

            //System.out.println(parts.length);
            //System.out.println(parts);

            if (parts.length == 0)
            {
                System.out.println("Empty command: invalid. Please try again.");
                continue;
            }

            // geen switch case want dan kan ik geen break erin doen om uit de while loop te geraken
            if (parts[0].equals("help"))
            {
                printHelp();
                continue;
            }
            else if (parts[0].equals("quit"))
            {
                break;
            }
            else if (parts[0].equals("search"))
            {
                if (parts.length <= 1)
                {
                    // "search" zonder query
                    System.out.println("Can't search on an empty query. Please try again.");
                    continue;
                }

                Query query = parser.parse(input_str.substring(7)); // query waarop we zoeken: "search " is 7 tekentjes, die knippen we eraf vanvoor
                hits = isearcher.search(query, 1000).scoreDocs; // max 1000 resultaten
                current_page = 0; // toont pagesize*current_page tot pagesize*(current_page+1)-1
                if (hits.length == 0)
                {
                    System.out.println("No results were found for the given query. Please try again.");
                }
            }
            else if (parts[0].equals("next"))
            {
                if (hits.length == 0)
                {
                    System.out.println("Can't go to next page: no results.");
                    continue;
                }
                int next_page_first_index = (current_page+1)*pagesize;
                if (next_page_first_index >= hits.length)
                {
                    System.out.println("Can't go to next page: no more results available.");
                    continue;
                }

                current_page++;
            }
            else if (parts[0].equals("prev"))
            {
                if (hits.length == 0)
                {
                    System.out.println("Can't go to previous page: no results.");
                    continue;
                }

                if (current_page <= 0)
                {
                    System.out.println("Can't go to previous page: this is the first page.");
                    continue;
                }

                current_page--;
            }
            else if (parts[0].equals("pagesize"))
            {
                if (parts.length != 2)
                {
                    System.out.println("Invalid use: please give a number (default: pagesize 10)");
                    continue;
                }

                int new_size = 0;
                try
                {
                    new_size = Integer.parseInt(parts[1]);
                }
                catch (Exception e)
                {
                    System.out.println("Error parsing number \""+parts[1]+"\".");
                    System.out.println("Page size left unchanged.");
                    continue;
                }

                if (new_size < 1)
                {
                    System.out.println("Can't use a page size of less than 1!");
                    continue;
                }

                // ook paginanummer veranderen: eerste # van huidige vorige pagina moeten we nu hebben
                int search_index = pagesize*current_page;

                pagesize = new_size;
                current_page = search_index / pagesize;
                // kan dit een current_page geven die te hoog is?
                assert pagesize*current_page < hits.length;
            }
            else if (parts[0].equals("seek"))
            {
                if (parts.length != 2)
                {
                    System.out.println("Invalid use: please give the result to be jumped to (example: seek 25 to jump to the page with result 25)");
                    continue;
                }

                if (hits.length == 0)
                {
                    // duidelijker errorbericht als er nog geen resultaten zijn
                    System.out.println("Can't jump to given result: no results.");
                    continue;
                }

                int seek_index = 0;
                try
                {
                    seek_index = Integer.parseInt(parts[1]);
                }
                catch (Exception e)
                {
                    System.out.println("Error parsing number \""+parts[1]+"\".");
                    System.out.println("Could not seek to given result.");
                    continue;
                }

                if ((seek_index < 1) | (seek_index > hits.length))
                {
                    System.out.println("Error: invalid index.");
                    continue;
                }

                seek_index--; // tussen 0 en hits.length-1
                current_page = seek_index / pagesize;
                // kan dit een current_page geven die te hoog is?
                assert pagesize*current_page < hits.length;
            }
            else if (parts[0].equals("select"))
            {
                if (parts.length != 2)
                {
                    System.out.println("Invalid use: please give the result to be printed in detail.");
                    continue;
                }

                if (hits.length == 0)
                {
                    // duidelijker errorbericht als er nog geen resultaten zijn
                    System.out.println("Can't select given result: no results.");
                    continue;
                }

                int select_index = 0;
                try
                {
                    select_index = Integer.parseInt(parts[1]);
                }
                catch (Exception e)
                {
                    System.out.println("Error parsing number \""+parts[1]+"\".");
                    continue;
                }

                if ((select_index < 1) | (select_index > hits.length))
                {
                    System.out.println("Error: invalid index.");
                    continue;
                }

                select_index--; // tussen 0 en hits.length-1
                Document hitDoc = isearcher.doc(hits[select_index].doc);
                List<IndexableField> fields = hitDoc.getFields();
                List<String> fields_str = new ArrayList<>();
                for(IndexableField field: fields)
                {
                    fields_str.add(field.name());
                    //System.out.println("field: \""+field.name()+"\"");
                }

                PrintWriter writer = new PrintWriter("result.html", "UTF-8");

                // title
                String title_str = "<h1>";
                title_str += hitDoc.get("Title");
                if (fields_str.contains("Question Creation Date"))
                {
                    title_str += " [created "+hitDoc.get("Question Creation Date")+"]";
                }
                title_str += "</h1>";
                writer.println(title_str);

                if (fields_str.contains("Question Last Editor"))
                {
                    writer.println("<h2> last edit by " + hitDoc.get("Question Last Editor")+" on "+hitDoc.get("Question Last Edit Date")+"</h2>");
                }
                if (fields_str.contains("Question Tags"))
                {
                    writer.println("<h2> tags: "+hitDoc.get("Question Tags")+"</h2>");
                }

                writer.println(hitDoc.get("Question"));

                writer.println("<h1>Comments</h1>");

                // zoeken naar Comments***** Text en dan die comments toevoegen aan de html
                for (String fieldname: fields_str)
                {
                    if (fieldname.length() < 7) continue; // zoeken enkel naar Comment*****
                    if (fieldname.substring(0, 7).equals("Comment") && fieldname.contains("Text"))
                    {
                        //writer.println("<h3>Comment</h3>");
                        String base_field = fieldname.split(" ")[0]; // Comment12345 zonder de Text
                        if (fields_str.contains(base_field+" Creation Date"))
                        {
                            writer.println("<b>created "+hitDoc.get(base_field+" Creation Date")+"</b>");
                        }
                        else
                        {
                            writer.println("<b>created at an unknown time</b>");
                        }

                        if (fields_str.contains(base_field+" Last Editor"))
                        {
                            // als er last editor instaat zal er ook wel last edit date instaan?
                            writer.println("last edit by " + hitDoc.get(base_field+" Last Editor")+" on "+hitDoc.get(base_field+" Last Edit Date"));
                        }
                        writer.println(hitDoc.get(base_field+" Text"));
                        writer.println("<hr>");
                        
                    }
                }
                
                //

                writer.close();

                try
                {
                    File f = new File("result.html");
                    Desktop.getDesktop().open(f);
                }
                catch (Exception e)
                {
                    System.out.println("Could not open result.html in your default browser. You may open the file manually.");
                }

                continue;
            }
            else if (parts[0].equals("view"))
            {
                if (hits.length == 0)
                {
                    System.out.println("Can't view results: no results.");
                    continue;
                }
                // niks doen, gewoon geen continue. code hieronder print al
            }
            else
            {
                System.out.println("Unrecognized command \""+parts[0]+"\". Please try again or type help for a list of commands.");
                continue;
            }

            // Iterate through the results:
            if (hits.length > 0)
            {
                System.out.println("Showing results from page "+Integer.toString(current_page+1)+": (total results: "+Integer.toString(hits.length)+")");
                for(int i = pagesize*current_page; (i < (pagesize*(current_page+1))) && (i < hits.length); i++)
                {
                    Document hitDoc = isearcher.doc(hits[i].doc);
                    System.out.println(String.format("% 4d", i+1)+": " + hitDoc.get("Title") + " [" + hitDoc.get("Question Creation Date") + "]");
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
