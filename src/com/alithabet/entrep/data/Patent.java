package com.alithabet.entrep.data;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Patent is a data object to store patent information read from the
 * <a href="http://www.google.com/googlebooks/uspto-patents-assignments.html">
 * Google USPTO Bulk Downloads database</a>
 *
 * @author Ali K Thabet
 */
public class Patent {

    // helper to parse date strings
    public static final SimpleDateFormat SAVE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat DISPLAY_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");

    // XML tag names used for parsing

    private static final String PATENT_ARRAY = "patent-assignment";
    private static final String DATE_ARRAY = "transaction-date";
    private static final String DATE_ELEMENT = "date";
    private static final String ASSIGNMENT_RECORD = "assignment-record";
    private static final String CORRESPONDENT = "correspondent";
    private static final String NAME = "name";
    private static final String ADDRESS = "address-";
    private static final String CITY = "city";
    private static final String COUNTRY = "country-name";
    private static final String POSTCODE = "postcode";
    private static final String ASSIGNORS_ARRAY = "patent-assignor";
    private static final String ASSIGNEE_ARRAY = "patent-assignee";
    private static final String PROPERTY_ARRAY = "patent-property";
    private static final String DOCUMENT_ID_ARRAY = "document-id";
    private static final String INVENTION_TITLE = "invention-title";
    private static final String DOCUMENT_NUMBER = "doc-number";
    private static final String KIND = "kind";

    private static final int CORRESPONDENT_ADDRESS = 4;
    private static final int ASSIGNOR_ADDRESS = 2;
    private static final int ASSIGNEE_ADDRESS = 2;

    private Person                    correspondent;
    private ArrayList<Person>         patentAssignors;
    private ArrayList<Person>         patentAssignees;
//    private ArrayList<PatentProperty> patentProperties;
    private HashMap<String, ArrayList<PatentProperty>> inventions;
    private Date recordedDate;
//    private String title;

    // default no argument constructor
    public Patent() {
        initialize();
    }

    /**
     * Constructor with XML file name as argument.
     * It uses the helper function <em>readXML</em>
     * to populate all the elements of the patent object
     *
     * @param xmlFile name of XML file
     */
    public Patent(String xmlFile, Date date, Node node) {
        initialize();

        readXML(xmlFile, node);
    }

    private void initialize() {
        correspondent    = new Person();
        patentAssignors  = new ArrayList<>();
        patentAssignees  = new ArrayList<>();
//        patentProperties = new ArrayList<>();
        inventions = new HashMap<>();
        recordedDate     = new Date();
    }

    public Person getCorrespondent() {
        return correspondent;
    }

    public void setCorrespondent(Person correspondent) {
        this.correspondent = correspondent;
    }

    public ArrayList<Person> getPatentAssignors() {
        return patentAssignors;
    }

    public void setPatentAssignors(ArrayList<Person> patentAssignors) {
        this.patentAssignors = patentAssignors;
    }

    public ArrayList<Person> getPatentAssignees() {
        return patentAssignees;
    }

    public void setPatentAssignees(ArrayList<Person> patentAssignees) {
        this.patentAssignees = patentAssignees;
    }

    public HashMap<String, ArrayList<PatentProperty>> getInventions() {
        return inventions;
    }

    public void setInventions(HashMap<String, ArrayList<PatentProperty>> inventions) {
        this.inventions = inventions;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
    }

