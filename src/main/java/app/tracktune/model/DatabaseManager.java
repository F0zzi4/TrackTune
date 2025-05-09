package app.tracktune.model;

import app.tracktune.config.AppConfig;

import java.io.File;
import java.sql.*;

import app.tracktune.utils.DBInit;
import app.tracktune.utils.Strings;

/**
 * Dedicated class for data manipulation
 */
public class DatabaseManager {
    private Connection dbConnection;
    private String dbUrl;

    public DatabaseManager() {
        this(AppConfig.DATABASE_PATH);
    }
    
    public DatabaseManager(String dbPath) {
        // Create parent directories if they don't exist
        File dbFile = new File(dbPath);
        if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
            if(!dbFile.getParentFile().mkdirs())
                return;
        }
        
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initializeDatabase();
    }
    
    /**
     * Initialize the database and create tables if they don't exist
     */
    private void initializeDatabase() {
        try{
            dbConnection = DriverManager.getConnection(dbUrl);
            Statement statement = dbConnection.createStatement();

            String sqlStatements = DBInit.getDBInitStatement();
            String[] queries = sqlStatements.split(";");
            for (String query : queries) {
                String trimmedQuery = query.trim();
                if (!trimmedQuery.isEmpty()) {
                    statement.executeUpdate(trimmedQuery + ";");
                }
            }

            // Check if admin user exists, create if it doesn't
            ResultSet rs = statement.executeQuery(DBInit.CHECK_ADMIN_USER_STMT);
            if (!rs.next()) {
                // Admin user doesn't exist, create it
                PreparedStatement prepStatement = dbConnection.prepareStatement(DBInit.INSERT_ADMIN_USER_STMT);
                for(int i = 0;i < DBInit.ADMIN_PARAMS.length;i++){
                    prepStatement.setObject(i+1, DBInit.ADMIN_PARAMS[i]);
                }
                prepStatement.executeUpdate();
            }
            
        } catch (SQLException e) {
            System.err.println(Strings.ERR_INIT_DB + e.getMessage());
        }
    }

    /**
     * Check if db is initialized correctly
     */
    public boolean isConnected() {
        return dbConnection != null;
    }

    /**
     * Check if the given sql statement contains sql keyword (avoiding SQL injection)
     * @param sql : Given sql statement
     */
    private boolean isValidSQL(String sql) {
        boolean result = true;

        if(sql.isBlank())
            return false;

        String[] forbiddenPatterns = {
                "('--|;|--|\\bDROP\\b|\\bSELECT\\b|\\bINSERT\\b|\\bUPDATE\\b|\\bDELETE\\b|\\bTRUNCATE\\b|\\bALTER\\b|\\bCREATE\\b|\\bEXEC\\b|\\bUNION\\b|\\bFROM\\b|\\bWHERE\\b|\\bJOIN\\b)"
        };

        for (String pattern : forbiddenPatterns) {
            if (sql.toUpperCase().matches(".*" + pattern + ".*")) {
                result = false;
                break;
            }
        }
        return result;
    }
    
    /**
     * Execute an update query (INSERT, UPDATE, DELETE)
     */
    public boolean executeUpdate(String sql, Object... params) {
        boolean result = false;
        try{
            if(!isValidSQL(sql))
                return result;

            PreparedStatement prepStatement = dbConnection.prepareStatement(sql);

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                prepStatement.setObject(i + 1, params[i]);
            }
            
            // Execute the query
            prepStatement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            System.err.println(Strings.ERR_EXEC_STMT + e.getMessage());
        }
        return result;
    }
    
    /**
     * Execute a query and process the results with a ResultSetProcessor
     */
    public <T> T executeQuery(String sql, ResultSetProcessor<T> processor, Object... params) {
        T result = null;
        try{
            if(isValidSQL(sql))
                return result;

            PreparedStatement prepStatement = dbConnection.prepareStatement(sql);
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                prepStatement.setObject(i + 1, params[i]);
            }
            
            // Execute the query and process results
            try (ResultSet rs = prepStatement.executeQuery()) {
                result = processor.process(rs);
            }
            
        } catch (SQLException e) {
            System.err.println(Strings.ERR_EXEC_STMT + e.getMessage());
        }
        return result;
    }
    
    /**
     * Interface for processing a ResultSet into a specific type
     */
    public interface ResultSetProcessor<T> {
        T process(ResultSet rs) throws SQLException;
    }
}