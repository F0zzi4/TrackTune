package app.tracktune.model.genre;

/**
 * Represents a music genre with an optional ID, a name, and a description.
 */
public class Genre {
    private final Integer id;
    private final String name;
    private final String description;

    /**
     * Constructs a Genre with the specified id, name, and description.
     *
     * @param id          the unique identifier of the genre
     * @param name        the name of the genre
     * @param description a textual description of the genre
     */
    public Genre(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Constructs a Genre without an ID, typically for new instances before persistence.
     *
     * @param name        the name of the genre
     * @param description a textual description of the genre
     */
    public Genre(String name, String description) {
        this.id = null;
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the genre ID.
     *
     * @return the ID of the genre, or null if not assigned
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets the genre name.
     *
     * @return the name of the genre
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the genre description.
     *
     * @return the description of the genre
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks equality with another object based on the genre name, case-insensitive.
     *
     * @param o the object to compare with
     * @return true if the other object is a Genre with the same name (case-insensitive), false otherwise
     */
    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Genre g) {
            result = g.getName().equalsIgnoreCase(this.name);
        }
        return result;
    }

    /**
     * Returns the string representation of the Genre, which is its name.
     *
     * @return the genre name
     */
    @Override
    public String toString() {
        return name;
    }
}
