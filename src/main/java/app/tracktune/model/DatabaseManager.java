package app.tracktune.model;

import app.tracktune.config.AppConfig;
import app.tracktune.utils.DBInit;
import app.tracktune.utils.Strings;

import java.io.File;
import java.sql.*;

/**
 * Dedicated class for data manipulation
 * Classic example of singleton pattern
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection dbConnection;
    private String dbUrl;

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

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
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

    public boolean isConnected() {
        return dbConnection != null;
    }

    private boolean isValidSQL(String sql) {
        boolean result = true;

        if (sql.isBlank())
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

    public boolean executeUpdate(String sql, Object... params) {
        boolean result = false;
        try {
            if (!isValidSQL(sql))
                return result;

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

    public <T> T executeQuery(String sql, ResultSetProcessor<T> processor, Object... params) {
        T result = null;
        try {
            if (!isValidSQL(sql))
                return result;

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

    public interface ResultSetProcessor<T> {
        T process(ResultSet rs) throws SQLException;
    }
}