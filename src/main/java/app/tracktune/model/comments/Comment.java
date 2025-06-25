package app.tracktune.model.comments;

import java.sql.Timestamp;

/**
 * Represents a comment associated with a resource and a user,
 * optionally linked to a specific interval of a track.
 */
public class Comment {
    private final Integer ID;
    private final String description;
    private final int startTrackInterval;
    private final int endTrackInterval;
    private final Timestamp creationDate;
    private final int userID;
    private final int resourceID;

    /**
     * Constructs a Comment with all fields specified.
     *
     * @param ID               the unique identifier of the comment, may be null for new comments
     * @param description      the text content of the comment
     * @param startTrackInterval the start position (in track units) related to the comment
     * @param endTrackInterval the end position (in track units) related to the comment
     * @param creationDate     the timestamp when the comment was created
     * @param userID           the ID of the user who created the comment
     * @param resourceID       the ID of the resource the comment is associated with
     */
    public Comment(Integer ID, String description, int startTrackInterval, int endTrackInterval, Timestamp creationDate, int userID, int resourceID){
        this.ID = ID;
        this.description = description;
        this.startTrackInterval = startTrackInterval;
        this.endTrackInterval = endTrackInterval;
        this.creationDate = creationDate;
        this.userID = userID;
        this.resourceID = resourceID;
    }

    /**
     * Constructs a Comment without specifying track intervals (defaults to 0).
     *
     * @param description  the text content of the comment
     * @param creationDate the timestamp when the comment was created
     * @param userID       the ID of the user who created the comment
     * @param resourceID   the ID of the resource the comment is associated with
     */
    public Comment(String description, Timestamp creationDate, int userID, int resourceID){
        this.ID = null;
        this.description = description;
        this.startTrackInterval = 0;
        this.endTrackInterval = 0;
        this.creationDate = creationDate;
        this.userID = userID;
        this.resourceID = resourceID;
    }

    /**
     * Constructs a Comment specifying track intervals but without an ID.
     *
     * @param description      the text content of the comment
     * @param startTrackInterval the start position (in track units) related to the comment
     * @param endTrackInterval the end position (in track units) related to the comment
     * @param creationDate     the timestamp when the comment was created
     * @param userID           the ID of the user who created the comment
     * @param resourceID       the ID of the resource the comment is associated with
     */
    public Comment(String description, int startTrackInterval, int endTrackInterval, Timestamp creationDate, int userID, int resourceID){
        this.ID = null;
        this.description = description;
        this.startTrackInterval = startTrackInterval;
        this.endTrackInterval = endTrackInterval;
        this.creationDate = creationDate;
        this.userID = userID;
        this.resourceID = resourceID;
    }

    /**
     * Returns the unique identifier of the comment.
     *
     * @return the comment ID, or null if not yet persisted
     */
    public Integer getID() {
        return ID;
    }

    /**
     * Returns the description text of the comment.
     *
     * @return the comment description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the start position of the track interval related to the comment.
     *
     * @return start track interval
     */
    public int getStartTrackInterval() {
        return startTrackInterval;
    }

    /**
     * Returns the end position of the track interval related to the comment.
     *
     * @return end track interval
     */
    public int getEndTrackInterval() {
        return endTrackInterval;
    }

    /**
     * Returns the creation timestamp of the comment.
     *
     * @return the creation date as a {@link Timestamp}
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the user ID of the author who created the comment.
     *
     * @return user ID associated with the comment
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Returns the resource ID to which the comment is attached.
     *
     * @return resource ID associated with the comment
     */
    public int getResourceID() {
        return resourceID;
    }
}
