package com.alithabet.entrep.data;

import java.util.Date;

/**
 * <tt>PatentProperty</tt> is a data object to store
 * information from the patent data extracted from the
 * <a href="http://www.google.com/googlebooks/uspto-patents-assignments.html">
 *     Google USPTO Bulk Downloads database</a>
 * This information corresponds to the 3 stages of
 * the patent lifecycle: Submission, Issue, and
 * Publication. Each property item contains
 * the following information:
 * <ul>
 *     <li>Country: country of origin</li>
 *     <li>Document number: Could be one of the following
 *     <ol>
 *         <li>8 digit numeric application number</li>
 *         <li>7 digit alphanumeric patent number</li>
 *         <li>10 digit numeric publication number
 *         comprised of a 4 digit year followed by a 6 digit numeric number). </li>
 *     </ol>
 *     </li>
 *     <li>Kind: 2 Position kind code</li>
 *     <li>Date: An 8 digit date in YYYYMMDD date format. Identifies either
 *     the filing-date of the patent application, the issue-date of the
 *     patent number, or the publication-date of the publication number</li>
 *     <li>Invention title: The invention title assigned to the patent property</li>
 * </ul>
 * @author Ali K Thabet
 */
public class PatentProperty {

    public static final String APPLICATION_STATUS = "Patent Application";
    public static final String ISSUED_STATUS = "Patent Issued";
    public static final String PUBLISHED_STATUS = "Patent Published";
    public static final String NO_STATUS = "No Status Available";

    public static final int APPLICATION_NUMBER_LENGTH = 8;
    public static final int ISSUE_NUMBER_LENGTH = 7;
    public static final int PUBLICATION_NUMBER_LENGTH = 11;

    private String country; // country of origin
    private String documentNumber; // document number
    private String kind; // 2 character kind
    private String status; // patent status
    private Date date; // related date

    // getters and setter:

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(status                                + " on ");
        builder.append(Patent.DISPLAY_FORMATTER.format(date) +   ", ");
        builder.append("with number " + documentNumber       +   "\n");

        return builder.toString();
    }
}
