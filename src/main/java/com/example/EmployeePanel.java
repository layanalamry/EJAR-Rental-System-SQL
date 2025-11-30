
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class EmployeePanel extends JPanel {

    private JTextField txtId, txtName, txtEqId;
    private JTable table;
    private DefaultTableModel model;

    public EmployeePanel() {

        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtId   = new JTextField(10);
        txtName = new JTextField(20);
        txtEqId = new JTextField(10);   

        int y = 0;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Emp_id (PK):"), g);
        g.gridx=1; form.add(txtId, g);

        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Emp_name:"), g);
        g.gridx=1; form.add(txtName, g);

        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Eq_id (equipment managed, optional):"), g);
        g.gridx=1; form.add(txtEqId, g);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn     = new JButton("Add");
        JButton updateBtn  = new JButton("Update");
        JButton deleteBtn  = new JButton("Delete");
        JButton clearBtn   = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");

        btns.add(addBtn);
        btns.add(updateBtn);
        btns.add(deleteBtn);
        btns.add(clearBtn);
        btns.add(refreshBtn);

        y++;
        g.gridx=0; g.gridy=y; g.gridwidth=2;
        form.add(btns, g);

        add(form, BorderLayout.NORTH);

     
        model = new DefaultTableModel(new Object[]{"Emp_id","Emp_name","Eq_id"},0){
            @Override
            public boolean isCellEditable(int r,int c){ return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

    
        addBtn.addActionListener(e -> addEmployee());
        updateBtn.addActionListener(e -> updateEmployee());
        deleteBtn.addActionListener(e -> deleteEmployee());
        clearBtn.addActionListener(e -> clearFields());
        refreshBtn.addActionListener(e -> loadData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && table.getSelectedRow() != -1){
                int row = table.getSelectedRow();
                txtId.setText(model.getValueAt(row,0).toString());
                txtName.setText(model.getValueAt(row,1).toString());
                Object eq = model.getValueAt(row,2);
                txtEqId.setText(eq == null ? "" : eq.toString());
            }
        });

        loadData();
    }

  

    private void addEmployee() {
        if(txtId.getText().trim().isEmpty()){
            msg("Emp_id is required.");
            return;
        }

        String name = txtName.getText().trim();
        String eqText = txtEqId.getText().trim();

        try {
            int id = Integer.parseInt(txtId.getText().trim());

          
            Integer eqId = null;
            if (!eqText.isEmpty()) {
                eqId = Integer.parseInt(eqText);

                if (!equipmentExists(eqId)) {
                    msg("Equip_id " + eqId + " does not exist in EQUIPMENT.");
                    return;
                }
            }

            String sql = "INSERT INTO EMPLOYEE (Emp_id, Emp_name, Eq_id) VALUES (?,?,?)";

            try(Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)){

                ps.setInt(1, id);
                ps.setString(2, name);
                if (eqId == null)
                    ps.setNull(3, Types.INTEGER);
                else
                    ps.setInt(3, eqId);

                ps.executeUpdate();
                msg("Employee added.");
                loadData();
                clearFields();
            }

        } catch(NumberFormatException ex){
            msg("Emp_id and Eq_id (if filled) must be numeric.");
        } catch(SQLException ex){
            error(ex);
        }
    }

    private void updateEmployee() {

        int row = table.getSelectedRow();
        if(row == -1){
            msg("Select a row to update.");
            return;
        }

        String name   = txtName.getText().trim();
        String eqText = txtEqId.getText().trim();

        try {
            int id = Integer.parseInt(txtId.getText().trim());

            Integer eqId = null;
            if (!eqText.isEmpty()) {
                eqId = Integer.parseInt(eqText);
                if (!equipmentExists(eqId)) {
                    msg("Equip_id " + eqId + " does not exist in EQUIPMENT.");
                    return;
                }
            }

            String sql = "UPDATE EMPLOYEE SET Emp_name = ?, Eq_id = ? WHERE Emp_id = ?";

            try(Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)){

                ps.setString(1, name);
                if (eqId == null)
                    ps.setNull(2, Types.INTEGER);
                else
                    ps.setInt(2, eqId);
                ps.setInt(3, id);

                ps.executeUpdate();
                msg("Employee updated.");
                loadData();
                clearFields();
            }

        } catch(NumberFormatException ex){
            msg("Emp_id and Eq_id (if filled) must be numeric.");
        } catch(SQLException ex){
            error(ex);
        }
    }

    private void deleteEmployee() {

        int row = table.getSelectedRow();
        if(row == -1){
            msg("Select a row to delete.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row,0).toString());

        // Optional safety: prevent delete if supervising rentals
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) AS cnt FROM RENTAL WHERE Employee_id = ?")) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    msg("This employee supervises rentals and cannot be deleted.");
                    return;
                }
            }
        } catch (SQLException ex) {
            error(ex);
            return;
        }

        if(JOptionPane.showConfirmDialog(this,
                "Delete Employee " + id + " ?",
                "Confirm delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        try(Connection c = DBConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("DELETE FROM EMPLOYEE WHERE Emp_id=?")){

            ps.setInt(1,id);
            ps.executeUpdate();
            msg("Employee deleted.");
            loadData();
            clearFields();

        } catch(SQLException ex){
            error(ex);
        }
    }

    private void loadData() {

        model.setRowCount(0);

        try(Connection c = DBConnection.getConnection();
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT Emp_id, Emp_name, Eq_id FROM EMPLOYEE ORDER BY Emp_id")){

            while(rs.next()){
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("Emp_id"));
                row.add(rs.getString("Emp_name"));
                Object eqId = rs.getObject("Eq_id");
                row.add(eqId);
                model.addRow(row);
            }

        } catch(SQLException ex){
            error(ex);
        }
    }


    private boolean equipmentExists(int eqId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM EQUIPMENT WHERE Equip_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eqId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        }
    }

    private void clearFields(){
        txtId.setText("");
        txtName.setText("");
        txtEqId.setText("");
        table.clearSelection();
    }

    private void msg(String m){
        JOptionPane.showMessageDialog(this,m);
    }

    private void error(Exception ex){
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());
    }
}



