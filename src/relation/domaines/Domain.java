package relation.domaines;


import java.util.*;

/**
 * Represents a domain that can contain various types of definitions including
 * classes, intervals, and specific values.
 */
public class Domain {
    private  String domainName;
    private Object[] definition;

    /**
     * Creates a new Domain with the specified name and definition.
     * @param domainName The name of the domain
     * @param definition The array of objects defining the domain's valid values
     * @throws IllegalArgumentException if domainName is null or empty, or if definition is null
     */
    public Domain(String domainName, Object[] definition) {
        if (domainName == null || domainName.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain name cannot be null or empty");
        }
        if (definition == null) {
            throw new IllegalArgumentException("Definition array cannot be null");
        }

        this.domainName = domainName;
        this.definition = Arrays.copyOf(definition, definition.length);
    }
    public Domain(String domainName, Object definition) {
        if (domainName == null || domainName.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain name cannot be null or empty");
        }
        if (definition == null) {
            throw new IllegalArgumentException("Definition array cannot be null");
        }

        this.domainName = domainName;
        this.definition = new Object[] {definition};
    }

    /**
     * Gets the domain name.
     * @return The domain name
     */
    public String getDomainName() {
        return domainName;
    }
    public void setDomainName(String domainName) {
         this.domainName = domainName;
    }

    /**
     * Gets a copy of the domain definition array.
     * @return A copy of the definition array
     */
    public Object[] getDefinition() {
        return Arrays.copyOf(definition, definition.length);
    }

