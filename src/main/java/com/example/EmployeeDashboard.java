
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmployeeDashboard extends JFrame {

    // Theme colors (MATCH CUSTOMER STYLE)
    private static final Color CARD_GREY     = new Color(230, 233, 238, 230);
    // butter yellow same as customer
    private static final Color BUTTER_YELLOW = new Color(245, 210, 90);
    // dark black/grey for buttons
    private static final Color BUTTON_DARK   = new Color(40, 40, 40);

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

        //String bgPath = "C:\\Users\\user\\OneDrive\\Documents\\380CSC Database\\CSC380_Phase-3-_Group-5-_EJAR-Rental-System\\photo_2025-11-30_18-29-55.jpg";
        BackgroundPanel bg = new BackgroundPanel("/images/dashboard.jpg");
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
        header.setBackground(BUTTER_YELLOW); // yellow header
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("EJAR – Employee Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(Color.BLACK);   // black text

        JLabel info = new JLabel("ID: " + employeeId + "   |   " + employeeName,
                SwingConstants.RIGHT);
        info.setFont(new Font("Serif", Font.BOLD, 16));
        info.setForeground(Color.BLACK);    // black text

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

        // ===== LEFT: Equipment I Currently Manage =====
        JPanel leftColumn = new JPanel(new BorderLayout());
        leftColumn.setOpaque(false);
        leftColumn.add(buildMyEquipCard(), BorderLayout.NORTH);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 0.6;
        center.add(leftColumn, gbc);

        // ===== MIDDLE: Rentals card =====
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 0.6;
        center.add(buildRentalsCard(), gbc);

        // ===== RIGHT: vertical menu of reports/buttons =====
        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton addEquipBtn   = bigNavButton("Add New Equipment");
        JButton allEquipBtn   = bigNavButton("All Equipments");
       JButton allPaymentsBtn = bigNavButton("All Payments");

        JButton abovePriceBtn = bigNavButton("Equipments above price");
        JButton salesBtn      = bigNavButton("Sales by type");

        // make them not stretch too wide
        Dimension sideSize = new Dimension(230, 45);
        addEquipBtn.setMaximumSize(sideSize);
        allEquipBtn.setMaximumSize(sideSize);
        abovePriceBtn.setMaximumSize(sideSize);
        salesBtn.setMaximumSize(sideSize);

        addEquipBtn.addActionListener(e -> addNewEquipment());
        allEquipBtn.addActionListener(e -> showAllEquipments());
        allPaymentsBtn.addActionListener(e ->showAllPayments());
        abovePriceBtn.addActionListener(e -> viewEquipmentsAbovePrice());
        salesBtn.addActionListener(e -> viewTypeSales());

        rightColumn.add(addEquipBtn);
        rightColumn.add(Box.createVerticalStrut(10));
        rightColumn.add(allEquipBtn);
        rightColumn.add(Box.createVerticalStrut(10));
rightColumn.add(allPaymentsBtn);   
        rightColumn.add(Box.createVerticalStrut(10));
        rightColumn.add(abovePriceBtn);
        rightColumn.add(Box.createVerticalStrut(10));
        rightColumn.add(salesBtn);
        rightColumn.add(Box.createVerticalGlue());

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 0.6;
        center.add(rightColumn, gbc);

        return center;
    }

    // ============ EQUIPMENT CARD ============ 
    private JPanel buildMyEquipCard() {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(BUTTER_YELLOW, 2));
        card.setPreferredSize(new Dimension(360, 220));   // taller so 3 buttons fit

        JLabel header = new JLabel("Equipment I Currently Manage", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 18));
        header.setOpaque(true);
        header.setBackground(BUTTER_YELLOW);
        header.setForeground(Color.BLACK);  // black text
        card.add(header, BorderLayout.NORTH);

        String[] cols = {"Equip ID", "Type", "Model", "Price", "Status"};
        myEquipTable = new JTable(new DefaultTableModel(cols, 0));
        myEquipTable.setRowHeight(22);
        card.add(new JScrollPane(myEquipTable), BorderLayout.CENTER);

        // ===== BUTTONS (VERTICAL) =====
        JButton chooseBtn = new JButton("Choose equipment to manage");
        JButton stopBtn   = new JButton("Stop managing");
        JButton editBtn   = new JButton("Update / Delete");

        styleMainButton(chooseBtn);
        styleMainButton(stopBtn);
        styleMainButton(editBtn);

        chooseBtn.addActionListener(e -> chooseEquipmentToManage());
        stopBtn.addActionListener(e -> stopManagingEquipment());
        editBtn.addActionListener(e -> handleManageEquipment());

        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // << important
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(chooseBtn);
        buttonsPanel.add(stopBtn);
        buttonsPanel.add(editBtn);

        card.add(buttonsPanel, BorderLayout.SOUTH);

        return card;
    }

    // ============ RENTALS CARD ============ 
    private JPanel buildRentalsCard() {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(CARD_GREY);
        card.setBorder(BorderFactory.createLineBorder(BUTTER_YELLOW, 2));
        card.setPreferredSize(new Dimension(420, 260));

        JLabel header = new JLabel("Rentals", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(BUTTER_YELLOW);
        header.setForeground(Color.BLACK); // black text
        card.add(header, BorderLayout.NORTH);

        String[] cols = {
                "Rental ID", "Customer", "Email",
                "Start", "End", "Equip ID",
                "Total Price", "Emp ID", "Emp Name"
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
        card.setBorder(BorderFactory.createLineBorder(BUTTER_YELLOW, 2));
        card.setPreferredSize(new Dimension(220, 180));

        JLabel header = new JLabel("My Info", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 18));
        header.setOpaque(true);
        header.setBackground(BUTTER_YELLOW);
        header.setForeground(Color.BLACK); // black text
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

    private JPanel buildBottomBar() {
        // Align buttons to the RIGHT
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 15));
        bar.setOpaque(false);

        JButton deleteMeBtn = bigNavButton("Delete My Record");
        deleteMeBtn.addActionListener(e -> deleteMyEmployeeRecord());

        JButton logoutBtn = bigNavButton("Logout");
        // allow logout anytime → back to login screen
        logoutBtn.addActionListener(e -> logoutAndReturnToFirstPage());

        bar.add(deleteMeBtn);
        bar.add(logoutBtn);

        return bar;
    }

    // ============ REPORT: Total sales per machinery type ============
    private void viewTypeSales() {
        String sql =
                "SELECT E.Type, SUM(P.Total_price) AS Total_sales " +
                "FROM EQUIPMENT E " +
                "JOIN MAKES M   ON E.Equip_id = M.Equ_id " +
                "JOIN RENTAL R  ON M.Rent_id  = R.Rental_id " +
                "JOIN PAYMENT P ON R.Rental_id = P.P_rental " +
                "GROUP BY E.Type " +
                "ORDER BY Total_sales DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String[] cols = {"Type", "Total sales"};
            DefaultTableModel tm = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                tm.addRow(new Object[]{
                        rs.getString("Type"),
                        rs.getDouble("Total_sales")
                });
            }

            JTable table = new JTable(tm);
            table.setRowHeight(22);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(500, 240));

            JOptionPane.showMessageDialog(
                    this,
                    scroll,
                    "Total sales per machinery type",
                    JOptionPane.PLAIN_MESSAGE
            );

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading sales by type.");
        }
    }

    // ============ REPORT: Equipments above chosen price ============
    private void viewEquipmentsAbovePrice() {
        String input = JOptionPane.showInputDialog(
                this,
                "Show equipments with price greater than:",
                "Filter equipments by price",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) {
            return; // user canceled
        }

        double minPrice;
        try {
            minPrice = Double.parseDouble(input.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price value.");
            return;
        }

        String sql =
                "SELECT Equip_id, Type, Model, Price " +
                "FROM EQUIPMENT " +
                "WHERE Price > ? " +
                "ORDER BY Price DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, minPrice);

            try (ResultSet rs = ps.executeQuery()) {

                String[] cols = {"Equip ID", "Type", "Model", "Price"};
                DefaultTableModel tm = new DefaultTableModel(cols, 0);

                while (rs.next()) {
                    tm.addRow(new Object[]{
                            rs.getInt("Equip_id"),
                            rs.getString("Type"),
                            rs.getString("Model"),
                            rs.getDouble("Price")
                    });
                }

                JTable table = new JTable(tm);
                table.setRowHeight(22);
                JScrollPane scroll = new JScrollPane(table);
                scroll.setPreferredSize(new Dimension(600, 260));

                JOptionPane.showMessageDialog(
                        this,
                        scroll,
                        "Equipments with price > " + minPrice,
                        JOptionPane.PLAIN_MESSAGE
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading equipments above price.");
        }
    }

    // ============ STYLING HELPERS ============ 
    private void styleMainButton(JButton btn) {
        btn.setBackground(BUTTON_DARK);        // black-ish
        btn.setForeground(Color.WHITE);        // white text
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private JButton bigNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BUTTON_DARK);        // black-ish
        btn.setForeground(Color.WHITE);        // white text
        btn.setFont(new Font("Serif", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    // ============ DB LOADERS ============ 

    // المعدات التي يديرها هذا الموظف فقط (0 أو 1) من الـ VIEW equipment1
    private void loadManagedEquipment() {
        String sql =
                "SELECT Equip_id, Type, Model, Price, Status " +
                "FROM equipment1 " +
                "WHERE Equip_id = (SELECT Eq_id FROM EMPLOYEE WHERE Emp_id = ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {

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
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading your equipment list");
        }
    }

    // كل الإيجارات مع اسم العميل والإيميل والتوتال برايس من PAYMENT
   private void loadAllRentals() {
    String sql =
    "SELECT R.Rental_id, " +
    "       C.Cus_name AS Customer, " +
    "       C.Email AS Email, " +
    "       R.Start_date, " +
    "       R.End_date, " +
    "       M.Equ_id AS Equip_id, " +
    "       P.Total_price, " +
    "       R.Employee_id, " +
    "       E.Emp_name " +
    "FROM RENTAL R " +
    "JOIN MAKES M ON R.Rental_id = M.Rent_id " +
    "JOIN CUSTOMER C ON M.C_name = C.Cus_name " +
    "LEFT JOIN EMPLOYEE E ON E.Emp_id = R.Employee_id " +
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
                rs.getObject("Employee_id"),
                rs.getString("Emp_name")
            });
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading rentals");
    }
}

    // ============ EQUIPMENT OPERATIONS ============ 

    // إضافة معدّة جديدة
    // إضافة معدّة جديدة واعتبار هذا الموظف هو المانِجر لها
