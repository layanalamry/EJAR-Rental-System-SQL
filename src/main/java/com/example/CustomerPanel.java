
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CustomerPanel extends JPanel {

    private JTextField txtName, txtMobile, txtEmail;
    private JTable table;
    private DefaultTableModel model;

    public CustomerPanel() {

        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(20);
        txtMobile = new JTextField(20);
        txtEmail = new JTextField(20);

        int y = 0;

        g.gridx = 0;
        g.gridy = y;
        form.add(new JLabel("Customer Name (PK):"), g);
        g.gridx = 1;
        form.add(txtName, g);

        y++;
        g.gridx = 0;
        g.gridy = y;
        form.add(new JLabel("Mobile:"), g);
        g.gridx = 1;
        form.add(txtMobile, g);

        y++;
        g.gridx = 0;
        g.gridy = y;
        form.add(new JLabel("Email:"), g);
        g.gridx = 1;
        form.add(txtEmail, g);

        // BUTTONS
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");

        btns.add(addBtn);
        btns.add(updateBtn);
        btns.add(deleteBtn);
        btns.add(clearBtn);
        btns.add(refreshBtn);

        y++;
        g.gridx = 0;
        g.gridy = y;
        g.gridwidth = 2;
        form.add(btns, g);

        add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Cus_name", "Cus_mobile", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> addCustomer());
        updateBtn.addActionListener(e -> updateCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        clearBtn.addActionListener(e -> clearFields());
        refreshBtn.addActionListener(e -> loadData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtName.setText(model.getValueAt(row, 0).toString());
                txtMobile.setText(model.getValueAt(row, 1).toString());
                txtEmail.setText(model.getValueAt(row, 2).toString());
            }
        });

        loadData();
    }

    private void addCustomer() {
        String name = txtName.getText().trim();
        String mobile = txtMobile.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty()) {
            msg("Customer Name is required (Primary Key).");
            return;
        }

        final String sql = "INSERT INTO CUSTOMER (Cus_name, Cus_mobile, Email) VALUES (?,?,?)";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, mobile);
            ps.setString(3, email);

            ps.executeUpdate();
            msg("Customer added successfully.");
            loadData();
            clearFields();

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void updateCustomer() {

        int selected = table.getSelectedRow();
        if (selected == -1) {
            msg("Please select a row from the table before updating.");
            return;
        }

        String originalName = model.getValueAt(selected, 0).toString();

        String name = txtName.getText().trim();
        String mobile = txtMobile.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty()) {
            msg("Customer name cannot be empty.");
            return;
        }

        final String sql = "UPDATE CUSTOMER SET Cus_name=?, Cus_mobile=?, Email=? WHERE Cus_name=?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, mobile);
            ps.setString(3, email);
            ps.setString(4, originalName);

            ps.executeUpdate();
            msg("Customer updated successfully.");
            loadData();
            clearFields();

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void deleteCustomer() {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            msg("Select a row to delete.");
            return;
        }

        String name = model.getValueAt(selected, 0).toString();

        if (JOptionPane.showConfirmDialog(this,
                "Delete customer: " + name + " ?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        final String sql = "DELETE FROM CUSTOMER WHERE Cus_name=?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.executeUpdate();
            msg("Customer deleted.");
            loadData();
            clearFields();

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void loadData() {
        model.setRowCount(0);

        final String sql
                = "SELECT Cus_name, Cus_mobile, Email FROM CUSTOMER ORDER BY Cus_name";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("Cus_name"));
                row.add(rs.getString("Cus_mobile"));
                row.add(rs.getString("Email"));
                //row.add(rs.getString("pass_word"));         
                model.addRow(row);
            }

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtMobile.setText("");
        txtEmail.setText("");
        table.clearSelection();
    }

    private void msg(String m) {
        JOptionPane.showMessageDialog(this, m);
    }

    private void error(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
    }
}
