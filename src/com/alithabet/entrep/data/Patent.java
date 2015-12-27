package com.alithabet.entrep.data;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Patent is a data object to store patent information read from the
 * <a href="http://www.google.com/googlebooks/uspto-patents-assignments.html">
 * Google USPTO Bulk Downloads database</a>. The patent data is stored
 * in XML format. The XML fields are described in
 * <a href="http://storage.googleapis.com/patents/docs/PADX-File-Description-v2.doc">
 * http://storage.googleapis.com/patents/docs/PADX-File-Description-v2.doc</a>.
 * Each XML file contains a list of patents that were modified in any
 * way during the recorded day of the XML file. The Patent class
 * stores information of a single patent. The full patent list
 * from a given XML fine is stored using class
 * {@link PatentArray}
 *
 * @author Ali K Thabet
 */
public class Patent {

    // helper to parse date strings
    public static final SimpleDateFormat SAVE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat DISPLAY_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");

    // XML tag names used for parsing
    public static final String PATENT_ARRAY = "patent-assignment";
    public static final String DATE_ARRAY = "transaction-date";
    public static final String DATE_ELEMENT = "date";
    public static final String ASSIGNMENT_RECORD = "assignment-record";
    public static final String CORRESPONDENT = "correspondent";
    public static final String NAME = "name";
    public static final String ADDRESS = "address-";
    public static final String CITY = "city";
    public static final String COUNTRY = "country-name";
    public static final String POSTCODE = "postcode";
    public static final String ASSIGNORS_ARRAY = "patent-assignor";
    public static final String ASSIGNEE_ARRAY = "patent-assignee";
    public static final String PROPERTY_ARRAY = "patent-property";
    public static final String DOCUMENT_ID_ARRAY = "document-id";
    public static final String INVENTION_TITLE = "invention-title";
    public static final String DOCUMENT_NUMBER = "doc-number";
    public static final String KIND = "kind";

    // number of address lines for each type of person
    public static final int CORRESPONDENT_ADDRESS = 4;
    public static final int ASSIGNOR_ADDRESS = 2;
    public static final int ASSIGNEE_ADDRESS = 2;

    // Attributes of each patent. Note that each patent entry
    // has many inventions attached to it. These inventions are
    // stored in an a Map, where the unique key is the
    // invention title
    private Person                     correspondent;
    private ArrayList<Person>          patentAssignors;
    private ArrayList<Person>          patentAssignees;
    private HashMap<String,
            ArrayList<PatentProperty>> inventions;
    private Date                       recordedDate;

    // default no argument constructor
    public Patent() {
        initialize();
    }

    /**
     * Constructor with Node as argument.
     * It uses the helper function <em>readXML</em>
     * to populate all the elements of the patent object
     *
     * @param node Node element containing single patent data
     */
    public Patent(Node node, Date date) {
        initialize();
        recordedDate = date;
        readXML(node);
    }

    // Initialize variables
    private void initialize() {
        correspondent    = new Person();
        patentAssignors  = new ArrayList<>();
        patentAssignees  = new ArrayList<>();
        inventions       = new HashMap<>();
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
     * Given a node from an XML {@link org.w3c.dom.Document},
     * parse all the patent data and store it
     * in the Patent object
     *
     * @param node Node element containing single patent data
     */
    private void readXML(Node node) {

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

            // check that there is an invention
            if (((Element) properties.item(k)).getElementsByTagName(INVENTION_TITLE).getLength() <= 0) continue;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
//        builder.append("Invention Title:\n" + title + "\n");
        builder.append("Last Modified:\n");
        builder.append(DISPLAY_FORMATTER.format(recordedDate) + "\n");

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
}
