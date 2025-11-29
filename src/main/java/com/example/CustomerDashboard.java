
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.swing.JFrame;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CustomerDashboard extends JFrame {

    // Theme colors (للبطاقات والازرار فوق الخلفية)
    private static final Color CARD_GREY   = new Color(230, 233, 238, 230);
    private static final Color HEADER_GREY = new Color(96, 111, 131, 230);
    private static final Color BUTTON_DARK = new Color(74, 88, 105);
    private static final Color TEXT_DARK   = new Color(40, 40, 40);

    // Tables
    private JTable equipmentTable;

    // Booking fields
    private JTextField equipIdField;
    private JTextField startField;
    private JTextField endField;
    private JTextField cityField;
    private JTextField zipField;

    // اسم العميل (يجي من شاشة اللوق إن)
    private String customerName;

    // اتصال قاعدة البيانات
    private Connection conn;

    // لعرض معلومات العميل تحت Welcome
    private JLabel customerInfoLabel;

    public CustomerDashboard(String customerName) {
        this.customerName = customerName;   // نخزن الاسم اللي دخل في اللوق إن

        setTitle("EJAR - Customer Portal");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String bgPath = "C:/Users/Joory/Downloads/WhatsApp Image 2025-11-28 at 02.20.17.jpeg";
        BackgroundPanel bg = new BackgroundPanel(bgPath);
        setContentPane(bg);
        bg.setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        try {
            conn = DBConnection.getConnection();
            loadAvailableFromDB();
            loadCustomerInfoFromDB();   // نعبّي معلومات الكستمر ونحطها في الليبل
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB connection error");
        }

        // ======= الرسالة اللي تطلع أول ما يدخل الكستمر =======
        JOptionPane.showMessageDialog(
                this,
                "Welcome, " + customerName +
                        "\nYou must create at least one rental now.",
                "EJAR Notice",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ===================== HEADER =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_GREY);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("EJAR – Customer Portal", SwingConstants.LEFT);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        // البانل اللي على اليمين فيه Welcome + معلومات العميل بخط صغير
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome, " + customerName, SwingConstants.RIGHT);
        welcome.setFont(new Font("Serif", Font.BOLD, 16));
        welcome.setForeground(Color.WHITE);
        welcome.setAlignmentX(Component.RIGHT_ALIGNMENT);

        customerInfoLabel = new JLabel("", SwingConstants.RIGHT);
        customerInfoLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        customerInfoLabel.setForeground(Color.WHITE);
        customerInfoLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(welcome);
        rightPanel.add(customerInfoLabel);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // ===================== MAIN AREA =====================

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new GridLayout(1, 2, 15, 0));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        main.add(buildAvailableEquipment());
        main.add(buildBookingPanel());

        return main;
    }

    private JPanel buildAvailableEquipment() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(HEADER_GREY, 2));

        JLabel title = new JLabel("Available Equipments", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setOpaque(true);
        title.setBackground(HEADER_GREY);
        title.setForeground(Color.WHITE);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Equip ID", "Type", "Model", "Price", "Status"};
        equipmentTable = new JTable(new DefaultTableModel(cols, 0));
        equipmentTable.setRowHeight(22);

        // لما يختار صف من الجدول → نعبي رقم المعدة في فورم الحجز
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
        card.setBorder(BorderFactory.createLineBorder(HEADER_GREY, 2));

        JLabel title = new JLabel("New Booking", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setOpaque(true);
        title.setBackground(HEADER_GREY);
        title.setForeground(Color.WHITE);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        equipIdField = new JTextField();
        equipIdField.setEditable(false);

        startField = new JTextField();
        endField   = new JTextField();
        cityField  = new JTextField();
        zipField   = new JTextField();

        form.add(new JLabel("Equipment ID:"));
        form.add(equipIdField);

        form.add(new JLabel("Start Date:"));
        form.add(startField);

        form.add(new JLabel("End Date:"));
        form.add(endField);

        form.add(new JLabel("City:"));
        form.add(cityField);

        form.add(new JLabel("Zip Code:"));
        form.add(zipField);

        card.add(form, BorderLayout.CENTER);

        JButton bookBtn = new JButton("Confirm Booking");
        bookBtn.setBackground(BUTTON_DARK);
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Serif", Font.BOLD, 16));
        bookBtn.setFocusPainted(false);
        bookBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        bookBtn.addActionListener(e -> handleBooking());

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(bookBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    // ===================== BOTTOM BAR =====================

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bar.setOpaque(false);

        JButton rentalsBtn = makeBtn("My Rentals");
        rentalsBtn.addActionListener(e -> openMyRentals());

        JButton cancelBtn = makeBtn("Cancel Booking");
        cancelBtn.addActionListener(e -> performCancelBooking());

        JButton paymentsBtn = makeBtn("My Payments");
        paymentsBtn.addActionListener(e -> openMyPayments());

        JButton updateInfoBtn = makeBtn("Update My Info");
        updateInfoBtn.addActionListener(e -> openUpdateInfoWindow());

        // ما في زر Back
        bar.add(rentalsBtn);
        bar.add(cancelBtn);
        bar.add(paymentsBtn);
        bar.add(updateInfoBtn);

        return bar;
    }

    private JButton makeBtn(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BUTTON_DARK);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Serif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return btn;
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

            // 👇👇 التعديل هنا: شلنا Employee_id من الأعمدة
            String rentalSql =
        "INSERT INTO RENTAL " +
        "(Rental_id, Start_date, End_date, City, Zip_code, Employee_id) " +
        "VALUES (?, ?, ?, ?, ?, ?)"; // add ? for Employee_id

try (PreparedStatement ps = conn.prepareStatement(rentalSql)) {
    ps.setInt(1, rentalId);
    ps.setString(2, start);
    ps.setString(3, end);
    ps.setString(4, city);
    ps.setString(5, zip);

    // Set Employee_id to NULL
    ps.setNull(6, java.sql.Types.INTEGER); // <-- this inserts SQL NULL

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

            // String updateEquip = "UPDATE EQUIPMENT SET Status = 'Rented' WHERE Equip_id = ?";
            // try (PreparedStatement ps = conn.prepareStatement(updateEquip)) {
            //     ps.setInt(1, Integer.parseInt(equipId));
            //     ps.executeUpdate();
            // }

            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this,
                    "Booking created successfully!\nTotal price: " + totalPrice + " SAR");

            loadAvailableFromDB();

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

            // try (PreparedStatement ps = conn.prepareStatement(
            //         "DELETE FROM RENTAL WHERE Rental_id = ?")) {
            //     ps.setInt(1, rentalId);
            //     ps.executeUpdate();
            // }

            // try (PreparedStatement ps = conn.prepareStatement(
            //         "UPDATE EQUIPMENT SET Status = 'Available' WHERE Equip_id = ?")) {
            //     ps.setInt(1, equipId);
            //     ps.executeUpdate();
            // }

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
                        // نحدّث الليبل فوق بعد ما يعدّل بياناته
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

    // ===================== LOAD AVAILABLE EQUIP =====================

    private void loadAvailableFromDB() {
        // نستخدم الــ view اللي اسمه equipment1
        String sql = "SELECT id, type, model, price, stat " +
                     "FROM equipment1 " +
                     "WHERE stat = 'available'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel m = (DefaultTableModel) equipmentTable.getModel();
            m.setRowCount(0);

            while (rs.next()) {
                m.addRow(new Object[]{
                        rs.getInt("id"),        // Equip_id
                        rs.getString("type"),   // Type
                        rs.getString("model"),  // Model
                        rs.getDouble("price"),  // Price
                        rs.getString("stat")    // Status
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading available equipment");
        }
    }

    // ===================== LOAD CUSTOMER INFO =====================

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