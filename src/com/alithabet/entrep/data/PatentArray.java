package com.alithabet.entrep.data;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * PatentArray is a data object to store patent information read from the
 * <a href="http://www.google.com/googlebooks/uspto-patents-assignments.html">
 * Google USPTO Bulk Downloads database</a>. PatentArray reads data from an
 * XML file from the Google USPTO Bulk Downloads and stores all the relevant
 * information into an array <em>patents</em> of {@link Patent} objects.
 * Each XML file has a recorded date attribute, which PatentArray stores
 * in <em>recordedDate</em>
 *
 * @author Ali K Thabet
 */
public class PatentArray {
    private ArrayList<Patent>              patents;      // list of patents
    private Date                           recordedDate; // recorded date
    private Map<String, ArrayList<Patent>> map; // map of string occurance to patents

    // default no argument constructor
    public PatentArray() {
        initialize();
    }

    /**
     * Constructor with xml file name as input.
     * To initialize the PatentArray object, we
     * read all the patents from <em>fileName</em>
     * and populate the <em>patents</em> array, and
     * the corresponding <em>recordedDate</em>
     * of the bulk file. The input can alternatively
     * be a folder name containing XML files. In the
     * folder case, all the patents are red from
     * each XML file and stored in the patent array.
     *
     * @param fileName name of XML file (or folder of XML files) containing patent information
     */
    public PatentArray(String fileName) {
        initialize();

        // if we get a directory then read all XML files in it
        File folder = new File(fileName);
        if (folder.isDirectory()) {
            folderBulkRead(fileName);
        } else {
            bulkRead(fileName);
        }
    }

    // Initialize variables
    private void initialize() {
        patents      = new ArrayList<>();
        recordedDate = new Date();
        map          = new TreeMap<>();
    }

    /**
     * Bulk read all patents in XML file inside <em>folderName</em>
     *
     * @param folderName folder containing XML patent files
     */
    private void folderBulkRead(String folderName) {
        File folder = new File(folderName);

        File[] filesList = folder.listFiles();
        if (filesList != null) {
            System.out.println("Reading " + filesList.length + " files in folder " + folderName);
            int count = 1;
            for (File file : filesList) {
                System.out.print(count++ + " ");
                if (file.isFile()) bulkRead(file.getAbsolutePath());
            }
            System.out.print("\n");
        }
    }

    /**
     * Bulk read all patents in a given XML file
     *
     * @param fileName name of XML file with patents
     */
    private void bulkRead(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("Input to bulk read cannot be null");
        }

        // check if file is xml
        if (!fileName.endsWith(".xml")) return;

