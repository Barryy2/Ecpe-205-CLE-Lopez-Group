import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:bank_tracker.db";

    public DatabaseManager() {
        initDatabase();
    }

    /**
     * Initializes the SQLite database and creates the table if it doesn't exist.
     */
    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS bank_accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "bank_name TEXT, " +
                    "amount TEXT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Loads accounts from the database.
     * @return A list of string arrays. Index 0 is the bank name, Index 1 is the amount.
     */
    public List<String[]> loadAccounts() {
        List<String[]> accounts = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT bank_name, amount FROM bank_accounts ORDER BY id")) {

            while (rs.next()) {
                String[] accountData = {rs.getString("bank_name"), rs.getString("amount")};
                accounts.add(accountData);
            }
        } catch (SQLException e) {
            System.out.println("Error loading database: " + e.getMessage());
        }
        return accounts;
    }

    /**
     * Clears the old data and saves the current list of accounts.
     * @param accounts A list of string arrays containing [bank_name, amount].
     */
    public void saveAccounts(List<String[]> accounts) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // 1. Delete the old saved data
            stmt.executeUpdate("DELETE FROM bank_accounts");

            // 2. Insert the new data
            String insertSql = "INSERT INTO bank_accounts (bank_name, amount) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (String[] acc : accounts) {
                    pstmt.setString(1, acc[0]);
                    pstmt.setString(2, acc[1]);
                    pstmt.executeUpdate();
                }
            }
        }
    }
}