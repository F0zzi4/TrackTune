package app.tracktune.model.musicalInstrument;

/**
 * Represents a musical instrument entity with an ID, name, and description.
 * Implements {@link Comparable} to allow sorting alphabetically by name (case-insensitive).
 */
public class MusicalInstrument implements Comparable<MusicalInstrument> {
    private final Integer id;
    private final String name;
    private final String description;

    /**
     * Constructs a MusicalInstrument with a specified ID, name, and description.
     * Typically used for existing instruments retrieved from the database.
     *
     * @param id the unique identifier of the instrument
     * @param name the name of the instrument
     * @param description a description of the instrument
     */
    public MusicalInstrument(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Constructs a new MusicalInstrument without an ID.
     * Typically used for creating new instruments before insertion into the database.
     *
     * @param name the name of the instrument
     * @param description a description of the instrument
     */
    public MusicalInstrument(String name, String description) {
        this.id = null;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the ID of the musical instrument.
     *
     * @return the instrument's ID, or null if not set
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the description of the musical instrument.
     *
     * @return the description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name of the musical instrument.
     *
     * @return the instrument's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the string representation of the musical instrument, which is its name.
     *
     * @return the instrument's name as string
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Compares this musical instrument with another based on their names, ignoring case.
     * This allows sorting instruments alphabetically.
     *
     * @param o the other MusicalInstrument to compare to
     * @return a negative integer, zero, or a positive integer as this instrument's
     *         name is less than, equal to, or greater than the other instrument's name
     */
    @Override
    public int compareTo(MusicalInstrument o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}
