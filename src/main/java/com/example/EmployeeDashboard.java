

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmployeeDashboard extends JFrame {

    // Theme colors
    private static final Color CARD_GREY   = new Color(230, 233, 238, 230);
    private static final Color HEADER_GREY = new Color(96, 111, 131, 230);
    private static final Color BUTTON_DARK = new Color(74, 88, 105);

    // Tables
    private JTable myEquipTable;
    private JTable rentalsTable;

    // Employee info
    private final String employeeName;
    private final int employeeId;

    // DB connection للكلاس كامل
    private Connection conn;

    public EmployeeDashboard(int employeeId, String employeeName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;

        setTitle("EJAR - Employee Portal");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String bgPath = "C:/Users/norah/Downloads/backg.jpeg";
        BackgroundPanel bg = new BackgroundPanel(bgPath);
        setContentPane(bg);
        bg.setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenterArea(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        try {
            this.conn = DBConnection.getConnection();
            loadManagedEquipment();
            loadAllRentals();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "DB connection error (Employee)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= HEADER =================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_GREY);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("EJAR – Employee Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel info = new JLabel("ID: " + employeeId + "   |   " + employeeName,
                SwingConstants.RIGHT);
        info.setFont(new Font("Serif", Font.BOLD, 16));
        info.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(info, BorderLayout.EAST);

        return header;
    }

    // ================= CENTER =================
    private JPanel buildCenterArea() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Equipment card
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 0.6;
        center.add(buildMyEquipCard(), gbc);

        // Rentals card
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 0.6;
        center.add(buildRentalsCard(), gbc);

        // My Info card
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 0.6;
        center.add(buildMyInfoCard(), gbc);

        return center;
    }

    // ============ EQUIPMENT CARD ============
    private JPanel buildMyEquipCard() {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(HEADER_GREY, 2));
        card.setPreferredSize(new Dimension(360, 240));

        JLabel header = new JLabel("Equipment", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(HEADER_GREY);
        header.setForeground(Color.WHITE);
        card.add(header, BorderLayout.NORTH);

        // نستخدم الـ VIEW equipment1 عشان نجيب الـ Status (stat)
        String[] cols = {"Equip ID", "Type", "Model", "Price", "Status"};
        myEquipTable = new JTable(new DefaultTableModel(cols, 0));
        myEquipTable.setRowHeight(22);
        card.add(new JScrollPane(myEquipTable), BorderLayout.CENTER);

        // ثلاثة أزرار: Add / Update / Delete
     // === Buttons ===
        JButton addBtn = new JButton("Add New Equipment");
        JButton editBtn = new JButton("Update / Delete");

        styleMainButton(addBtn);
        styleMainButton(editBtn);

        addBtn.addActionListener(e -> addNewEquipment());
        editBtn.addActionListener(e -> handleManageEquipment());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(addBtn);
        bottom.add(editBtn);

        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // ============ RENTALS CARD ============
    private JPanel buildRentalsCard() {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(HEADER_GREY, 2));
        card.setPreferredSize(new Dimension(420, 260));

        JLabel header = new JLabel("Rentals", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(HEADER_GREY);
        header.setForeground(Color.WHITE);
        card.add(header, BorderLayout.NORTH);

        String[] cols = {
                "Rental ID", "Customer", "Email",
                "Start", "End", "Equip ID",
                "Total Price", "Emp ID"
        };
        rentalsTable = new JTable(new DefaultTableModel(cols, 0));
        rentalsTable.setRowHeight(22);
        card.add(new JScrollPane(rentalsTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonsPanel.setOpaque(false);

        JButton superviseBtn   = new JButton("Supervise a rental");
        JButton mySuperviseBtn = new JButton("Rentals I supervise");
        JButton viewContactBtn = new JButton("View Customer Contact");

        styleMainButton(superviseBtn);
        styleMainButton(mySuperviseBtn);
        styleMainButton(viewContactBtn);

        superviseBtn.addActionListener(e -> assignRentalToEmployee());
        mySuperviseBtn.addActionListener(e -> showRentalsISupervise());
        viewContactBtn.addActionListener(e -> viewCustomerContactForSelectedRental());

        buttonsPanel.add(superviseBtn);
        buttonsPanel.add(mySuperviseBtn);
        buttonsPanel.add(viewContactBtn);

        card.add(buttonsPanel, BorderLayout.SOUTH);

        return card;
    }

    // ============ MY INFO CARD ============
    private JPanel buildMyInfoCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(HEADER_GREY, 2));
        card.setPreferredSize(new Dimension(220, 180));

        JLabel header = new JLabel("My Info", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 18));
        header.setOpaque(true);
        header.setBackground(HEADER_GREY);
        header.setForeground(Color.WHITE);
        card.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel idLabel   = new JLabel("Employee ID: " + employeeId);
        JLabel nameLabel = new JLabel("Name: " + employeeName);
        idLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        nameLabel.setFont(new Font("Serif", Font.PLAIN, 15));

        content.add(idLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(nameLabel);

        JButton deleteMeBtn = new JButton("Delete My Record");
        deleteMeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleMainButton(deleteMeBtn);
        deleteMeBtn.addActionListener(e -> deleteMyEmployeeRecord());

        content.add(Box.createVerticalStrut(15));
        content.add(deleteMeBtn);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    // ================= BOTTOM BAR =================
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bar.setOpaque(false);

        JButton allEquipBtn = bigNavButton("All Equipments");
        allEquipBtn.addActionListener(e -> showAllEquipments());

        JButton paymentsBtn = bigNavButton("Payments I supervise");
        paymentsBtn.addActionListener(e -> showPaymentsForMyRentals());

        JButton logoutBtn = bigNavButton("Logout");
        logoutBtn.addActionListener(e -> dispose());

        bar.add(allEquipBtn);
        bar.add(paymentsBtn);
        bar.add(logoutBtn);

        return bar;
    }

    // ============ STYLING HELPERS ============
    private void styleMainButton(JButton btn) {
        btn.setBackground(BUTTON_DARK);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private JButton bigNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BUTTON_DARK);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Serif", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    // ============ DB LOADERS ============

    // كل المعدات – من الـ VIEW equipment1 (فيه stat)
    private void loadManagedEquipment() {
        String sql = "SELECT id, type, model, price, stat FROM equipment1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = (DefaultTableModel) myEquipTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("model"),
                        rs.getDouble("price"),
                        rs.getString("stat")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading equipment list");
        }
    }

    // كل الإيجارات مع اسم العميل والإيميل والتوتال برايس من PAYMENT
    private void loadAllRentals() {
        String sql =
                "SELECT R.Rental_id, " +
                "       C.Cus_name AS Customer, " +
                "       C.Email    AS Email, " +
                "       R.Start_date, " +
                "       R.End_date, " +
                "       M.Equ_id   AS Equip_id, " +
                "       P.Total_price, " +
                "       R.Employee_id " +
                "FROM RENTAL R " +
                "JOIN MAKES M    ON R.Rental_id = M.Rent_id " +
                "JOIN CUSTOMER C ON M.C_name    = C.Cus_name " +
                "LEFT JOIN PAYMENT P ON P.P_rental = R.Rental_id";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = (DefaultTableModel) rentalsTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Rental_id"),
                        rs.getString("Customer"),
                        rs.getString("Email"),
                        rs.getString("Start_date"),
                        rs.getString("End_date"),
                        rs.getInt("Equip_id"),
                        rs.getDouble("Total_price"),
                        rs.getObject("Employee_id")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading rentals");
        }
    }

    // ============ EQUIPMENT OPERATIONS ============

    // إضافة معدّة جديدة
    private void addNewEquipment() {
        JTextField typeField  = new JTextField();
        JTextField modelField = new JTextField();
        JTextField priceField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Type:"));
        panel.add(typeField);
        panel.add(new JLabel("Model:"));
        panel.add(modelField);
        panel.add(new JLabel("Price (per day):"));
        panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add New Equipment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String type  = typeField.getText().trim();
        String model = modelField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (type.isEmpty() || model.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price.");
            return;
        }

        try {
            int newId = getNextId("EQUIPMENT", "Equip_id");

            String sql = "INSERT INTO EQUIPMENT (Equip_id, Type, Model, Price) " +
                         "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newId);
                ps.setString(2, type);
                ps.setString(3, model);
                ps.setDouble(4, price);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Equipment added with ID " + newId);
            loadManagedEquipment();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding equipment");
        }
    }

    // تحديث السعر بالـ ID – مسموح فقط لو الموظف Manager للمعدّة
    private void handleUpdateEquipmentById() {
        String idStr = JOptionPane.showInputDialog(this,
                "Enter Equip ID to update price:");
        if (idStr == null || idStr.trim().isEmpty()) return;

        int equipId;
        try {
            equipId = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Equip ID.");
            return;
        }

        if (!isEmployeeManagerOfEquip(equipId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are not supervising any rental for this equipment.",
                    "Not allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String priceStr = JOptionPane.showInputDialog(this,
                "Enter new price per day:");
        if (priceStr == null || priceStr.trim().isEmpty()) return;

        double newPrice;
        try {
            newPrice = Double.parseDouble(priceStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE EQUIPMENT SET Price = ? WHERE Equip_id = ?")) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, equipId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Price updated successfully.");
                loadManagedEquipment();
            } else {
                JOptionPane.showMessageDialog(this, "Equipment not found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating price.");
        }
    }

    // حذف المعدّة – مسموح فقط إذا الموظف مانجر عليها والمعدّة غير مستخدمة في MAKES
    private void handleDeleteEquipmentById() {
        String idStr = JOptionPane.showInputDialog(this,
                "Enter Equip ID to delete:");
        if (idStr == null || idStr.trim().isEmpty()) return;

        int equipId;
        try {
            equipId = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Equip ID.");
            return;
        }

        if (!isEmployeeManagerOfEquip(equipId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are not supervising any rental for this equipment.",
                    "Not allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // نتأكد أنها ليست مستخدمة في MAKES (يعني مو rented)
        String checkSql =
                "SELECT COUNT(*) AS cnt " +
                "FROM MAKES WHERE Equ_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, equipId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "This equipment is used in rentals and cannot be deleted.",
                            "Not allowed",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking rentals for equipment.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete equipment #" + equipId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM EQUIPMENT WHERE Equip_id = ?")) {
            ps.setInt(1, equipId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Equipment deleted.");
                loadManagedEquipment();
            } else {
                JOptionPane.showMessageDialog(this, "Equipment not found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting equipment.");
        }
    }

    // يتحقق أن الموظف الحالي Manager على أي رنتال فيها هذا الـ Equip_id
    private boolean isEmployeeManagerOfEquip(int equipId) {
        String sql =
                "SELECT COUNT(*) AS cnt " +
                "FROM RENTAL R " +
                "JOIN MAKES M ON R.Rental_id = M.Rent_id " +
                "WHERE R.Employee_id = ? AND M.Equ_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, equipId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    // ============ RENTAL OPERATIONS ============

    private void assignRentalToEmployee() {
        String rentalStr = JOptionPane.showInputDialog(this,
                "Enter Rental ID to supervise:");
        if (rentalStr == null || rentalStr.trim().isEmpty()) return;

        int rentalId;
        try {
            rentalId = Integer.parseInt(rentalStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Rental ID.");
            return;
        }

        String sql = "UPDATE RENTAL SET Employee_id = ? WHERE Rental_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, rentalId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "You now supervise rental #" + rentalId);
                loadAllRentals();
            } else {
                JOptionPane.showMessageDialog(this, "Rental not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error assigning rental");
        }
    }

    private void showRentalsISupervise() {
        String sql =
                "SELECT R.Rental_id, R.Start_date, R.End_date, " +
                "       R.City, R.Zip_code, P.Total_price " +
                "FROM RENTAL R " +
                "JOIN PAYMENT P ON P.P_rental = R.Rental_id " +
                "WHERE R.Employee_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            String[] cols = {"Rental ID", "Start", "End", "City", "Zip", "Total Price"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Rental_id"),
                        rs.getString("Start_date"),
                        rs.getString("End_date"),
                        rs.getString("City"),
                        rs.getString("Zip_code"),
                        rs.getDouble("Total_price")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(600, 220));

            JOptionPane.showMessageDialog(this, scroll,
                    "Rentals I supervise", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading supervised rentals");
        }
    }

    // عرض وسيلة التواصل مع العميل – الموبايل يظهر فقط لو الموظف مشرف
    private void viewCustomerContactForSelectedRental() {
        int row = rentalsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a rental first.");
            return;
        }

        int rentalId    = (int) rentalsTable.getValueAt(row, 0);
        String cusName  = rentalsTable.getValueAt(row, 1).toString();
        String email    = rentalsTable.getValueAt(row, 2).toString();
        Object empIdObj = rentalsTable.getValueAt(row, 7);

        boolean isSupervisor = (empIdObj != null &&
                                ((Number) empIdObj).intValue() == employeeId);

        String mobile = null;

        if (isSupervisor) {
            String sql = "SELECT Cus_mobile FROM CUSTOMER WHERE Cus_name = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cusName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        mobile = rs.getString("Cus_mobile");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Customer: ").append(cusName).append("\n");
        msg.append("Email: ").append(email).append("\n");
        if (isSupervisor && mobile != null) {
            msg.append("Mobile: ").append(mobile).append("\n");
        } else {
            msg.append("Mobile: (hidden – you do not supervise this rental)").append("\n");
        }

        JOptionPane.showMessageDialog(this, msg.toString(),
                "Customer Contact for Rental #" + rentalId,
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ============ OTHER VIEWS ============

    private void showAllEquipments() {
        String sql = "SELECT id, type, model, price, stat FROM equipment1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String[] cols = {"Equip ID", "Type", "Model", "Price", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("model"),
                        rs.getDouble("price"),
                        rs.getString("stat")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(700, 260));

            JOptionPane.showMessageDialog(this, scroll,
                    "All Equipments", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading all equipments");
        }
    }

    private void showPaymentsForMyRentals() {
        String sql =
                "SELECT P.Invoice, P.Pay_date, P.Total_price, P.P_rental " +
                "FROM PAYMENT P " +
                "JOIN RENTAL R ON P.P_rental = R.Rental_id " +
                "WHERE R.Employee_id = ? " +
                "ORDER BY P.Pay_date DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            String[] cols = {"Invoice", "Pay Date", "Total Price", "Rental ID"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Invoice"),
                        rs.getString("Pay_date"),
                        rs.getDouble("Total_price"),
                        rs.getInt("P_rental")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(600, 240));

            JOptionPane.showMessageDialog(this, scroll,
                    "Payments for my rentals", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading payments");
        }
    }

    // ============ EMPLOYEE DELETE ============

    private void deleteMyEmployeeRecord() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete your employee record?\n" +
                "You must not be supervising any rentals.",
                "Confirm delete", JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // هل يشرف على أي رنتال؟
            String checkSql = "SELECT COUNT(*) AS cnt FROM RENTAL WHERE Employee_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, employeeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("cnt") > 0) {
                        JOptionPane.showMessageDialog(
                                this,
                                "You are supervising rentals.\n" +
                                "You cannot be removed while supervising.",
                                "Cannot delete",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                }
            }

            // ما يشرف على شيء → نحذفه
            String delSql = "DELETE FROM EMPLOYEE WHERE Emp_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                ps.setInt(1, employeeId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Your employee record has been removed.\nGoodbye!",
                            "Deleted",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Employee record not found.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting employee record");
        }
    }
 // يفتح دايلوج بثلاث خيارات: Update price / Delete / Cancel
    private void handleManageEquipment() {
        String[] options = {"Update price", "Delete", "Cancel"};

        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose an action for equipment:",
                "Manage Equipment",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            // تحديث السعر بالـ ID  (الميثود اللي عندك أصلاً)
            handleUpdateEquipmentById();
        } else if (choice == 1) {
            // حذف المعدّة بالـ ID  (الميثود اللي عندك أصلاً)
            handleDeleteEquipmentById();
        }
        // لو Cancel → ما نسوي شيء
    }

    // ============ HELPERS ============

    private int getNextId(String table, String column) throws SQLException {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 AS next_id FROM " + table;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 1;
    }
}