import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ejar?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "EJAR123456789.";

    public static void main(String[] args) {
        String url = getEnvOrDefault("EJAR_DB_URL", DEFAULT_URL);
        String user = getEnvOrDefault("EJAR_DB_USER", DEFAULT_USER);
        String password = getEnvOrDefault("EJAR_DB_PASS", DEFAULT_PASSWORD);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // load driver

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

                System.out.println("Connected successfully to: " + url);
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("email"));
                }

            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add mysql-connector-java to your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getEnvOrDefault(String name, String def) {
        String v = System.getenv(name);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