        try {
            File in = new File(fileName);
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            // first get date string and convert it to Date format
            String dateString = ((Element) doc.getElementsByTagName(Patent.DATE_ARRAY).item(0)).getElementsByTagName(Patent.DATE_ELEMENT).item(0).getTextContent();
            recordedDate = Patent.SAVE_FORMATTER.parse(dateString);
            // get the list of patent assignments
            NodeList nList = doc.getElementsByTagName(Patent.PATENT_ARRAY);

            for (int i = 0; i < nList.getLength(); i++) {
                Patent patent = new Patent(nList.item(i), recordedDate);
//                System.out.print(patent.toString());
                patents.add(patent);
            }

            // once all patents are retrieved, create index map
            addPatentsToMap();
        } catch (Exception e) {
            throw new RuntimeException("Exception reading file " + fileName
                    + " " + e.getCause());
        }
    }

    // check if a file has .xml extension
    private boolean isXML(String fileName) {
        int i = fileName.lastIndexOf('.');

        return i > 0 && fileName.substring(i + 1).equals("xml");
    }

    /**
     * The patent map is an indexing map used to find
     * the occurrence of given words in the patent
     * array. The map is used to query keywords related
     * to people affiliated with a patent. The query
     * words could be individual names or addresses.
     * The indexed words come from the patent's
     * correspondent, assignees, and assignors
     *
     */
    private void addPatentsToMap() {
        for (Patent patent : patents) {
            Person correspondent = patent.getCorrespondent();

            for (String s : correspondent.getAllStrings()) {
                addStringToMap(s, patent);
            }

            for (Person assignor : patent.getPatentAssignors()) {
                for (String s : assignor.getAllStrings()) {
                    addStringToMap(s, patent);
                }
            }

            for (Person assignee : patent.getPatentAssignees()) {
                for (String s : assignee.getAllStrings()) {
                    addStringToMap(s, patent);
                }
            }
        }
    }

    // add a patent to the index map using the string key
    private void addStringToMap(String key, Patent patent) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<Patent>());
        }

        ArrayList<Patent> list = map.get(key);
        list.add(patent);
    }

    // query the map index for a string occurrence
    public ArrayList<Patent> queryIndex(String query) {
        query = query.toLowerCase();
        if (map.containsKey(query)) {
            return map.get(query);
        } else {
            return new ArrayList<Patent>();
        }
    }

    /**
     * Create a CSV file containing all the patents
     *
     * @param fileName name of CSV file to write
     */
    private void patentArrayToCSV(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("CSV file name is null");
        }

        // check if filename ends with .csv
        // if not the add extension
        if (!fileName.endsWith(".csv")) {
            fileName = fileName + ".csv";
        }

        // create the CSV file
        try {
            FileWriter writer = new FileWriter(fileName);

            // First add headers
            writer.append("Correspondent,");
            writer.append("Assignors,");
            writer.append("Assignees,");
            writer.append("Invention Title,");
            writer.append("Application Date,");
            writer.append("Publish Date,");
            writer.append("Issue Date\n");

            for (Patent p : patents) {

                // add an entry for every invention in the patent
                HashMap<String, ArrayList<PatentProperty>> properties = p.getInventions();
                for (String invention : properties.keySet()) {
                    // get the correspondent
                    writer.append(p.getCorrespondent().getName().replace(",",""));
                    writer.append(',');

                    // all the names of patent assignors
                    StringBuilder assignors = new StringBuilder();
                    for (Person assignor : p.getPatentAssignors()) {
                        assignors.append(assignor.getName());
                        assignors.append(" - ");
                    }
                    writer.append(assignors.toString().replace(",", ""));
                    writer.append(',');

                    // all the names of patent assignees
                    StringBuilder assignees = new StringBuilder();
                    for (Person assignee : p.getPatentAssignees()) {
                        assignees.append(assignee.getName());
                        assignees.append(" - ");
                    }
                    writer.append(assignees.toString().replace(",", ""));
                    writer.append(',');

                    writer.append(invention.replace(",",""));
                    writer.append(",");

                    // now add the relevant invention dates
                    for (PatentProperty property : properties.get(invention)) {
                        writer.append(Patent.DISPLAY_FORMATTER.format(property.getDate()));
                        writer.append(",");
                    }
                    writer.append('\n');
                }
            }

            // now flush the file and close it
            writer.flush();
            writer.close();
        } catch (IOException e) {
            StringBuilder message = new StringBuilder();
            message.append("Class: ");
            message.append(e.getClass());
            message.append(" Cause: ");
            message.append(e.getCause());
            message.append(" Message: ");
            message.append(e.getMessage());
            throw new RuntimeException(message.toString());
        }


    }
    // getters and setter
    public ArrayList<Patent> getPatents() {
        return patents;
    }

    public void setPatents(ArrayList<Patent> patents) {
        this.patents = patents;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
    }

    // unit test
    public static void main(String[] args) {
        // read all patents in fileName
//        final String fileName = "test.xml";
        final String fileName = "Google USPTO\\ad20150224.xml"; //"ad20150209.xml";
        PatentArray patents = new PatentArray(fileName);

        // test query index
        String query = "university";
        for (Patent p : patents.queryIndex(query)) {
            System.out.print(p.toString());
        }
//        // now create a csv file
//        String[] names = fileName.split("\\."); // get the file name to use for CSV file
//        patents.patentArrayToCSV(names[0]);

//        String folderName = "Google USPTO";
//
//        File folder = new File(folderName);
//        for (File file : folder.listFiles()) {
//            String temp = file.getAbsolutePath();
//            int i = temp.lastIndexOf('.');
//            if (i > 0) {
//                System.out.println(temp.substring(i + 1));
//            }
//            System.out.println(file.getAbsolutePath());
//        }
//        System.out.println("End");
    }
}
