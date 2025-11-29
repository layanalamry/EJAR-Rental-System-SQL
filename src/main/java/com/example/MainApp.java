
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.SwingUtilities;

public class MainApp {


    private static final String URL = "jdbc:mysql://localhost:3306/ejar?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";           // your MySQL username
    private static final String PASSWORD = "EJAR123456789."; // your MySQL password

public static void main(String[] args) {
SwingUtilities.invokeLater(() -> {
MainFrame frame = new MainFrame();
frame.setVisible(true);
});
}

}

