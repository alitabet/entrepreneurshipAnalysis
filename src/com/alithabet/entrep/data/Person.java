package com.alithabet.entrep.data;

import java.util.ArrayList;

/**
 * <tt>Person</tt> is a data object to store
 * information about individuals in the patent
 * data extracted from the
 * <a href="http://www.google.com/googlebooks/uspto-patents-assignments.html">
 *     Google USPTO Bulk Downloads database</a>
 * The patent has 3 types of persons:
 *  - Correspondent: Name and address of
 *    person of correspondence
 *  - Patent Assignor: Person or entity
 *    registered as assignor of patent
 *  - Patent Assignee: Person or entity
 *    registered as assignee of patent
 * Class implements {@link Comparable} and defines
 * the natural order to be by name.
 * @author Ali K Thabet
 */
public class Person implements Comparable<Person> {
    private String name; // name of person or entity
    private ArrayList<String> address; // at most 4 lines of address
    private String city;
    private String state;
    private String country;
    private String postcode;

    // default no argument constructor
    public Person() {
        address = new ArrayList<>();
    }

    // getters and setters:

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public ArrayList<String> getAddress() {
        return address;
    }

    public void setAddress(ArrayList<String> address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name + ", ");
        for (String s : address) {
            builder.append(s + ", ");
        }

        if (city != null)     builder.append(city + ", ");
        if (state != null)    builder.append(state + ", ");
        if (country != null)  builder.append(country + ", ");
        if (postcode != null) builder.append(postcode + ", ");

        builder.deleteCharAt(builder.lastIndexOf(","));
        return builder.toString();
    }

    /**
     * This method returns all the strings
     * related to a person's name and address
     * in an ArrayList
     *
     * @return list of person related strings
     */
    public ArrayList<String> getAllStrings() {
        ArrayList<String> list = new ArrayList<>();

        String[] names = name.split(" ");
        for (String s : names) list.add(s.toLowerCase());

        for (String line : address) {
            String[] addresses = line.split(" ");
            for (String s : addresses) list.add(s.toLowerCase());
        }

        if (city != null) {
            String[] cities = city.split(" ");
            for (String s : cities) list.add(s.toLowerCase());
        }
        if (state != null) {
            String[] states = state.split(" ");
            for (String s : states) list.add(s.toLowerCase());
        }
        if (country != null) {
            String[] countries = country.split(" ");
            for (String s : countries) list.add(s.toLowerCase());
        }
        if (postcode != null) {
            String[] postcodes = postcode.split(" ");
            for (String s : postcodes) list.add(s.toLowerCase());
        }

        return list;
    }

//    private void addStringArrayToList(String[] strings, ArrayList<String> list) {
//        for (String s : strings) list.add(s.toLowerCase());
//    }

    /**
     * Compares two entities by name. Current implementation
     * is case-insensitive, and returns zero only if names
     * match exactly.
     *
     * TODO: Provide a more flexible matching criteria
     *
     *
     * @param that
     * @return 0 if both entities have the same name;
     *         -1 if this.name is less than that.name;
     *         1 if this.name is greater that that.name
     */
    @Override
    public int compareTo(Person that) {
        return this.name.compareTo(that.name);
    }
}
