package relation;

import relation.domaines.Domain;


public class Attribute {
    private String attributeName;
    private Domain domain;

    /**
     * Creates a new Attribute with a name and domain.
     *
     * @param attributeName The name of the attribute
     * @param domain        The domain constraining valid values
     * @throws IllegalArgumentException if attributeName is null or empty, or if domain is null
     */
    public Attribute(String attributeName, Domain domain) {
        setAttributeName(attributeName);
        setDomain(domain);
        // Note: We don't set attributeClass to Domain.class anymore as it was incorrect
    }

    /**
     * Creates an empty Attribute.
     *
     * @deprecated Use parameterized constructors instead for proper initialization
     */
    @Deprecated
    public Attribute() {
        // Default constructor kept for backward compatibility
    }

    /**
     * Gets the domain constraining this attribute's values.
     *
     * @return The domain, or null if no domain is set
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     * Sets the domain constraining this attribute's values.
     *
     * @param domain The domain to set
     * @throws IllegalArgumentException if domain is null
     */
    public void setDomain(Domain domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain cannot be null");
        }
        this.domain = domain;
    }

    /**
     * Gets the attribute name.
     *
     * @return The attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Sets the attribute name.
     *
     * @param attributeName The name to set
     * @throws IllegalArgumentException if attributeName is null or empty
     */
    public void setAttributeName(String attributeName) {
        if (attributeName == null || attributeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null or empty");
        }
        this.attributeName = attributeName.trim();
    }


    public boolean sameName(String name) {
        return this.attributeName.equalsIgnoreCase(name);
    }

    /**
     * Checks if a value is valid for this attribute.
     *
     * @param value The value to check
     * @return true if the value is valid, false otherwise
     */
    public boolean checkValue(Object value) {
        // if (value == null) {
        //    return false;
        // }
        return domain == null || domain.checkValue(value);
    }

    /**
     * Checks if this attribute has the specified name (case-insensitive).
     *
     * @param name The name to check
     * @return true if the names match, false otherwise
     */
    public boolean hasName(String name) {
        return attributeName != null &&
               attributeName.equalsIgnoreCase(name);
    }


}