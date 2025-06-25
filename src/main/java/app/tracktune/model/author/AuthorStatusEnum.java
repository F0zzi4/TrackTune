package app.tracktune.model.author;

/**
 * Enumeration representing the status of an Author.
 * Each status is associated with an integer value for database storage.
 */
public enum AuthorStatusEnum {
    /**
     * Indicates the author is active.
     */
    ACTIVE(0),

    /**
     * Indicates the author has been removed.
     */
    REMOVED(1);

    private final int value;

    /**
     * Constructs an AuthorStatusEnum with the specified integer value.
     *
     * @param value the integer value representing the status
     */
    AuthorStatusEnum(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this status.
     *
     * @return integer representation of the status
     */
    public int getValue() {
        return value;
    }

    /**
     * Converts an integer value to its corresponding {@link AuthorStatusEnum}.
     *
     * @param i the integer value to convert
     * @return the corresponding AuthorStatusEnum
     * @throws IllegalArgumentException if the value does not correspond to any status
     */
    public static AuthorStatusEnum fromInt(int i) {
        for (AuthorStatusEnum status : AuthorStatusEnum.values()) {
            if (status.getValue() == i) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}
