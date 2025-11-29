
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

        // جدول المعدات
        String[] cols = {"Equip ID", "Type", "Model", "Price", "Status"};
        myEquipTable = new JTable(new DefaultTableModel(cols, 0));
        myEquipTable.setRowHeight(22);
        card.add(new JScrollPane(myEquipTable), BorderLayout.CENTER);

        // أزرار تحت: زر إدارة (Update/Delete) + زر Add New
        JButton manageBtn = new JButton("Update / Delete");
        styleMainButton(manageBtn);
        manageBtn.addActionListener(e -> handleUpdateOrDeleteEquipment());

        JButton addBtn = new JButton("Add New Equipment");
        styleMainButton(addBtn);
        addBtn.addActionListener(e -> addNewEquipment());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(manageBtn);
        bottom.add(addBtn);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }
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

        JButton superviseBtn = new JButton("Supervise a rental");
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

        JLabel idLabel = new JLabel("Employee ID: " + employeeId);
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

    // كل المعدات من EQUIPMENT
    private void loadManagedEquipment() {
        String sql = "SELECT Equip_id, Type, Model, Price, Status FROM EQUIPMENT";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = (DefaultTableModel) myEquipTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Equip_id"),
                        rs.getString("Type"),
                        rs.getString("Model"),
                        rs.getDouble("Price"),
                        rs.getString("Status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading equipment list");
        }
    }

    // كل الإيجارات مع اسم العميل والإيميل، بدون الموبايل
    private void loadAllRentals() {
        String sql =
                "SELECT R.Rental_id, " +
                "       C.Cus_name AS Company, " +
                "       C.Email    AS Email, " +
                "       R.Start_date, " +
                "       R.End_date, " +
                "       M.Equip_id   AS Equip_id, " +
                "       R.Tot_price, " +
                "       R.Employee_id " +
                "FROM RENTAL R " +
                "JOIN MAKES M ON R.Rental_id = M.Rent_id " +
                "JOIN CUSTOMER C ON M.C_name = C.Cus_name";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = (DefaultTableModel) rentalsTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Rental_id"),
                        rs.getString("Company"),
                        rs.getString("Email"),
                        rs.getString("Start_date"),
                        rs.getString("End_date"),
                        rs.getInt("Equip_id"),
                        rs.getDouble("Tot_price"),
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
            // نجيب ID جديد
            int newId = getNextId("EQUIPMENT", "Equip_id");

            String sql = "INSERT INTO EQUIPMENT (Equip_id, Type, Model, Price, Status) " +
                         "VALUES (?, ?, ?, ?, 'Available')";
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

    // Update/Delete مع شرط يكون المانجر على المعدّة
    private void handleUpdateOrDeleteEquipment() {
        int row = myEquipTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an equipment first.");
            return;
        }

        int equipId = (int) myEquipTable.getValueAt(row, 0);

        // أولاً: نتأكد أن هذا الموظف هو المانجر (يشرف على رنتال لهذه المعدّة)
        if (!isManagerForEquipment(equipId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are not the manager for this equipment.\n" +
                    "You can update or delete only equipments you supervise rentals for.",
                    "Not allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String currentType   = myEquipTable.getValueAt(row, 1).toString();
        String currentModel  = myEquipTable.getValueAt(row, 2).toString();
        String currentStatus = myEquipTable.getValueAt(row, 4).toString();

        JTextField typeField   = new JTextField(currentType);
        JTextField modelField  = new JTextField(currentModel);
        JTextField statusField = new JTextField(currentStatus);
        JTextField priceField  = new JTextField(); // optional

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Type:"));
        panel.add(typeField);
        panel.add(new JLabel("Model:"));
        panel.add(modelField);
        panel.add(new JLabel("Status:"));
        panel.add(statusField);
        panel.add(new JLabel("New Price (optional):"));
        panel.add(priceField);

        String[] options = {"Update", "Delete", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this, panel, "Update / Delete Equipment",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]
        );

        if (choice == 0) {  // Update
            String newType   = typeField.getText().trim();
            String newModel  = modelField.getText().trim();
            String newStatus = statusField.getText().trim();
            String newPrice  = priceField.getText().trim();

            if (newType.isEmpty() || newModel.isEmpty() || newStatus.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Type, Model and Status cannot be empty.");
                return;
            }

            try {
                String sql = "UPDATE EQUIPMENT SET Type = ?, Model = ?, Status = ?"
                           + (newPrice.isEmpty() ? "" : ", Price = ?")
                           + " WHERE Equip_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, newType);
                    ps.setString(i++, newModel);
                    ps.setString(i++, newStatus);
                    if (!newPrice.isEmpty()) {
                        ps.setDouble(i++, Double.parseDouble(newPrice));
                    }
                    ps.setInt(i, equipId);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Equipment updated successfully.");
                loadManagedEquipment();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating equipment");
            }

        } else if (choice == 1) {  // Delete
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Delete equipment #" + equipId + "?",
                    "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM EQUIPMENT WHERE Equip_id = ?")) {
                    ps.setInt(1, equipId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Equipment deleted.");
                    loadManagedEquipment();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting equipment");
                }
            }
        }
    }

    // الموظف Manager للمعدّة لو يشرف على أي رنتال تستخدمها
    private boolean isManagerForEquipment(int equipId) {
        String sql =
                "SELECT COUNT(*) AS cnt " +
                "FROM RENTAL R " +
                "JOIN MAKES M ON R.Rental_id = M.Rent_id " +
                "WHERE M.Equip_id = ? AND R.Employee_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, equipId);
            ps.setInt(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
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
                "SELECT R.Rental_id, R.Start_date, R.End_date, R.City, R.Zip_code, R.Tot_price " +
                "FROM RENTAL R WHERE R.Employee_id = ?";

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
                        rs.getDouble("Tot_price")
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

        int rentalId   = (int) rentalsTable.getValueAt(row, 0);
        String cusName = rentalsTable.getValueAt(row, 1).toString();
        String email   = rentalsTable.getValueAt(row, 2).toString();
        Object empIdObj = rentalsTable.getValueAt(row, 7);

        boolean isSupervisor = (empIdObj != null &&
                                ((Number) empIdObj).intValue() == employeeId);

        String mobile = null;

        if (isSupervisor) {
            // نجيب الموبايل فقط لو انه مشرف
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
        String sql = "SELECT Equip_id, Type, Model, Price, Status FROM EQUIPMENT";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String[] cols = {"Equip ID", "Type", "Model", "Price", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("Equip_id"),
                        rs.getString("Type"),
                        rs.getString("Model"),
                        rs.getDouble("Price"),
                        rs.getString("Status")
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
 // يفتح دايلوج بثلاث خيارات: Add / Update / Delete
    private void handleManageEquipment() {
        String[] options = {"Add new", "Update price", "Delete", "Cancel"};

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
            // إضافة معدّة جديدة (يستدعي كود الإضافة اللي عندك)
            addNewEquipment();     // لو كان اسم دالتك مختلف عدّليه هنا
        } else if (choice == 1) {
            handleUpdateEquipmentById();  // تحديث السعر
        } else if (choice == 2) {
            handleUpdateEquipmentById();  // حذف
        }
        // لو Cancel أو إغلاق الدايالوج → ولا شي
    }


    // يحدّث سعر معدّة فقط إذا كان هذا الموظف يشرف على رنتال لها
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

        // نتأكد أنه يشرف على معدّة بهذا الرقم
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
    private boolean isEmployeeManagerOfEquip(int equipId) {
		// TODO Auto-generated method stub
		return false;
	}
 // يحدّث سعر معدّة فقط إذا كان هذا الموظف يشرف على رنتال لها
    private void handleUpdateEquipmentById1() {
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

        // نتأكد أنه يشرف على معدّة بهذا الرقم
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

        // نتأكد أنه يشرف على معدّة بهذا الرقم
        if (!isEmployeeManagerOfEquip(equipId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are not supervising any rental for this equipment.",
                    "Not allowed",
                    JOptionPane.WARNING_MESSAGE
            );
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
 // يتحقق أن الموظف الحالي يشرف على رنتال فيها هذا الـ Equip_id
    private boolean isEmployeeManagerOfEquip1(int equipId) {
        String sql =
                "SELECT COUNT(*) AS cnt " +
                "FROM RENTAL R " +
                "JOIN MAKES M ON R.Rental_id = M.Rent_id " +
                "WHERE R.Employee_id = ? AND M.Equip_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, equipId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking equipment manager.");
        }
        return false;
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