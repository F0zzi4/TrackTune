package app.tracktune.model;

import app.tracktune.config.AppConfig;
import app.tracktune.utils.DBInit;
import app.tracktune.utils.Strings;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Dedicated class for data manipulation
 * Using singleton pattern
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection dbConnection;
    private String dbUrl;

    /**
     * Create the instance of the database manager following singleton pattern
     */
    private DatabaseManager() {
        String dbPath = AppConfig.DATABASE_PATH;
        File dbFile = new File(dbPath);
        if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
            if (!dbFile.getParentFile().mkdirs())
                return;
        }

        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initializeDatabase();
    }

    /**
     * Get the instance of database manager, following singleton pattern
     * @return the instance of database manager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialize database with DDL statements if tables don't exist, also creating an admin user to manage system
     */
    private void initializeDatabase() {
        try {
            dbConnection = DriverManager.getConnection(dbUrl);
            // to be able to do property ON DELETE CASCADE
            dbConnection.createStatement().execute("PRAGMA foreign_keys = ON;");
            Statement statement = dbConnection.createStatement();

            String sqlStatements = DBInit.getDBInitStatement();
            String[] queries = sqlStatements.split(";");
            for (String query : queries) {
                String trimmedQuery = query.trim();
                if (!trimmedQuery.isEmpty()) {
                    statement.executeUpdate(trimmedQuery + ";");
                }
            }

            ResultSet rs = statement.executeQuery(DBInit.CHECK_ADMIN_USER_STMT);
            if (!rs.next()) {
                PreparedStatement prepStatement = dbConnection.prepareStatement(DBInit.INSERT_ADMIN_USER_STMT);
                for (int i = 0; i < DBInit.ADMIN_PARAMS.length; i++) {
                    prepStatement.setObject(i + 1, DBInit.ADMIN_PARAMS[i]);
                }
                prepStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(Strings.ERR_INIT_DB + e.getMessage());
        }
    }

    /**
     * Check if the database is connected
     * @return true if it's connected, false otherwise
     */
    public boolean isConnected() {
        return dbConnection != null;
    }

    /**
     * Method to execute a statement that updates or create tables
     * @param sql statement converted in string
     * @param params statement parameters
     * @return true if the statement ran without problems, false otherwise
     */
    public boolean executeUpdate(String sql, Object... params) {
        boolean result = false;
        try {
            PreparedStatement prepStatement = dbConnection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                prepStatement.setObject(i + 1, params[i]);
            }

            prepStatement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            System.err.println(Strings.ERR_EXEC_STMT + e.getMessage());
        }
        return result;
    }

    /**
     * Method to execute a query that returns a result set
     * @param sql sql statement converted in string
     * @param processor lambda expression (processor type) that contains the result set management
     * @param params statement parameters
     * @return type of the object returned from the processor
     */
    public <T> T executeQuery(String sql, ResultSetProcessor<T> processor, Object... params) {
        T result = null;
        try {
            PreparedStatement prepStatement = dbConnection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                prepStatement.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = prepStatement.executeQuery()) {
                result = processor.process(rs);
            }

        } catch (SQLException e) {
            System.err.println(Strings.ERR_EXEC_STMT + e.getMessage());
        }
        return result;
    }

    /**
     * lambda expression (processor type) that contains the result set management
     * @param <T> type of the object returned from the processor
     */
    public interface ResultSetProcessor<T> {
        T process(ResultSet rs) throws SQLException;
    }

    public Integer getLastInsertId() {
        String sql = "SELECT last_insert_rowid()";

        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println(Strings.ERR_EXEC_STMT + e.getMessage());
        }

        return null;
    }
}