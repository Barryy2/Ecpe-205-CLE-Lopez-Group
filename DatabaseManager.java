import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:bank_tracker.db";

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            // Added image_path column to the schema
            String sql = "CREATE TABLE IF NOT EXISTS bank_accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "bank_name TEXT, " +
                    "amount TEXT, " +
                    "image_path TEXT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    public List<String[]> loadAccounts() {
        List<String[]> accounts = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT bank_name, amount, image_path FROM bank_accounts ORDER BY id")) {

            while (rs.next()) {
                accounts.add(new String[]{
                        rs.getString("bank_name"),
                        rs.getString("amount"),
                        rs.getString("image_path")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error loading database: " + e.getMessage());
        }
        return accounts;
    }

    public void saveAccounts(List<String[]> accounts) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM bank_accounts");

            String insertSql = "INSERT INTO bank_accounts (bank_name, amount, image_path) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (String[] acc : accounts) {
                    pstmt.setString(1, acc[0]);
                    pstmt.setString(2, acc[1]);
                    pstmt.setString(3, (acc.length > 2 && acc[2] != null) ? acc[2] : "");
                    pstmt.executeUpdate();
                }
            }
        }
    }
}