    /**
     * Given an XML file containing patent data,
     * read all the information and store it in
     * the current object
     *
     * @param fileName name of XML file
     * @throws IllegalArgumentException for any problem with input
     */
    private void readXML(String fileName, Node node) {

        Element corr = (Element) ((Element) ((Element) node).getElementsByTagName(ASSIGNMENT_RECORD).item(0))
                .getElementsByTagName(CORRESPONDENT).item(0);
        corr = (Element) corr.getElementsByTagName(NAME).item(0);
        if (corr.getTextContent() == null) return;

        correspondent.setName(corr.getTextContent());
        ArrayList<String> address = new ArrayList<>();
        for (int j = 1; j <= CORRESPONDENT_ADDRESS; j++) {
            NodeList temp = ((Element) node).getElementsByTagName(ADDRESS + j);
            if (temp != null && temp.getLength() > 0) {
                address.add(temp.item(0).getTextContent());
            }
        }
        correspondent.setAddress(address);

        // patent assignors
        NodeList assignors = ((Element) node).getElementsByTagName(ASSIGNORS_ARRAY);
        for (int j = 0; j < assignors.getLength(); j++) {
            Person person = new Person();

            Element nameElement = (Element) ((Element) assignors.item(j)).getElementsByTagName(NAME).item(0);
            person.setName(nameElement.getTextContent());

            address.clear();
            for (int k = 1; k <= ASSIGNOR_ADDRESS; k++) {
                NodeList temp = ((Element) assignors.item(j)).getElementsByTagName(ADDRESS + k);
                if (temp != null && temp.getLength() > 0) {
                    address.add(temp.item(0).getTextContent());
                }
            }
            person.setAddress(address);
            patentAssignors.add(person);
        }

        // patent assignees
        NodeList assignees = ((Element) node).getElementsByTagName(ASSIGNEE_ARRAY);
        for (int j = 0; j < assignees.getLength(); j++) {
            Person person = new Person();
            Element root = (Element) assignees.item(j);

            person.setName(root.getElementsByTagName(NAME).item(0).getTextContent());

            address.clear();
            for (int k = 1; k <= ASSIGNEE_ADDRESS; k++) {
                NodeList temp = root.getElementsByTagName(ADDRESS + k);
                if (temp != null && temp.getLength() > 0) {
                    address.add(temp.item(0).getTextContent());
                }
            }
            person.setAddress(address);

            if (root.getElementsByTagName(CITY) != null && root.getElementsByTagName(CITY).getLength() > 0) {
                person.setCity(root.getElementsByTagName(CITY).item(0).getTextContent());
            }

            if (root.getElementsByTagName(POSTCODE) != null && root.getElementsByTagName(POSTCODE).getLength() > 0) {
                person.setPostcode(root.getElementsByTagName(POSTCODE).item(0).getTextContent());
            }

            if (root.getElementsByTagName(COUNTRY) != null && root.getElementsByTagName(COUNTRY).getLength() > 0) {
                person.setCountry(root.getElementsByTagName(COUNTRY).item(0).getTextContent());
            } else {
                person.setCountry("US");
            }
            patentAssignees.add(person);
        }

        // patent properties
        NodeList properties = ((Element) node).getElementsByTagName(PROPERTY_ARRAY);

        for (int k = 0; k < properties.getLength(); k++) {
            ArrayList<PatentProperty> patentProperties = new ArrayList<>();

            String title = ((Element) properties.item(k)).getElementsByTagName(INVENTION_TITLE).item(0).getTextContent();
            NodeList documentId = ((Element) properties.item(k)).getElementsByTagName(DOCUMENT_ID_ARRAY);
            for (int j = 0; j < documentId.getLength(); j++) {
                PatentProperty property = new PatentProperty();
                Element root = (Element) documentId.item(j);

                if (root.getElementsByTagName(COUNTRY) != null && root.getElementsByTagName(COUNTRY).getLength() > 0) {
                    property.setCountry(root.getElementsByTagName(COUNTRY).item(0).getTextContent());
                } else {
                    property.setCountry("US");
                }

                if (root.getElementsByTagName(DOCUMENT_NUMBER) != null && root.getElementsByTagName(DOCUMENT_NUMBER).getLength() > 0) {
                    property.setDocumentNumber(root.getElementsByTagName(DOCUMENT_NUMBER).item(0).getTextContent());
                }

                if (root.getElementsByTagName(KIND) != null && root.getElementsByTagName(KIND).getLength() > 0) {
                    property.setKind(root.getElementsByTagName(KIND).item(0).getTextContent());
                }

                if (root.getElementsByTagName(DATE_ELEMENT) != null && root.getElementsByTagName(DATE_ELEMENT).getLength() > 0) {
                    String dateString = root.getElementsByTagName(DATE_ELEMENT).item(0).getTextContent();

                    try {
                        property.setDate(SAVE_FORMATTER.parse(dateString));
                    } catch (Exception e) {
                        System.out.println("Property date formatter exception:"
                                + e.getMessage());
                    }
                }

                switch (property.getDocumentNumber().length()) {
                    case PatentProperty.APPLICATION_NUMBER_LENGTH:
                        property.setStatus(PatentProperty.APPLICATION_STATUS);
                        break;
                    case PatentProperty.ISSUE_NUMBER_LENGTH:
                        property.setStatus(PatentProperty.ISSUED_STATUS);
                        break;
                    case PatentProperty.PUBLICATION_NUMBER_LENGTH:
                        property.setStatus(PatentProperty.PUBLISHED_STATUS);
                        break;
                    default:
                        property.setStatus(PatentProperty.NO_STATUS);
                        break;
                }
                patentProperties.add(property);
            }
            inventions.put(title, patentProperties);
        }
    }

    /**
     * Bulk read all patents in a given XML file
     *
     * @param fileName name of XML file with patents
     * @param patents list to store resulting patents
     */
    public static void bulkRead(String fileName, ArrayList<Patent> patents) {
        if (fileName == null || patents == null) {
            throw new NullPointerException("Input to bulk read cannot be null");
        }
        try {
            File in = new File(fileName);
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            // first get date string and convert it to Date format
            String dateString = ((Element) doc.getElementsByTagName(DATE_ARRAY).item(0)).getElementsByTagName(DATE_ELEMENT).item(0).getTextContent();
            // get the list of patent assignments
            NodeList nList = doc.getElementsByTagName(PATENT_ARRAY);

            for (int i = 0; i < nList.getLength(); i++) {
                Patent patent = new Patent(fileName,
                        SAVE_FORMATTER.parse(dateString), nList.item(i));
                System.out.print(patent.toString());
                patents.add(patent);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception reading file " + fileName
                    + " " + e.getMessage());
//            System.out.println("Exception at main");
//            System.out.println(e.getMessage());
        }
    }

    public static void patentArrayToCSV(String fileName, ArrayList<Patent> patents) {
        if (fileName == null || patents == null) {
            throw new NullPointerException("Input to bulk read cannot be null");
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
//        builder.append("Invention Title:\n" + title + "\n");
        builder.append("Correspondent:\n");
        builder.append(correspondent.toString() + "\n");

        builder.append("Assignors:\n");
        for (Person p : patentAssignors) {
            builder.append(p.toString() + "\n");
        }

        builder.append("Assignees:\n");
        for (Person p : patentAssignees) {
            builder.append(p.toString() + "\n");
        }

        for (String key : inventions.keySet()) {
            builder.append("Invention Title: " + key + "\n");

            for (PatentProperty p : inventions.get(key)) {
                builder.append(p.toString());
            }
        }

        builder.append("-----------------------------------------------------------------------------------------\n");

        return builder.toString();
    }

    // unit test
    public static void main(String[] args) {
        final String fileName = "ad20150209.xml";
        ArrayList<Patent> patents = new ArrayList<>();
        Patent.bulkRead(fileName, patents); // Read all patents in file

        // now create a csv file
        String[] names = fileName.split("\\."); // get the file name to use for CSV file
        Patent.patentArrayToCSV(names[0] + ".csv", patents);
    }
}
