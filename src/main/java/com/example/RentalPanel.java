
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class RentalPanel extends JPanel {

private JTextField txtRentalId, txtStart, txtEnd, txtCity, txtZip, txtTotal, txtEquipId, txtCustomer, txtEmpId;
private JTable table;
private DefaultTableModel model;

public RentalPanel() {

setLayout(new BorderLayout(8,8));

JPanel form = new JPanel(new GridBagLayout());
GridBagConstraints g = new GridBagConstraints();
g.insets = new Insets(5,5,5,5);
g.fill = GridBagConstraints.HORIZONTAL;

txtRentalId = new JTextField(10);
txtStart = new JTextField(10);
txtEnd = new JTextField(10);
txtCity = new JTextField(15);
txtZip = new JTextField(10);
txtTotal = new JTextField(10);
txtEquipId = new JTextField(10);
txtCustomer = new JTextField(20);
txtEmpId = new JTextField(10);

int y = 0;

addField(form, g, "Rental_id (PK):", txtRentalId, y++);
addField(form, g, "Start_date (YYYY-MM-DD):", txtStart, y++);
addField(form, g, "End_date (YYYY-MM-DD):", txtEnd, y++);
addField(form, g, "City:", txtCity, y++);
addField(form, g, "Zip_code:", txtZip, y++);
addField(form, g, "Tot_price:", txtTotal, y++);
addField(form, g, "Equip_id:", txtEquipId, y++);
addField(form, g, "C_name:", txtCustomer, y++);
addField(form, g, "Employee_id:", txtEmpId, y++);

JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
JButton addBtn = new JButton("Add");
JButton updateBtn = new JButton("Update");
JButton deleteBtn = new JButton("Delete");
JButton clearBtn = new JButton("Clear");
JButton refreshBtn = new JButton("Refresh");

btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn);
btns.add(clearBtn); btns.add(refreshBtn);

g.gridx=0; g.gridy=y; g.gridwidth=2;
form.add(btns, g);

add(form, BorderLayout.NORTH);

model = new DefaultTableModel(
new Object[]{"Rental_id","Start_date","End_date","City","Zip_code",
"Tot_price","Equip_id","C_name","Employee_id"}, 0
) {
@Override public boolean isCellEditable(int r,int c){ return false; }
};

table = new JTable(model);
add(new JScrollPane(table), BorderLayout.CENTER);

addBtn.addActionListener(e -> addRental());
updateBtn.addActionListener(e -> updateRental());
deleteBtn.addActionListener(e -> deleteRental());
clearBtn.addActionListener(e -> clearFields());
refreshBtn.addActionListener(e -> loadData());

table.getSelectionModel().addListSelectionListener(e -> {
if(!e.getValueIsAdjusting() && table.getSelectedRow() != -1){
int row = table.getSelectedRow();

txtRentalId.setText(model.getValueAt(row,0).toString());
txtStart.setText(model.getValueAt(row,1).toString());
txtEnd.setText(model.getValueAt(row,2).toString());
txtCity.setText(model.getValueAt(row,3).toString());
txtZip.setText(model.getValueAt(row,4).toString());
txtTotal.setText(model.getValueAt(row,5).toString());
txtEquipId.setText(model.getValueAt(row,6).toString());
txtCustomer.setText(model.getValueAt(row,7).toString());
txtEmpId.setText(model.getValueAt(row,8).toString());
}
});

loadData();
}

private void addField(JPanel form, GridBagConstraints g, String label, JTextField field, int y){
g.gridx=0; g.gridy=y; g.gridwidth=1;
form.add(new JLabel(label), g);
g.gridx=1; g.gridwidth=1;
form.add(field, g);
}

private void addRental(){
try {
int id = Integer.parseInt(txtRentalId.getText().trim());
double total = Double.parseDouble(txtTotal.getText().trim());
int equip = Integer.parseInt(txtEquipId.getText().trim());
int emp = Integer.parseInt(txtEmpId.getText().trim());

String sql =
"INSERT INTO RENTAL VALUES (?,?,?,?,?,?,?,?,?)";

try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement(sql)){

ps.setInt(1,id);
ps.setString(2, txtStart.getText().trim());
ps.setString(3, txtEnd.getText().trim());
ps.setString(4, txtCity.getText().trim());
ps.setString(5, txtZip.getText().trim());
ps.setDouble(6, total);
ps.setInt(7, equip);
ps.setString(8, txtCustomer.getText().trim());
ps.setInt(9, emp);

ps.executeUpdate();
msg("Rental added.");
loadData();
clearFields();

}

} catch(Exception ex){
error(ex);
}
}

private void updateRental(){
try {
String sql =
"UPDATE RENTAL SET Start_date=?,End_date=?,City=?,Zip_code=?,Tot_price=?,Equip_id=?,C_name=?,Employee_id=? WHERE Rental_id=?";

try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement(sql)){

ps.setString(1, txtStart.getText().trim());
ps.setString(2, txtEnd.getText().trim());
ps.setString(3, txtCity.getText().trim());
ps.setString(4, txtZip.getText().trim());
ps.setDouble(5, Double.parseDouble(txtTotal.getText().trim()));
ps.setInt(6, Integer.parseInt(txtEquipId.getText().trim()));
ps.setString(7, txtCustomer.getText().trim());
ps.setInt(8, Integer.parseInt(txtEmpId.getText().trim()));
ps.setInt(9, Integer.parseInt(txtRentalId.getText().trim()));

ps.executeUpdate();
msg("Rental updated.");
loadData();
clearFields();
}

} catch(Exception ex){
error(ex);
}
}

private void deleteRental(){
int row = table.getSelectedRow();
if(row == -1){ msg("Select row to delete."); return; }

int id = Integer.parseInt(model.getValueAt(row,0).toString());

if(JOptionPane.showConfirmDialog(this,
"Delete Rental " + id + " ?",
"Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
return;

try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement("DELETE FROM RENTAL WHERE Rental_id=?")){

ps.setInt(1,id);
ps.executeUpdate();
msg("Rental deleted.");
loadData();
clearFields();

} catch(Exception ex){
error(ex);
}
}

private void loadData(){
model.setRowCount(0);

try(Connection c = DBConnection.getConnection();
Statement st = c.createStatement();
ResultSet rs = st.executeQuery("SELECT * FROM RENTAL ORDER BY Rental_id")){

while(rs.next()){
Vector<Object> row = new Vector<>();

row.add(rs.getInt("Rental_id"));
row.add(rs.getString("Start_date"));
row.add(rs.getString("End_date"));
row.add(rs.getString("City"));
row.add(rs.getString("Zip_code"));
row.add(rs.getDouble("Tot_price"));
row.add(rs.getInt("Equip_id"));
row.add(rs.getString("C_name"));
row.add(rs.getInt("Employee_id"));

model.addRow(row);
}

} catch(Exception ex){
error(ex);
}
}

private void clearFields(){
txtRentalId.setText("");
txtStart.setText("");
txtEnd.setText("");
txtCity.setText("");
txtZip.setText("");
txtTotal.setText("");
txtEquipId.setText("");
txtCustomer.setText("");
txtEmpId.setText("");
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







