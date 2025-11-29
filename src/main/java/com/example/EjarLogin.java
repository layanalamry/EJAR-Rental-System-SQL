
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class EjarLogin extends JFrame {

    private JRadioButton customerRadio;
    private JRadioButton employeeRadio;
    private JLabel inputLabel;
    private JTextField inputField;

    public EjarLogin() {
        setTitle("EJAR - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // ========= خلفية صورة اللوق ان =========
        String imgPath = "C:/Users/norah/Downloads/login pic.jpeg";

        ImageIcon bgIcon = new ImageIcon(imgPath);
        JLabel background = new JLabel(bgIcon);   // نخلي الصورة على لابل
        background.setLayout(new GridBagLayout()); // نحط الفورم في النص

        setContentPane(background);

        // نحط كرت اللوق ان في وسط الصورة
        background.add(buildLoginCard());
    }

    // ========= كرت اللوق إن فوق الخلفية =========
    private JPanel buildLoginCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 220)); // أبيض شفاف شوي

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(96, 111, 131), 2),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome to EJAR", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));

        customerRadio = new JRadioButton("Customer");
        employeeRadio = new JRadioButton("Employee");
        customerRadio.setOpaque(false);
        employeeRadio.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(customerRadio);
        group.add(employeeRadio);
        customerRadio.setSelected(true);

        customerRadio.addActionListener(e -> updateInputLabel());
        employeeRadio.addActionListener(e -> updateInputLabel());

        inputLabel = new JLabel("Customer name:");
        inputLabel.setFont(new Font("Serif", Font.BOLD, 15));

        inputField = new JTextField();
        inputField.setFont(new Font("Serif", Font.PLAIN, 15));

        JButton loginButton = new JButton("Login");
        styleMainButton(loginButton);
        loginButton.addActionListener(e -> handleLogin());

        JButton registerButton = new JButton("Register");
        styleMainButton(registerButton);
        registerButton.addActionListener(e -> handleRegister());

        // panel للأزرار (Login / Register)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        // ترتيب العناصر داخل الكرت
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        card.add(customerRadio, gbc);

        gbc.gridx = 1;
        card.add(employeeRadio, gbc);

        gbc.gridx = 0; gbc.gridy++;
        card.add(inputLabel, gbc);

        gbc.gridx = 1;
        card.add(inputField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(buttonsPanel, gbc);

        return card;
    }

    private void styleMainButton(JButton btn) {
        btn.setBackground(new Color(74, 88, 105));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    }

    private void updateInputLabel() {
        if (customerRadio.isSelected()) {
            inputLabel.setText("Customer name:");
        } else {
            inputLabel.setText("Employee ID:");
        }
    }

    // ==================== LOGIN (نفسه تقريباً ما تغيّر) ====================
    private void handleLogin() {
        String text = inputField.getText().trim();

        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your " + (customerRadio.isSelected() ? "name." : "employee ID."),
                    "Missing information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (customerRadio.isSelected()) {
            // ====== CUSTOMER LOGIN ======
            String customerName = text;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT Cus_name FROM CUSTOMER WHERE Cus_name = ?")) {

                ps.setString(1, customerName);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Customer not found in database.",
                            "Login error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Database error while checking customer.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Welcome, " + customerName + "!",
                    "Login successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            CustomerDashboard cd = new CustomerDashboard(customerName);
            cd.setVisible(true);

        } else {
            // ====== EMPLOYEE LOGIN ======
            int empId;
            try {
                empId = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Employee ID must be a number.",
                        "Input error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String empName = null;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT Emp_name FROM EMPLOYEE WHERE Emp_id = ?")) {

                ps.setInt(1, empId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Employee not found in database.",
                            "Login error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                empName = rs.getString("Emp_name");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Database error while checking employee.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Welcome, " + empName + " (ID " + empId + ")",
                    "Login successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            EmployeeDashboard ed = new EmployeeDashboard(empId, empName);
            ed.setVisible(true);
        }

        // نقفل شاشة اللوق إن بعد ما نفتح البورتل
        dispose();
    }

    // ==================== REGISTER (الجديد) ====================
    private void handleRegister() {
        if (customerRadio.isSelected()) {
            registerCustomer();
        } else {
            registerEmployee();
        }
    }

    private void registerCustomer() {
        JTextField nameField   = new JTextField();
        JTextField emailField  = new JTextField();
        JTextField mobileField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Mobile:"));
        panel.add(mobileField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Customer registration",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String name   = nameField.getText().trim();
        String email  = emailField.getText().trim();
        String mobile = mobileField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required.",
                    "Input error",
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
                    "Customer registered successfully! You can now login.",
                    "Registered",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLIntegrityConstraintViolationException dup) {
            JOptionPane.showMessageDialog(this,
                    "Customer already exists (name or email in use).",
                    "Register error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error while registering customer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerEmployee() {
        JTextField idField   = new JTextField();
        JTextField nameField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Employee ID:"));
        panel.add(idField);
        panel.add(new JLabel("Employee name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Employee registration",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String idText = idField.getText().trim();
        String name   = nameField.getText().trim();

        if (idText.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Both ID and name are required.",
                    "Input error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int empId;
        try {
            empId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID must be a number.",
                    "Input error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO EMPLOYEE (Emp_id, Emp_name) VALUES (?, ?)")) {

            ps.setInt(1, empId);
            ps.setString(2, name);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Employee registered successfully! You can now login.",
                    "Registered",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLIntegrityConstraintViolationException dup) {
            JOptionPane.showMessageDialog(this,
                    "This employee ID already exists.",
                    "Register error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error while registering employee.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // هذا الميثود ما عدنا نحتاجه فعلياً، لكن لو حبيتي تخليه براحتك
    private String getEmployeeName(String text) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Emp_name FROM EMPLOYEE WHERE Emp_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(text));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("Emp_name");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EjarLogin frame = new EjarLogin();
            frame.setVisible(true);
        });
    }
}