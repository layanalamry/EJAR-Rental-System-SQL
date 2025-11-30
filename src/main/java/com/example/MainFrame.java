
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

public MainFrame() {

setTitle("EJAR Machinery Rental System");
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setSize(1150, 700);
setLocationRelativeTo(null);

JTabbedPane tabs = new JTabbedPane();

tabs.addTab("Customers", new CustomerPanel());
tabs.addTab("Equipment", new EquipmentPanel());
tabs.addTab("Employees", new EmployeePanel());
tabs.addTab("Rentals", new RentalPanel());
tabs.addTab("Payments", new PaymentPanel());
tabs.addTab("Makes", new MakesPanel());

add(tabs, BorderLayout.CENTER);
}
}