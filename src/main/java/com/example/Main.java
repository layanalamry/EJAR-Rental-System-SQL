
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/ejar?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";           // your MySQL username
    private static final String PASSWORD = "EJAR123456789."; // your MySQL password

    public static void main(String[] args) {
        try {
            // Load the driver (optional in modern JDBC, but safe)
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected successfully!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("email"));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found. Make sure MySQL Connector/J is in your dependencies.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
