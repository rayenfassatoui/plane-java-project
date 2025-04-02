package com.planeapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // !!! IMPORTANT: Store credentials securely, not hardcoded in production !!!
    // Consider environment variables or a configuration file.

    // URL without user/password
    private static final String DB_HOST = "ep-autumn-block-a227t8xs-pooler.eu-central-1.aws.neon.tech";
    private static final String DB_NAME = "plane_management_db";
    private static final String DB_URL_BASE = "jdbc:postgresql://" + DB_HOST + "/" + DB_NAME;
    private static final String DB_OPTIONS = "?sslmode=require"; // Keep options separate or add here
    private static final String DB_URL = DB_URL_BASE + DB_OPTIONS;

    // Separate User/Password constants
    private static final String DB_USER = "plane_management_db_owner";
    private static final String DB_PASSWORD = "npg_uQ8pGDSRE3cf";

    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        // Simple singleton pattern for the connection (can be improved for robustness)
        if (connection == null || connection.isClosed()) {
            try {
                 // Explicitly load the driver class (can often be omitted with modern drivers)
                 Class.forName("org.postgresql.Driver");

                System.out.println("Attempting to connect to database..."); // Debug output

                // Set connection properties for user and password
                Properties props = new Properties();
                props.setProperty("user", DB_USER);
                props.setProperty("password", DB_PASSWORD);
                // Add sslmode here if not in the URL string
                // props.setProperty("sslmode", "require");

                // Get connection using URL and Properties
                connection = DriverManager.getConnection(DB_URL, props);

                System.out.println("Database connection established successfully."); // Debug output
            } catch (ClassNotFoundException cnfe) {
                System.err.println("Database Driver Error: PostgreSQL JDBC Driver not found in classpath!");
                cnfe.printStackTrace();
                throw new SQLException("PostgreSQL JDBC Driver not found", cnfe);
            } catch (SQLException e) {
                System.err.println("Database Connection Error: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for detailed error
                throw e; // Re-throw the exception to signal the failure
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
                connection = null; // Reset the connection variable
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Optional: Add a shutdown hook to ensure connection is closed on application exit
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
    }
} 