private void addNewEquipment() {

    // 0) أولاً: تأكد أن هذا الموظف لا يدير معدّة أخرى الآن
    String checkMine = "SELECT Eq_id FROM EMPLOYEE WHERE Emp_id = ?";
    try (PreparedStatement checkPs = conn.prepareStatement(checkMine)) {
        checkPs.setInt(1, employeeId);
        try (ResultSet rs = checkPs.executeQuery()) {
            if (rs.next()) {
                Integer myEq = (Integer) rs.getObject("Eq_id");
                if (myEq != null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "You already manage equipment #" + myEq +
                            ".\nStop managing it first before adding a new one you manage.",
                            "Not allowed",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error checking your managed equipment");
        return;
    }

    // 1) جمع البيانات من المستخدم
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
        // 2) نحسب الـ ID الجديد
        int newId = getNextId("EQUIPMENT", "Equip_id");

        // 3) نضيف المعدّة في جدول EQUIPMENT
        String insertSql = "INSERT INTO EQUIPMENT (Equip_id, Type, Model, Price) " +
                           "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, newId);
            ps.setString(2, type);
            ps.setString(3, model);
            ps.setDouble(4, price);
            ps.executeUpdate();
        }

        // 4) نربط هذه المعدّة بالموظف الحالي كـ Manager (EMPLOYEE.Eq_id)
        String updateEmpSql = "UPDATE EMPLOYEE SET Eq_id = ? WHERE Emp_id = ?";
        try (PreparedStatement ps2 = conn.prepareStatement(updateEmpSql)) {
            ps2.setInt(1, newId);
            ps2.setInt(2, employeeId);
            ps2.executeUpdate();
        }

        JOptionPane.showMessageDialog(this,
                "Equipment added with ID " + newId +
                " and assigned to you as manager.");

        // تحدّث جدول "Equipment I Currently Manage"
        loadManagedEquipment();

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error adding equipment");
    }
}
    // اختر معدّة لإدارتها – فقط إذا الموظف لا يدير أي معدّة الآن
    private void chooseEquipmentToManage() {

        // 1) This employee must not already manage something
        String checkMine = "SELECT Eq_id FROM EMPLOYEE WHERE Emp_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkMine)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer myEq = (Integer) rs.getObject("Eq_id");
                    if (myEq != null) {
                        JOptionPane.showMessageDialog(
                                this,
                                "You already manage equipment #" + myEq +
                                ".\nStop managing it first before choosing another.",
                                "Not allowed",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking your managed equipment");
            return;
        }

        // 2) Ask which Equip_id to manage
        String idStr = JOptionPane.showInputDialog(
                this,
                "Enter Equip ID you want to manage:"
        );
        if (idStr == null || idStr.trim().isEmpty()) return;

        int equipId;
        try {
            equipId = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Equip ID.");
            return;
        }

        try {
            // 3) Check that the equipment exists
            String existsSql = "SELECT Equip_id FROM EQUIPMENT WHERE Equip_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(existsSql)) {
                ps.setInt(1, equipId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this, "Equipment not found.");
                        return;
                    }
                }
            }

            // 4) Assign this equipment to current employee
            String updateSql = "UPDATE EMPLOYEE SET Eq_id = ? WHERE Emp_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, equipId);
                ps.setInt(2, employeeId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "You now manage equipment #" + equipId
                    );
                    loadManagedEquipment();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error assigning equipment manager");
        }
    }

    // التوقف عن إدارة المعدّة الحالية المختارة في الجدول
    private void stopManagingEquipment() {
        int row = myEquipTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an equipment first.");
            return;
        }

        int equipId = (int) myEquipTable.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Stop managing equipment #" + equipId + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE EMPLOYEE SET Eq_id = NULL WHERE Emp_id = ? AND Eq_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, equipId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "You no longer manage this equipment.");
                loadManagedEquipment();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating equipment manager");
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
                    "You do not manage this equipment.",
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

    // حذف المعدّة – مسموح فقط إذا الموظف Manager عليها والمعدّة غير مستخدمة في MAKES
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
                    "You do not manage this equipment.",
                    "Not allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // نتأكد أنها ليست مستخدمة في MAKES
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

    // يتحقق أن هذا الموظف هو Manager للمعدّة بحسب EMPLOYEE.Eq_id
    private boolean isEmployeeManagerOfEquip(int equipId) {
        String sql = "SELECT Eq_id FROM EMPLOYEE WHERE Emp_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int myEq = rs.getInt("Eq_id");
                    if (!rs.wasNull() && myEq == equipId) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
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
            handleUpdateEquipmentById();
        } else if (choice == 1) {
            handleDeleteEquipmentById();
        }
        // Cancel → nothing
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

    private void showAllPayments() {
    String sql =
            "SELECT Invoice, Pay_date, Total_price, P_rental " +
            "FROM PAYMENT " +
            "ORDER BY Pay_date DESC";

    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

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
        table.setRowHeight(22);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(220, 180));

        JOptionPane.showMessageDialog(this, scroll,
                "All Payments", JOptionPane.PLAIN_MESSAGE);

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading payments");
    }
}


    private void showAllEquipments() {
        String sql = "SELECT Equip_id, Type, Model, Price, Status FROM equipment1";

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
                    logoutAndReturnToFirstPage();
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

    // ============ LOGOUT HELPER ============ 
    private void logoutAndReturnToFirstPage() {
        // close this window and go back to login screen
        dispose();
        SwingUtilities.invokeLater(() -> new EjarLogin().setVisible(true));
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