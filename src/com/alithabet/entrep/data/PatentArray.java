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
    private ArrayList<Patent> patents;      // list of patents
    private Date              recordedDate; // recorded date

    // default no argument constructor
    public PatentArray() {
        initialize();
    }

    /**
     * Constructor with xml file name as input.
     * To initialize the PatentArray object, we
     * read all the patents from <em>xmlFileName</em>
     * and populate the <em>patents</em> array, and
     * the corresponding <em>recordedDate</em>
     * of the bulk file
     *
     * @param xmlFileName name of XML file containing patent information
     */
    public PatentArray(String xmlFileName) {
        initialize();
        bulkRead(xmlFileName);
    }

    // Initialize variables
    private void initialize() {
        patents      = new ArrayList<>();
        recordedDate = new Date();
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
                Patent patent = new Patent(nList.item(i));
                System.out.print(patent.toString());
                patents.add(patent);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception reading file " + fileName
                    + " " + e.getMessage());
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
        final String fileName = "ad20150209.xml";
        PatentArray patents = new PatentArray(fileName);
        // now create a csv file
        String[] names = fileName.split("\\."); // get the file name to use for CSV file
        patents.patentArrayToCSV(names[0]);
    }
}