    /**
     * Sets a new definition array for the domain.
     * @param definition The new definition array
     * @throws IllegalArgumentException if the definition array is null
     */
    public void setDefinition(Object[] definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Definition array cannot be null");
        }
        this.definition = Arrays.copyOf(definition, definition.length);
    }

    /**
     * Checks if a value is valid within this domain.
     * @param value The value to check
     * @return true if the value is valid within the domain, false otherwise
     */
    public boolean checkValue(Object value) {
        if (value == null) {
            return true;
         }

        for (Object obj : definition) {
            // Handle null elements in definition
            if (obj == null) {
                continue;
            }

            // Check if obj is a Class and value is an instance of that class
            if (obj instanceof Class<?> cls) {
                if (cls.isInstance(value)) {
                    return true;
                }
            }

            // Handle strings in definition starting with "class "
            if (obj instanceof String objStr && objStr.startsWith("class ")) {
                try {
                    // Extract the class name and load the class
                    String className = objStr.substring(6).trim();
                    Class<?> cls = Class.forName(className);

                    // Check if value is an instance of the loaded class
                    if (cls.isInstance(value)) {
                        return true;
                    }
                } catch (ClassNotFoundException e) {
                    // Handle the case where the class cannot be loaded
                    System.err.println("Class not found: " + objStr);
                }
            }

            // Check if obj is a string representing an interval
            if (obj instanceof String objStr && objStr.startsWith("class ")) {
                try {
                    // Extract the class name and load the class
                    String className = objStr.substring(6).trim();
                    Class<?> cls = Class.forName(className);

                    // Check if value is an instance of the loaded class
                    if (cls.isInstance(value)) {
                        return true;
                    }
                } catch (ClassNotFoundException e) {
                    // Handle the case where the class cannot be loaded
                    System.err.println("Class not found: " + objStr);
                }
            }

            // Check if obj is a string representing an interval
            if (obj instanceof String objStr && objStr.matches("\\[\\d+,\\d{2}, \\d+,\\d{2}\\]")) {
                try {
                    // Clean the string to extract numbers (remove brackets)
                    objStr = objStr.replaceAll("[\\[\\]]", ""); // Remove brackets
                    objStr = objStr.replace(",", "."); // Replace commas with dots for decimal numbers

                    // Remove any trailing dot that could be present in the string
                    objStr = objStr.replaceAll("\\.$", ""); // Remove dot at the end of the string

                    // Split the string into parts by the space between the two numbers
                    String[] parts = objStr.split("\\s+"); // Split by spaces

                    // Debugging: print the parts array

                    // Ensure that the array has two parts (min and max)
                    if (parts.length == 2) {
                        // Extract minVal and maxVal as the first and second parts
                        String minValStr = parts[0].trim().substring(0,parts[0].trim().length()-1);  // min value as string
                        String maxValStr = parts[1].trim();  // max value as string

                        // Convert the cleaned strings to double values
                        double minVal = Double.parseDouble(minValStr);
                        double maxVal = Double.parseDouble(maxValStr);

                        // Create interval and check if the value is inside the range
                        Interval interval = new Interval(minVal, maxVal);
                        if (interval.isInInterval(value)) {
                            return true;
                        }
                    } else {
                        System.err.println("Invalid interval format: " + objStr);
                    }
                } catch (Exception e) {
                    System.err.println("Invalid interval format: " + objStr);
                }
            }




            // Check if value equals obj
            if (Objects.equals(value, obj)) {
                return true;
            }

            // Check if obj is an Interval
            if (obj instanceof Interval interval) {
                try {
                    if (interval.isInInterval(value)) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // Continue checking other definition elements if this one fails
                    continue;
                }
            }
        }
        return false;
    }





    /**
     * Adds a new definition element to the domain.
     * @param element The element to add
     * @throws IllegalArgumentException if the element is invalid
     */
    public void addDefinitionElement(Object element) {
        Object[] newDefinition = Arrays.copyOf(definition, definition.length + 1);
        newDefinition[definition.length] = element;
        setDefinition(newDefinition);
    }

    @Override
    public String toString() {
        return "Domain{" +
               "name='" + domainName + '\'' +
               ", definition=" + Arrays.toString(definition) +
               '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Domain other)) return false;
        return Objects.equals(domainName, other.domainName) &&
               Arrays.deepEquals(definition, other.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainName, Arrays.deepHashCode(definition));
    }
    /**
     * Creates a union of multiple domains.
     * @param domains The domains to unite
     * @return A new Domain containing all elements from all input domains
     * @throws IllegalArgumentException if domains array is null or empty
     */
    public static Domain union(Domain... domains) {
        if (domains == null || domains.length == 0) {
            throw new IllegalArgumentException("Domains array cannot be null or empty");
        }
        if (domains.length == 1) {
            return new Domain(domains[0].getDomainName(), domains[0].getDefinition());
        }

        // Collect all definitions
        Set<Object> unionSet = new HashSet<>();
        List<Interval> intervals = new ArrayList<>();

        // First pass: collect all non-interval elements and intervals separately
        for (Domain domain : domains) {
            for (Object def : domain.getDefinition()) {
                if (def instanceof Interval) {
                    intervals.add((Interval) def);
                } else {
                    unionSet.add(def);
                }
            }
        }

        // Handle intervals if present
        if (!intervals.isEmpty()) {
            List<Interval> mergedIntervals = Interval.mergeIntervals(intervals);
            unionSet.addAll(mergedIntervals);
        }

        // Create new domain with unified elements
        return new Domain("Union_" + domains[0].getDomainName(), unionSet.toArray());
    }

    /**
     * Creates an intersection of multiple domains.
     * @param domains The domains to intersect
     * @return A new Domain containing elements common to all input domains
     * @throws IllegalArgumentException if domains array is null or empty
     */
    public static Domain intersection(Domain... domains) {
        if (domains == null || domains.length == 0) {
            throw new IllegalArgumentException("Domains array cannot be null or empty");
        }
        if (domains.length == 1) {
            return new Domain(domains[0].getDomainName(), domains[0].getDefinition());
        }

        // Start with all elements from the first domain
        Set<Object> intersectionSet = new HashSet<>();
        List<Interval> firstIntervals = new ArrayList<>();

        // Separate intervals and other elements from first domain
        for (Object def : domains[0].getDefinition()) {
            if (def instanceof Interval) {
                firstIntervals.add((Interval) def);
            } else {
                intersectionSet.add(def);
            }
        }

        // Intersect with each subsequent domain
        for (int i = 1; i < domains.length; i++) {
            Set<Object> currentSet = new HashSet<>();
            List<Interval> currentIntervals = new ArrayList<>();

            // Collect current domain's elements
            for (Object def : domains[i].getDefinition()) {
                if (def instanceof Interval) {
                    currentIntervals.add((Interval) def);
                } else {
                    currentSet.add(def);
                }
            }

            // Intersect non-interval elements
            intersectionSet.retainAll(currentSet);

            // Intersect intervals
            if (!firstIntervals.isEmpty() && !currentIntervals.isEmpty()) {
                firstIntervals = Interval.intersectIntervalLists(firstIntervals, currentIntervals);
            } else {
                firstIntervals.clear();
            }
        }

        // Add remaining intervals to result
        intersectionSet.addAll(firstIntervals);

        return new Domain("Intersection_" + domains[0].getDomainName(), intersectionSet.toArray());
    }

    /**
     * Creates a difference between two domains (first domain minus the second).
     * @param first The domain to subtract from
     * @param second The domain to subtract
     * @return A new Domain containing elements in first but not in second
     * @throws IllegalArgumentException if either domain is null
     */
    public static Domain difference(Domain first, Domain second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Domains cannot be null");
        }

        Set<Object> differenceSet = new HashSet<>();
        List<Interval> firstIntervals = new ArrayList<>();
        List<Interval> secondIntervals = new ArrayList<>();

        // Separate intervals and other elements
        for (Object def : first.getDefinition()) {
            if (def instanceof Interval) {
                firstIntervals.add((Interval) def);
            } else {
                differenceSet.add(def);
            }
        }

        Set<Object> secondSet = new HashSet<>();
        for (Object def : second.getDefinition()) {
            if (def instanceof Interval) {
                secondIntervals.add((Interval) def);
            } else {
                secondSet.add(def);
            }
        }

        // Remove elements from second domain
        differenceSet.removeAll(secondSet);

        // Handle intervals
        if (!firstIntervals.isEmpty()) {
            List<Interval> diffIntervals = Interval.differenceIntervalLists(firstIntervals, secondIntervals);
            differenceSet.addAll(diffIntervals);
        }

        return new Domain("Difference_" + first.getDomainName(), differenceSet.toArray());
    }
}