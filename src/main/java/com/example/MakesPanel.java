
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class MakesPanel extends JPanel {

    private JTextField txtName, txtEquip, txtRent;
    private JTable table;
    private DefaultTableModel model;

    public MakesPanel(){

        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtName  = new JTextField(20);
        txtEquip = new JTextField(10);
        txtRent  = new JTextField(10);

        int y = 0;

        addField(form,g,"C_name (must exist in CUSTOMER):", txtName, y++);
        addField(form,g,"Equ_id (must exist in EQUIPMENT):", txtEquip, y++);
        addField(form,g,"Rent_id (must exist in RENTAL):", txtRent, y++);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn     = new JButton("Add");
        JButton deleteBtn  = new JButton("Delete");
        JButton clearBtn   = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");

        btns.add(addBtn);
        btns.add(deleteBtn);
        btns.add(clearBtn);
        btns.add(refreshBtn);

        g.gridx=0; g.gridy=y; g.gridwidth=2;
        form.add(btns,g);

        add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"C_name","Equ_id","Rent_id"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> addMakes());
        deleteBtn.addActionListener(e -> deleteMakes());
        clearBtn.addActionListener(e -> clearFields());
        refreshBtn.addActionListener(e -> loadData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && table.getSelectedRow()!=-1){
                int row = table.getSelectedRow();

                txtName.setText(model.getValueAt(row,0).toString());
                txtEquip.setText(model.getValueAt(row,1).toString());
                txtRent.setText(model.getValueAt(row,2).toString());
            }
        });

        loadData();
    }

    private void addField(JPanel p, GridBagConstraints g, String label, JTextField f, int y){
        g.gridx=0; g.gridy=y; p.add(new JLabel(label),g);
        g.gridx=1; p.add(f,g);
    }

    
    private void addMakes(){
        String nameText  = txtName.getText().trim();
        String equipText = txtEquip.getText().trim();
        String rentText  = txtRent.getText().trim();

        if (nameText.isEmpty() || equipText.isEmpty() || rentText.isEmpty()) {
            msg("كل الحقول مطلوبة (C_name, Equ_id, Rent_id).");
            return;
        }

        int equipId, rentId;
        try {
            equipId = Integer.parseInt(equipText);
            rentId  = Integer.parseInt(rentText);
        } catch (NumberFormatException ex) {
            msg("Equ_id و Rent_id لازم أرقام.");
            return;
        }

        try (Connection c = DBConnection.getConnection()) {

        
            if (!exists(c, "SELECT 1 FROM CUSTOMER WHERE Cus_name = ?", nameText)) {
                msg("C_name غير موجود في جدول CUSTOMER.");
                return;
            }

        
            if (!exists(c, "SELECT 1 FROM EQUIPMENT WHERE Equ_id = ?", equipId)) {
                msg("Equ_id غير موجود في جدول EQUIPMENT.");
                return;
            }

       
            if (!exists(c, "SELECT 1 FROM RENTAL WHERE Rental_id = ?", rentId)) {
                msg("Rent_id غير موجود في جدول RENTAL.");
                return;
            }

         
            final String sql = "INSERT INTO MAKES (C_name, Equ_id, Rent_id) VALUES (?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, nameText);
                ps.setInt(2, equipId);
                ps.setInt(3, rentId);
                ps.executeUpdate();
            }

            msg("Record added to MAKES.");
            loadData();
            clearFields();

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private boolean exists(Connection c, String sql, Object param) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (param instanceof String)
                ps.setString(1, (String)param);
            else if (param instanceof Integer)
                ps.setInt(1, (Integer)param);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void deleteMakes(){
        int row = table.getSelectedRow();
        if(row == -1){
            msg("Select row to delete.");
            return;
        }

        String name = model.getValueAt(row,0).toString();
        int equip = Integer.parseInt(model.getValueAt(row,1).toString());
        int rent  = Integer.parseInt(model.getValueAt(row,2).toString());

        if(JOptionPane.showConfirmDialog(this,
                "Delete relation ?", "Confirm",
                JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
            return;

        final String sql = "DELETE FROM MAKES WHERE C_name=? AND Equ_id=? AND Rent_id=?";

        try(Connection c = DBConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)){

            ps.setString(1,name);
            ps.setInt(2,equip);
            ps.setInt(3,rent);

            ps.executeUpdate();
            msg("Deleted.");
            loadData();
            clearFields();

        } catch(Exception ex){ error(ex); }
    }

    private void loadData(){

        model.setRowCount(0);

        final String sql = "SELECT C_name, Equ_id, Rent_id FROM MAKES ORDER BY C_name, Equ_id, Rent_id";

        try(Connection c = DBConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("C_name"));
                row.add(rs.getInt("Equ_id"));
                row.add(rs.getInt("Rent_id"));
                model.addRow(row);
            }

        } catch(Exception ex){ error(ex); }
    }

    private void clearFields(){
        txtName.setText("");
        txtEquip.setText("");
        txtRent.setText("");
        table.clearSelection();
    }

    private void msg(String m){ JOptionPane.showMessageDialog(this,m); }
    private void error(Exception ex){
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());
    }
}
