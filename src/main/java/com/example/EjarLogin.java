
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EjarLogin extends JFrame {

    // ====== THEME ======
    private static final Color BUTTER_YELLOW = new Color(245, 210, 90);
    private static final Color BUTTON_DARK   = new Color(40, 40, 40);
    private static final Color CARD_BG       = new Color(255, 255, 255, 235);
    private static final Color BORDER_COLOR  = new Color(180, 180, 180);

    private static final Font BASE_FONT   = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font TITLE_FONT  = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font LABEL_FONT  = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font HINT_FONT   = new Font("Segoe UI", Font.PLAIN, 12);

    private JRadioButton customerRadio;
    private JRadioButton employeeRadio;
    private JLabel inputLabel;
    private JTextField inputField;
    private JPasswordField passwordField;
    private JLabel passwordHint;

    public EjarLogin() {
        setTitle("EJAR - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // === Full-screen background image using BackgroundPanel (same as dashboards) ===
        BackgroundPanel bg = new BackgroundPanel("/images/bg.jpg");   // this panel draws scaled image
        bg.setLayout(new GridBagLayout());                   // center the login card
        setContentPane(bg);

        // center card
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        bg.add(buildLoginCard(), gbc);
    }

    private JPanel buildLoginCard() {

        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(CARD_BG);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // ===== Title strip =====
        JLabel title = new JLabel("Welcome to EJAR", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(Color.BLACK);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BUTTER_YELLOW);
        titleBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        titleBar.add(title, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(titleBar, gbc);

        // ===== Role radio buttons =====
        customerRadio = new JRadioButton("Customer");
        employeeRadio = new JRadioButton("Employee");
        customerRadio.setOpaque(false);
        employeeRadio.setOpaque(false);
        customerRadio.setFont(LABEL_FONT);
        employeeRadio.setFont(LABEL_FONT);

        ButtonGroup group = new ButtonGroup();
        group.add(customerRadio);
        group.add(employeeRadio);
        customerRadio.setSelected(true);

        customerRadio.addActionListener(e -> updateInputLabel());
        employeeRadio.addActionListener(e -> updateInputLabel());

        gbc.gridy++;
        gbc.gridwidth = 1;
        card.add(customerRadio, gbc);
        gbc.gridx = 1;
        card.add(employeeRadio, gbc);

        // ===== Input fields =====
        inputLabel = new JLabel("Customer name:");
        inputLabel.setFont(LABEL_FONT);

        inputField = new JTextField();
        inputField.setFont(BASE_FONT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);

        passwordField = new JPasswordField();
        passwordField.setFont(BASE_FONT);

        gbc.gridx = 0; gbc.gridy++;
        card.add(inputLabel, gbc);

        gbc.gridx = 1;
        card.add(inputField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        card.add(passLabel, gbc);

        gbc.gridx = 1;
        card.add(passwordField, gbc);

        // ===== Hint =====
        passwordHint = new JLabel("Hint: Password = last 4 digits of your mobile number.");
        passwordHint.setFont(HINT_FONT);
        passwordHint.setForeground(new Color(80, 80, 80));

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        card.add(passwordHint, gbc);

        // ===== Buttons =====
        JButton loginButton = new JButton("Login");
        styleMainButton(loginButton);
        loginButton.addActionListener(e -> handleLogin());

        JButton registerButton = new JButton("Register");
        styleMainButton(registerButton);
        registerButton.addActionListener(e -> handleRegister());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        card.add(buttonsPanel, gbc);

        return card;
    }

    private void styleMainButton(JButton btn) {
        btn.setBackground(BUTTON_DARK);
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void updateInputLabel() {
        if (customerRadio.isSelected()) {
            inputLabel.setText("Customer name:");
            passwordHint.setText("Hint: Password = last 4 digits of your mobile number.");
        } else {
            inputLabel.setText("Employee ID:");
            passwordHint.setText("Hint: Use your default company password.");
        }
    }

    // ==================== LOGIN ====================
    private void handleLogin() {
        String text = inputField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (text.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your " + (customerRadio.isSelected() ? "name" : "employee ID") +
                            " and password.",
                    "Missing information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (customerRadio.isSelected()) {
            handleCustomerLogin(text, password);
        } else {
            handleEmployeeLogin(text, password);
        }
    }

    private void handleCustomerLogin(String customerName, String password) {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Cus_mobile FROM CUSTOMER WHERE Cus_name = ?")) {

            ps.setString(1, customerName);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Customer not found.",
                        "Login error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String mobile = rs.getString("Cus_mobile");
            String last4 = mobile.substring(mobile.length() - 4);

            if (!password.equals(last4)) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect password.",
                        "Login error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Welcome, " + customerName + "!",
                    "Login successful",
                    JOptionPane.INFORMATION_MESSAGE);

            CustomerDashboard cd = new CustomerDashboard(customerName, false);
            cd.setVisible(true);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleEmployeeLogin(String id, String password) {

        int empId;
        try {
            empId = Integer.parseInt(id);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID must be a number.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Emp_name FROM EMPLOYEE WHERE Emp_id = ?")) {

            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Employee not found.",
                        "Login error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals("1234")) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect password.",
                        "Login error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = rs.getString("Emp_name");

            JOptionPane.showMessageDialog(this,
                    "Welcome, " + name,
                    "Login successful",
                    JOptionPane.INFORMATION_MESSAGE);

            EmployeeDashboard ed = new EmployeeDashboard(empId, name);
            ed.setVisible(true);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ==================== REGISTER ====================
    private void handleRegister() {
        if (customerRadio.isSelected()) {
            registerCustomer();
        } else {
            registerEmployee();
        }
    }

    private void registerCustomer() {

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField mobileField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Mobile:"));
        panel.add(mobileField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Customer registration", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String mobile = mobileField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO CUSTOMER (Cus_name, Cus_mobile, Email) VALUES (?, ?, ?)")) {

            ps.setString(1, name);
            ps.setString(2, mobile);
            ps.setString(3, email);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Customer registered successfully!",
                    "Registered",
                    JOptionPane.INFORMATION_MESSAGE);

            CustomerDashboard cd = new CustomerDashboard(name, true);
            cd.setVisible(true);
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Customer already exists.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerEmployee() {

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Employee ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Employee registration", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO EMPLOYEE (Emp_id, Emp_name) VALUES (?, ?)")) {

            ps.setInt(1, Integer.parseInt(idField.getText().trim()));
            ps.setString(2, nameField.getText().trim());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Employee registered successfully!",
                    "Registered",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error registering employee",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EjarLogin().setVisible(true);
        });
    }
}