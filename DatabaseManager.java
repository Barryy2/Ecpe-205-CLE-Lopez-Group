import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:bank_tracker.db";

    public DatabaseManager() {
        try {

            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite Driver not found! Ensure the JAR is in your project libraries.");
        }
    }

    private void initDatabase() {

        String sql = "CREATE TABLE IF NOT EXISTS accounts_v2 (" +
                "id INTEGER PRIMARY KEY, " +
                "parent_id INTEGER, " +
                "bank_name TEXT, " +
                "amount TEXT, " +
                "image_path TEXT)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
        } catch (SQLException e) {
            System.err.println("DB Initialization failed: " + e.getMessage());
        }
    }

    public List<String[]> loadAccounts() {
        List<String[]> accounts = new ArrayList<>();
        String query = "SELECT id, parent_id, bank_name, amount, image_path FROM accounts_v2 ORDER BY id";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                accounts.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getInt("parent_id")),
                        rs.getString("bank_name"),
                        rs.getString("amount"),
                        rs.getString("image_path") != null ? rs.getString("image_path") : ""
                });
            }
        } catch (SQLException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
        return accounts;
    }

    public void saveAccounts(List<String[]> accounts) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            conn.setAutoCommit(false);

            try (Statement deleteStmt = conn.createStatement()) {

                deleteStmt.executeUpdate("DELETE FROM accounts_v2");

                String insertSql = "INSERT INTO accounts_v2 (id, parent_id, bank_name, amount, image_path) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (String[] acc : accounts) {
                        pstmt.setInt(1, Integer.parseInt(acc[0])); // id
                        pstmt.setInt(2, Integer.parseInt(acc[1])); // parent_id
                        pstmt.setString(3, acc[2]);                // bank_name
                        pstmt.setString(4, acc[3]);                // amount
                        pstmt.setString(5, (acc.length > 4) ? acc[4] : ""); // image_path
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}