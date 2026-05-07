import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.swing.JFrame;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CustomerDashboard extends JFrame {

    // ===== THEME COLORS =====
    private static final Color CARD_GREY     = new Color(230, 233, 238, 230);
    private static final Color BUTTER_YELLOW = new Color(245, 210, 90);  // header + card headers + borders
    private static final Color BUTTON_DARK   = new Color(40, 40, 40);    // bottom buttons
    private static final Color TEXT_DARK     = new Color(20, 20, 20);

    // Tables
    private JTable equipmentTable;

    // Booking fields
    private JTextField equipIdField;
    private JTextField startField;
    private JTextField endField;
    private JTextField cityField;
    private JTextField zipField;

    private String customerName;
    private boolean newlyRegistered;   // true only when just registered
    private Connection conn;

    private JLabel customerInfoLabel;

    // NEW: keep references so we can add Logout later
    private JPanel bottomBar;
    private JButton logoutBtn;

    // ===================== CONSTRUCTOR =====================

    public CustomerDashboard(String customerName, boolean newlyRegistered) {
        this.customerName = customerName;
        this.newlyRegistered = newlyRegistered;

        setTitle("EJAR - Customer Portal");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String bgPath =  "/images/dashboard.jpg";
        BackgroundPanel bg = new BackgroundPanel(bgPath);
        setContentPane(bg);
        bg.setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);

        // keep reference to bottom bar
        bottomBar = buildBottomBar();
        add(bottomBar, BorderLayout.SOUTH);

        try {
            conn = DBConnection.getConnection();
            loadAvailableFromDB();
            loadCustomerInfoFromDB();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB connection error");
        }

        if (this.newlyRegistered) {
            JOptionPane.showMessageDialog(
                    this,
                    "Welcome, " + customerName +
                            "\nYou must create at least one rental now.",
                    "EJAR Notice",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // ===================== HEADER =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BUTTER_YELLOW);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("EJAR – Customer Portal", SwingConstants.LEFT);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(Color.BLACK); // black text

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome, " + customerName, SwingConstants.RIGHT);
        welcome.setFont(new Font("Serif", Font.BOLD, 20));
        welcome.setForeground(Color.BLACK); // black text
        welcome.setAlignmentX(Component.RIGHT_ALIGNMENT);

        customerInfoLabel = new JLabel("", SwingConstants.RIGHT);
        customerInfoLabel.setFont(new Font("Serif", Font.PLAIN, 16));
        customerInfoLabel.setForeground(Color.BLACK); // black text
        customerInfoLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(welcome);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(customerInfoLabel);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // ===================== MAIN AREA =====================

    private JPanel buildMainArea() {
        // make boxes a bit longer again (smaller bottom padding)
        JPanel main = new JPanel(new GridLayout(1, 2, 15, 0));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(15, 20, 90, 20)); // was 120 → now 90 so cards are taller

        main.add(buildAvailableEquipment());
        main.add(buildBookingPanel());

        return main;
    }

    private JPanel buildAvailableEquipment() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(BUTTER_YELLOW, 2));

        JLabel title = new JLabel("Available Equipments", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setOpaque(true);
        title.setBackground(BUTTER_YELLOW);
        title.setForeground(Color.BLACK); // black text
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Equip ID", "Type", "Model", "Price"};
        equipmentTable = new JTable(new DefaultTableModel(cols, 0));
        equipmentTable.setRowHeight(22);

        equipmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && equipmentTable.getSelectedRow() != -1) {
                equipIdField.setText(
                        equipmentTable.getValueAt(equipmentTable.getSelectedRow(), 0).toString()
                );
            }
        });

        card.add(new JScrollPane(equipmentTable), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildBookingPanel() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(BUTTER_YELLOW, 2));

        JLabel title = new JLabel("New Booking", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setOpaque(true);
        title.setBackground(BUTTER_YELLOW);
        title.setForeground(Color.BLACK); // black text
        card.add(title, BorderLayout.NORTH);

        // form area
        JPanel form = new JPanel(new GridLayout(5, 2, 12, 12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        equipIdField = new JTextField();
        equipIdField.setEditable(false);

        startField = new JTextField();
        endField   = new JTextField();
        cityField  = new JTextField();
        zipField   = new JTextField();

        // Make text fields visually bigger
        Dimension fieldSize = new Dimension(280, 34);
        Font fieldFont = new Font("Serif", Font.PLAIN, 16);
        equipIdField.setPreferredSize(fieldSize);
        startField.setPreferredSize(fieldSize);
        endField.setPreferredSize(fieldSize);
        cityField.setPreferredSize(fieldSize);
        zipField.setPreferredSize(fieldSize);

        equipIdField.setFont(fieldFont);
        startField.setFont(fieldFont);
        endField.setFont(fieldFont);
        cityField.setFont(fieldFont);
        zipField.setFont(fieldFont);

        // Bigger label fonts
        Font labelFont = new Font("Serif", Font.BOLD, 16);
        JLabel l1 = new JLabel("Equipment ID:");
        JLabel l2 = new JLabel("Start Date: YYYY-MM-DD");
        JLabel l3 = new JLabel("End Date: YYYY-MM-DD");
        JLabel l4 = new JLabel("City:");
        JLabel l5 = new JLabel("Zip Code:");

        l1.setForeground(TEXT_DARK);
        l2.setForeground(TEXT_DARK);
        l3.setForeground(TEXT_DARK);
        l4.setForeground(TEXT_DARK);
        l5.setForeground(TEXT_DARK);

        l1.setFont(labelFont);
        l2.setFont(labelFont);
        l3.setFont(labelFont);
        l4.setFont(labelFont);
        l5.setFont(labelFont);

        form.add(l1);
        form.add(equipIdField);

        form.add(l2);
        form.add(startField);

        form.add(l3);
        form.add(endField);

        form.add(l4);
        form.add(cityField);

        form.add(l5);
        form.add(zipField);

        card.add(form, BorderLayout.CENTER);

        // Confirm Booking button (a bit smaller than before)
        JButton bookBtn = new JButton("Confirm Booking");
        bookBtn.setBackground(BUTTER_YELLOW);          // butter yellow
        bookBtn.setForeground(Color.BLACK);            // black text
        bookBtn.setFont(new Font("Serif", Font.BOLD, 20));
        bookBtn.setFocusPainted(false);
        bookBtn.setBorder(BorderFactory.createEmptyBorder(6, 26, 6, 26));
        bookBtn.setPreferredSize(new Dimension(240, 42));
        bookBtn.addActionListener(e -> handleBooking());

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(bookBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    // ===================== BOTTOM BAR =====================

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 120, 0));

        JButton rentalsBtn = makeBtn("My Rentals");
        rentalsBtn.addActionListener(e -> openMyRentals());

        JButton cancelBtn = makeBtn("Cancel Booking");
        cancelBtn.addActionListener(e -> performCancelBooking());

        JButton paymentsBtn = makeBtn("My Payments");
        paymentsBtn.addActionListener(e -> openMyPayments());

        JButton updateInfoBtn = makeBtn("Update My Info");
        updateInfoBtn.addActionListener(e -> openUpdateInfoWindow());

        bar.add(rentalsBtn);
        bar.add(cancelBtn);
        bar.add(paymentsBtn);
        bar.add(updateInfoBtn);

        // Create Logout button and keep reference
        logoutBtn = makeBtn("Logout");
        logoutBtn.addActionListener(e -> logoutAndReturnToFirstPage());

        // For existing customers → show logout from the start
        if (!newlyRegistered) {
            bar.add(logoutBtn);
        }

        return bar;
    }

    private JButton makeBtn(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BUTTON_DARK);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Serif", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btn.setPreferredSize(new Dimension(210, 55));
        return btn;
    }

    // ===================== LOGOUT =====================

    private void logoutAndReturnToFirstPage() {
        // close this dashboard window
        dispose();

        // go back to your login screen
        SwingUtilities.invokeLater(() -> {
            new EjarLogin().setVisible(true);
        });
    }

    // ===================== BOOKING LOGIC =====================

    private void handleBooking() {
        String equipId = equipIdField.getText().trim();
        String start   = startField.getText().trim();
        String end     = endField.getText().trim();
        String city    = cityField.getText().trim();
        String zip     = zipField.getText().trim();

        if (equipId.isEmpty() || start.isEmpty() || end.isEmpty()
                || city.isEmpty() || zip.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please fill all fields and select an equipment.",
                    "Missing info",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate s = LocalDate.parse(start);
            LocalDate e = LocalDate.parse(end);
            long days = ChronoUnit.DAYS.between(s, e);
            if (days <= 0) {
                JOptionPane.showMessageDialog(this,
                        "End date must be after start date.",
                        "Date error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double pricePerDay = 0;
            String priceSql = "SELECT Price FROM EQUIPMENT WHERE Equip_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(priceSql)) {
                ps.setInt(1, Integer.parseInt(equipId));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    pricePerDay = rs.getDouble("Price");
                } else {
                    JOptionPane.showMessageDialog(this, "Equipment not found in DB");
                    return;
                }
            }

            double totalPrice = pricePerDay * days;

            conn.setAutoCommit(false);

            int rentalId  = getNextId("RENTAL",  "Rental_id");
            int invoiceId = getNextId("PAYMENT", "Invoice");

            String rentalSql =
                    "INSERT INTO RENTAL " +
                            "(Rental_id, Start_date, End_date, City, Zip_code, Employee_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(rentalSql)) {
                ps.setInt(1, rentalId);
                ps.setString(2, start);
                ps.setString(3, end);
                ps.setString(4, city);
                ps.setString(5, zip);
                ps.setNull(6, java.sql.Types.INTEGER); // Employee_id = NULL
                ps.executeUpdate();
            }

            String paySql =
                    "INSERT INTO PAYMENT (Invoice, Pay_date, Total_price, P_rental) " +
                            "VALUES (?, CURRENT_DATE(), ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(paySql)) {
                ps.setInt(1, invoiceId);
                ps.setDouble(2, totalPrice);
                ps.setInt(3, rentalId);
                ps.executeUpdate();
            }

            String makesSql = "INSERT INTO MAKES (C_name, Equ_id, Rent_id) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(makesSql)) {
                ps.setString(1, customerName);
                ps.setInt(2, Integer.parseInt(equipId));
                ps.setInt(3, rentalId);
                ps.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this,
                    "Booking created successfully!\nTotal price: " + totalPrice + " SAR");

            loadAvailableFromDB();

            // After first successful booking, allow logout for newly registered customers
            if (newlyRegistered) {
                newlyRegistered = false;  // condition satisfied

                if (bottomBar != null && logoutBtn != null) {
                    bottomBar.add(logoutBtn);   // add Logout button to bar
                    bottomBar.revalidate();     // refresh layout
                    bottomBar.repaint();
                }
            }

        } catch (Exception ex) {
            try { conn.rollback(); } catch (Exception ignore) {}
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while creating booking");
        }
    }

    private int getNextId(String tableName, String idColumn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(" + idColumn + "), 0) + 1 AS next_id FROM " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 1;
    }

    // ===================== OTHER BUTTONS =====================

    private void openMyRentals() {
        String sql = "SELECT R.Rental_id, R.Start_date, R.End_date, R.City, R.Zip_code " +
                "FROM RENTAL R JOIN MAKES M ON R.Rental_id = M.Rent_id " +
                "WHERE M.C_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerName);
            ResultSet rs = ps.executeQuery();

            String[] cols = {"Rental ID", "Start", "End", "City", "Zip"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Rental_id"),
                        rs.getString("Start_date"),
                        rs.getString("End_date"),
                        rs.getString("City"),
                        rs.getString("Zip_code")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(600, 220));

            JOptionPane.showMessageDialog(this, scroll, "My Rentals", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading rentals");
        }
    }

    private void openMyPayments() {
        String sql = "SELECT P.Invoice, P.Pay_date, P.Total_price " +
                "FROM PAYMENT P " +
                "JOIN RENTAL R ON P.P_rental = R.Rental_id " +
                "JOIN MAKES M ON R.Rental_id = M.Rent_id " +
                "WHERE M.C_name = ? ORDER BY P.Pay_date DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerName);
            ResultSet rs = ps.executeQuery();

            String[] cols = {"Invoice", "Pay Date", "Total Price"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Invoice"),
                        rs.getString("Pay_date"),
                        rs.getDouble("Total_price")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(500, 220));

            JOptionPane.showMessageDialog(this, scroll, "My Payments", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading payments");
        }
    }

    private void performCancelBooking() {
        String rentalStr = JOptionPane.showInputDialog(this,
                "Enter Rental ID to cancel:");
        if (rentalStr == null || rentalStr.trim().isEmpty()) return;

        int rentalId;
        try {
            rentalId = Integer.parseInt(rentalStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Rental ID.");
            return;
        }

        try {
            conn.setAutoCommit(false);

            String checkSql = "SELECT Equ_id FROM MAKES WHERE Rent_id = ? AND C_name = ?";
            int equipId = -1;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, rentalId);
                ps.setString(2, customerName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        equipId = rs.getInt("Equ_id");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Rental does not belong to this customer.");
                        conn.setAutoCommit(true);
                        return;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE PAYMENT SET Total_price = 0 WHERE P_rental = ?")) {
                ps.setInt(1, rentalId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM MAKES WHERE Rent_id = ?")) {
                ps.setInt(1, rentalId);
                ps.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
            loadAvailableFromDB();

        } catch (Exception ex) {
            try { conn.rollback(); } catch (Exception ignore) {}
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while cancelling booking");
        }
    }

    private void openUpdateInfoWindow() {
        JTextField mobileField = new JTextField();
        JTextField emailField  = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("New mobile:"));
        panel.add(mobileField);
        panel.add(new JLabel("New email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Update My Info", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String mobile = mobileField.getText().trim();
            String email  = emailField.getText().trim();

            if (mobile.isEmpty() && email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nothing to update.");
                return;
            }

            try {
                String sql = "UPDATE CUSTOMER SET " +
                        "Cus_mobile = IF(? = '', Cus_mobile, ?), " +
                        "Email = IF(? = '', Email, ?) " +
                        "WHERE Cus_name = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, mobile);
                    ps.setString(2, mobile);
                    ps.setString(3, email);
                    ps.setString(4, email);
                    ps.setString(5, customerName);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Info updated successfully!");
                        loadCustomerInfoFromDB();
                    } else {
                        JOptionPane.showMessageDialog(this, "Customer not found.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating info");
            }
        }
    }

    private void loadAvailableFromDB() {

        String sql =
            "SELECT Equip_id, Type, Model, Price " +
            "FROM EQUIPMENT1 " +
            "WHERE Status = 'available'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel m = (DefaultTableModel) equipmentTable.getModel();
            m.setRowCount(0);

            while (rs.next()) {
                m.addRow(new Object[]{
                        rs.getInt("Equip_id"),
                        rs.getString("Type"),
                        rs.getString("Model"),
                        rs.getDouble("Price")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading available equipment");
        }
    }

    private void loadCustomerInfoFromDB() {
        if (conn == null) return;

        String sql = "SELECT Cus_mobile, Email FROM CUSTOMER WHERE Cus_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String mobile = rs.getString("Cus_mobile");
                    String email  = rs.getString("Email");

                    if (mobile == null) mobile = "";
                    if (email  == null) email  = "";

                    String text = "Mobile: " + mobile;
                    if (!email.isEmpty()) {
                        text += "    |    Email: " + email;
                    }
                    customerInfoLabel.setText(text);
                } else {
                    customerInfoLabel.setText("Customer info not found.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            customerInfoLabel.setText("Error loading customer info.");
        }
    }

}