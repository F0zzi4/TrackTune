package app.tracktune.model;

import app.tracktune.config.AppConfig;

import java.io.File;
import java.sql.*;

import app.tracktune.utils.SetupDB;
import app.tracktune.utils.Strings;

/**
 * Dedicated class for managing database manipulation
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

            // Create users table if it doesn't exist
            statement.execute(SetupDB.CREATE_USERS_TABLE_STMT);
            
            // Check if admin user exists, create if it doesn't
            ResultSet rs = statement.executeQuery(SetupDB.CHECK_ADMIN_USER_STMT);
            if (!rs.next()) {
                // Admin user doesn't exist, create it
                PreparedStatement prepStatement = dbConnection.prepareStatement(SetupDB.INSERT_ADMIN_USER_STMT);
                prepStatement.setString(1, SetupDB.ADMIN_USERNAME);
                prepStatement.setString(2, SetupDB.ADMIN_PASSWORD);
                prepStatement.setInt(3, 1);
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
     * Execute an update query (INSERT, UPDATE, DELETE)
     */
    public boolean executeUpdate(String sql, Object... params) {
        try{
            PreparedStatement prepStatement = dbConnection.prepareStatement(sql);

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                prepStatement.setObject(i + 1, params[i]);
            }
            
            // Execute the query
            prepStatement.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println(Strings.ERR_EXEC_STMT + e.getMessage());
            return false;
        }
    }
    
    /**
     * Execute a query and process the results with a ResultSetProcessor
     */
    public <T> T executeQuery(String sql, ResultSetProcessor<T> processor, Object... params) {
        try{
            PreparedStatement prepStatement = dbConnection.prepareStatement(sql);
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                prepStatement.setObject(i + 1, params[i]);
            }
            
            // Execute the query and process results
            try (ResultSet rs = prepStatement.executeQuery()) {
                return processor.process(rs);
            }
            
        } catch (SQLException e) {
            System.err.println(Strings.ERR_EXEC_STMT + e.getMessage());
            return null;
        }
    }
    
    /**
     * Interface for processing a ResultSet into a specific type
     */
    public interface ResultSetProcessor<T> {
        T process(ResultSet rs) throws SQLException;
    }
}