package app.tracktune.model.author;

/**
 * Represents an Author entity with an ID, authorship name, and status.
 * This class is immutable and provides basic equality based on authorship name.
 */
public class Author {

    /**
     * Unique identifier of the author.
     * May be null if the author is not yet persisted.
     */
    private final Integer id;

    /**
     * The name of the author or authorship.
     */
    private final String authorshipName;

    /**
     * The current status of the author (e.g., ACTIVE, INACTIVE).
     */
    private final AuthorStatusEnum status;

    /**
     * Constructs an Author with the given id, authorship name, and status.
     * If the status is null, it defaults to {@link AuthorStatusEnum#ACTIVE}.
     *
     * @param id            unique identifier of the author, may be null
     * @param authorshipName the name of the author, must not be null
     * @param status        the status of the author, or null to default to ACTIVE
     */
    public Author(Integer id, String authorshipName, AuthorStatusEnum status) {
        this.id = id;
        this.authorshipName = authorshipName;
        this.status = status != null ? status : AuthorStatusEnum.ACTIVE;
    }

    /**
     * Constructs an Author with the given authorship name and status.
     * The ID is set to null.
     *
     * @param authorshipName the name of the author, must not be null
     * @param status        the status of the author, or null to default to ACTIVE
     */
    public Author(String authorshipName, AuthorStatusEnum status) {
        this(null, authorshipName, status);
    }

    /**
     * Returns the unique identifier of the author.
     *
     * @return the author ID, or null if not assigned
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the authorship name of this author.
     *
     * @return the author's name
     */
    public String getAuthorshipName() {
        return authorshipName;
    }

    /**
     * Returns the current status of this author.
     *
     * @return the author's status
     */
    public AuthorStatusEnum getStatus() {
        return status;
    }

    /**
     * Returns the string representation of the author,
     * which is the authorship name.
     *
     * @return authorship name as string
     */
    @Override
    public String toString() {
        return authorshipName;
    }

    /**
     * Checks if another object is equal to this Author.
     * Equality is based on case-insensitive authorship name comparison.
     *
     * @param o the object to compare with
     * @return true if the other object is an Author with the same authorship name ignoring case
     */
    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Author a) {
            result = a.getAuthorshipName().equalsIgnoreCase(this.authorshipName);
        }
        return result;
    }
}