package dataAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import static java.sql.Types.NULL;

public class DatabaseManager {
    private static final String databaseName;
    private static final String user;
    private static final String password;
    private static final String connectionUrl;

    /*
     * Load the database information from the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) throw new Exception("Unable to load db.properties");
                Properties props = new Properties();
                props.load(propStream);
                databaseName = props.getProperty("db.name");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Create the database if it does not already exist
     *
     * @throws DataAccessException if the SQL connector fails
     */
    static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
            var conn = DriverManager.getConnection(connectionUrl, user, password);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and set the catalog based upon the
     * properties specified in db.properties
     * NOTE: Connections to the database should be short-lived,
     * and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     *
     * @return a new connection to the SQL database
     * @throws DataAccessException if the connection process fails
     */
    private static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(connectionUrl, user, password);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Update the database with a given SQL statement
     *
     * @param statement the SQL statement to execute
     * @param params    the SQL parameters to verify types
     * @throws DataAccessException if the SQL connector fails
     */
    static void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                for (int i = 0; i < params.length; ++i) {
                    switch (params[i]) {
                        case String p -> preparedStatement.setString((i + 1), p);
                        case Integer p -> preparedStatement.setInt((i + 1), p);
                        default -> preparedStatement.setNull((i + 1), NULL);
                    }
                }
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Could not update database: %s, %s", statement, e.getMessage()));
        }
    }

    /**
     * Query the database with a given SQL statement
     *
     * @param returnLabels the labels of each result row
     * @param statement    the SQL statement to execute
     * @param params       the SQL parameters to verify types
     * @return an ArrayList (rows) of Object arrays (elements)
     * @throws DataAccessException if the SQL connector fails
     */
    static ArrayList<Object[]> executeQuery(String[] returnLabels,
                                            String statement,
                                            Object... params) throws DataAccessException {
        ArrayList<Object[]> result = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                for (int i = 0; i < params.length; ++i) {
                    switch (params[i]) {
                        case String p -> preparedStatement.setString((i + 1), p);
                        case Integer p -> preparedStatement.setInt((i + 1), p);
                        default -> preparedStatement.setNull((i + 1), NULL);
                    }
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Object[] row = new Object[returnLabels.length];
                        for (int i = 0; i < row.length; ++i) {
                            row[i] = resultSet.getObject(returnLabels[i]);
                        }
                        result.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Could not query database: %s, %s", statement, e.getMessage()));
        }
        return result;
    }
}