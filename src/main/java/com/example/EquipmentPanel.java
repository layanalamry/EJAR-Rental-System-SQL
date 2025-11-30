
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class EquipmentPanel extends JPanel {

    private JTextField txtId, txtType, txtModel, txtPrice, txtStatus;
    private JTable table;
    private DefaultTableModel model;

    public EquipmentPanel() {

        setLayout(new BorderLayout(8,8));

   
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtId     = new JTextField(15);
        txtType   = new JTextField(20);
        txtModel  = new JTextField(20);
        txtPrice  = new JTextField(15);
        txtStatus = new JTextField(15);

        int y = 0;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Equip_id (PK):"), g);
        g.gridx=1; form.add(txtId, g);

        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Type:"), g);
        g.gridx=1; form.add(txtType, g);

        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Model:"), g);
        g.gridx=1; form.add(txtModel, g);

        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Price:"), g);
        g.gridx=1; form.add(txtPrice, g);

        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Status:"), g);
        g.gridx=1; form.add(txtStatus, g);

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

      
        model = new DefaultTableModel(new Object[]{"Equip_id","Type","Model","Price","Status"},0) {
            @Override
            public boolean isCellEditable(int r,int c){ return false;}
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

      
        addBtn.addActionListener(e -> addEquipment());
        updateBtn.addActionListener(e -> updateEquipment());
        deleteBtn.addActionListener(e -> deleteEquipment());
        clearBtn.addActionListener(e -> clearFields());
        refreshBtn.addActionListener(e -> loadData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1){
                int row = table.getSelectedRow();
                txtId.setText(model.getValueAt(row,0).toString());
                txtType.setText(model.getValueAt(row,1).toString());
                txtModel.setText(model.getValueAt(row,2).toString());
                txtPrice.setText(model.getValueAt(row,3).toString());
                txtStatus.setText(model.getValueAt(row,4).toString());
            }
        });

        loadData();
    }

  
    private void addEquipment(){

        String idText   = txtId.getText().trim();
        String type     = txtType.getText().trim();
        String modelTxt = txtModel.getText().trim();
        String priceTxt = txtPrice.getText().trim();
        String status   = txtStatus.getText().trim();

        if (idText.isEmpty() || type.isEmpty() || modelTxt.isEmpty() || priceTxt.isEmpty()) {
            msg("Equip_id, Type, Model, Price are required.");
            return;
        }

        int id;
        double price;
        try {
            id = Integer.parseInt(idText);
            price = Double.parseDouble(priceTxt);
        } catch (NumberFormatException ex) {
            msg("Equip_id and Price must be numeric.");
            return;
        }

        if (status.isEmpty()) status = "Available";

        final String sql = "INSERT INTO EQUIPMENT (Equip_id, Type, Model, Price) VALUES (?,?,?,?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, type);
            ps.setString(3, modelTxt);
            ps.setDouble(4, price);
            //ps.setString(5, status);

            ps.executeUpdate();
            msg("Equipment added.");
            loadData();
            clearFields();

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void updateEquipment(){

        int row = table.getSelectedRow();
        if (row == -1){
            msg("Select a row to update.");
            return;
        }

        String idText   = txtId.getText().trim();
        String type     = txtType.getText().trim();
        String modelTxt = txtModel.getText().trim();
        String priceTxt = txtPrice.getText().trim();
        String status   = txtStatus.getText().trim();

        if (idText.isEmpty()) {
            msg("Equip_id is required.");
            return;
        }

        int id;
        double price;
        try {
            id = Integer.parseInt(idText);
            price = Double.parseDouble(priceTxt);
        } catch (NumberFormatException ex) {
            msg("Equip_id and Price must be numeric.");
            return;
        }

        final String sql = "UPDATE EQUIPMENT SET Type=?, Model=?, Price=? WHERE Equip_id=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setString(2, modelTxt);
            ps.setDouble(3, price);
            ps.setInt(4, id);

            ps.executeUpdate();
            msg("Equipment updated.");
            loadData();
            clearFields();

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void deleteEquipment(){

        int row = table.getSelectedRow();
        if (row == -1){
            msg("Select a row to delete.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row,0).toString());

        if (JOptionPane.showConfirmDialog(this,
                "Delete equipment " + id + " ?",
                "Confirm delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        final String sql = "DELETE FROM EQUIPMENT WHERE Equip_id=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            msg("Equipment deleted.");
            loadData();
            clearFields();

        } catch (SQLException ex) {
            error(ex);
        }
    }

   private void loadData() {

    model.setRowCount(0);

    final String sql = "SELECT Equip_id, Type, Model, Price, Status FROM EQUIPMENT1 ORDER BY Equip_id";

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            row.add(rs.getInt("Equip_id"));
            row.add(rs.getString("Type"));
            row.add(rs.getString("Model"));
            row.add(rs.getDouble("Price"));
            row.add(rs.getString("Status")); // <-- VIEW column
            model.addRow(row);
        }

    } catch (SQLException ex) {
        error(ex);
    }
}


    private void clearFields(){
        txtId.setText("");
        txtType.setText("");
        txtModel.setText("");
        txtPrice.setText("");
        txtStatus.setText("");
        table.clearSelection();
    }

    private void msg(String m){
        JOptionPane.showMessageDialog(this,m);
    }

    private void error(Exception ex){
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,"Error: " + ex.getMessage());
    }
}
