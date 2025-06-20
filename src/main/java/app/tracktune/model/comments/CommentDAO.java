package app.tracktune.model.comments;

import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CommentDAO implements DAO<Comment> {
    private final DatabaseManager dbManager;

    //FIELDS
    private static final String ID = "ID";
    private static final String DESCRIPTION = "description";
    private static final String START_TRACK_INTERVAL = "startTrackInterval";
    private static final String END_TRACK_INTERVAL = "endTrackInterval";
    private static final String CREATION_DATE = "creationDate";
    private static final String USER_ID = "userID";
    private static final String RESOURCE_ID = "resourceID";

    // CRUD STATEMENTS
    private static final String INSERT_COMMENT_STMT = """
        INSERT INTO Comments (description, startTrackInterval, endTrackInterval, creationDate, userID, resourceID)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_COMMENT_STMT = """
        UPDATE Comments
        SET description = ?, startTrackInterval = ?, endTrackInterval = ?, creationDate = ?, userID = ?, resourceID = ?
        WHERE ID = ?
    """;

    private static final String DELETE_COMMENT_STMT = """
        DELETE FROM Comments
        WHERE ID = ?
    """;

    private static final String DELETE_INTERACTIONS_BY_COMMENT_STMT = """
        DELETE FROM Interactions
        WHERE commentID = ? OR replyID = ?
    """;

    private static final String GET_ALL_COMMENTS_STMT = """
        SELECT * FROM Comments
    """;

    private static final String GET_COMMENT_BY_ID_STMT = """
        SELECT * FROM Comments
        WHERE ID = ?
    """;

    private static final String GET_COMMENT_BY_TRACK_ID_STMT = """
        SELECT c.*
        FROM Comments c
        LEFT JOIN Interactions i ON c.ID = i.replyID
        WHERE c.resourceID = ?
        AND i.replyID IS NULL;
    """;

    private static final String GET_REPLY_BY_COMMENT_ID_STMT = """
        SELECT c.*
        FROM Comments c
        JOIN Interactions i ON c.ID = i.replyID
        WHERE i.commentID = ?;
    """;

    private static final String INSERT_REPLY_STMT = """
            INSERT INTO Interactions (commentID, replyID)
            VALUES (?, ?)
    """;

    public CommentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Integer insert(Comment comment) {
        boolean success = dbManager.executeUpdate(INSERT_COMMENT_STMT,
                comment.getDescription(),
                comment.getStartTrackInterval(),
                comment.getEndTrackInterval(),
                comment.getCreationDate(),
                comment.getUserID(),
                comment.getResourceID()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return dbManager.getLastInsertId();
    }

    public void insertReply(int commentID, int replyID) {
        boolean success = dbManager.executeUpdate(INSERT_REPLY_STMT,
                commentID,
                replyID
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        dbManager.getLastInsertId();
    }

    @Override
    public void updateById(Comment comment, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_COMMENT_STMT,
                comment.getDescription(),
                comment.getStartTrackInterval(),
                comment.getEndTrackInterval(),
                comment.getCreationDate(),
                comment.getUserID(),
                comment.getResourceID(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public void deleteById(int id) {
        List<Comment> replies = getAllReplies(id);
        for (Comment reply : replies) {
            deleteById(reply.getID());
        }
        boolean success = dbManager.executeUpdate(DELETE_INTERACTIONS_BY_COMMENT_STMT, id, id);
        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        success = dbManager.executeUpdate(DELETE_COMMENT_STMT, id);
        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public Comment getById(int id) {
        AtomicReference<Comment> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_COMMENT_BY_ID_STMT,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return result.get();
    }

    @Override
    public List<Comment> getAll() {
        List<Comment> comments = new ArrayList<>();
        dbManager.executeQuery(GET_ALL_COMMENTS_STMT,
                rs -> {
                    while (rs.next()) {
                        comments.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return comments;
    }

    public List<Comment> getAllReplies(int commentId) {
        List<Comment> replies = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_REPLY_BY_COMMENT_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        replies.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, commentId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return replies;
    }

    public List<Comment> getAllCommentByResource(int commentId) {
        List<Comment> comments = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_COMMENT_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        comments.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, commentId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return comments;
    }


    private Comment mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String description = rs.getString(DESCRIPTION);

        // Start duration
        int startTrackInterval = rs.getInt(START_TRACK_INTERVAL);
        int endTrackInterval = rs.getInt(END_TRACK_INTERVAL);
        Timestamp creationDate = rs.getTimestamp(CREATION_DATE);
        int userID = rs.getInt(USER_ID);
        int trackID = rs.getInt(RESOURCE_ID);

        return new Comment(id, description, startTrackInterval, endTrackInterval, creationDate, userID, trackID);
    }

}