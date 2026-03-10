package src;

import java.sql.*;

public class DBConnection {

    public static Connection getConnection(String host, int port, String database, String user, String password) throws SQLException {

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        try {
            System.out.println(
                    "CONNECTING: " + host + ":" + port + "/" + database +
                            " USER=" + user
            );

            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found", e);
        }

        return DriverManager.getConnection(url, user, password);
    }

    public static void testConnection(Connection conn) {

        try {

            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT 1");

            if(rs.next()) {

                System.out.println("DB connection works: " + rs.getInt(1));
                System.out.println("Connected to DB: " + conn.getCatalog());

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
