package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    private static List<String[]> extractBooks(ResultSet rs) throws SQLException {

        List<String[]> books = new ArrayList<>();

        while (rs.next()) {

            String[] row = {
                    String.valueOf(rs.getInt("id")),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    String.valueOf(rs.getInt("year"))
            };

            books.add(row);
        }

        return books;
    }

    public static List<String[]> getAllBooks(Connection conn) throws SQLException {

        String sql = "SELECT * FROM sp_get_all_books()";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return extractBooks(rs);
        }
    }

    public static List<String[]> search(Connection conn, String field, String value) throws SQLException {

        String sql;

        switch(field) {

            case "Title":
                sql = "SELECT * FROM sp_find_by_title(?)";
                break;

            case "Author":
                sql = "SELECT * FROM sp_find_by_author(?)";
                break;

            case "Genre":
                sql = "SELECT * FROM sp_find_by_genre(?)";
                break;

            default:
                throw new SQLException("Invalid search field");
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);

            try (ResultSet rs = stmt.executeQuery()) {

                return extractBooks(rs);

            }
        }
    }

    public static void addBook(Connection conn, String title, String author, String genre, int year) throws SQLException {

        String sql = "CALL sp_add_book(?, ?, ?, ?)";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, genre);
            stmt.setInt(4, year);

            stmt.execute();
        }
    }

    public static void updateBook(Connection conn, int id, String title, String author, String genre, int year) throws SQLException {

        String sql = "CALL sp_update_book(?, ?, ?, ?, ?)";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, id);
            stmt.setString(2, title);
            stmt.setString(3, author);
            stmt.setString(4, genre);
            stmt.setInt(5, year);

            stmt.execute();
        }
    }

    public static void deleteById(Connection conn, int id) throws SQLException {

        String sql = "CALL sp_delete_by_id(?)";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, id);
            stmt.execute();
        }
    }

    public static void deleteByTitle(Connection conn, String title) throws SQLException {

        String sql = "CALL sp_delete_by_title(?)";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, title);
            stmt.execute();
        }
    }

    public static void deleteByAuthor(Connection conn, String author) throws SQLException {

        String sql = "CALL sp_delete_by_author(?)";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, author);
            stmt.execute();
        }
    }

    public static void deleteByGenre(Connection conn, String genre) throws SQLException {

        String sql = "CALL sp_delete_by_genre(?)";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, genre);
            stmt.execute();
        }
    }

    public static void clearTable(Connection conn) throws SQLException {

        String sql = "CALL sp_clear_table()";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.execute();
        }
    }

    public static int countBooks(Connection conn) throws SQLException {

        String sql = "SELECT sp_count_books()";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if(rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    public static void createDatabase(String dbName) throws SQLException {

        String url = "jdbc:postgresql://localhost:5433/postgres";

        try(Connection conn = DriverManager.getConnection(url,"postgres","postgres");
            CallableStatement stmt = conn.prepareCall("CALL sp_create_database(?)")) {

            stmt.setString(1, dbName);
            stmt.execute();
        }
    }
}
