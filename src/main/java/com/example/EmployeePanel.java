
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class EmployeePanel extends JPanel {

private JTextField txtId, txtName;
private JTable table;
private DefaultTableModel model;

public EmployeePanel() {

setLayout(new BorderLayout(8,8));

// =======================
// FORM SECTION
// =======================
JPanel form = new JPanel(new GridBagLayout());
GridBagConstraints g = new GridBagConstraints();
g.insets = new Insets(5,5,5,5);
g.fill = GridBagConstraints.HORIZONTAL;

txtId = new JTextField(10);
txtName = new JTextField(20);

int y = 0;

g.gridx=0; g.gridy=y; form.add(new JLabel("Emp_id (PK):"), g);
g.gridx=1; form.add(txtId, g);

y++;
g.gridx=0; g.gridy=y; form.add(new JLabel("Emp_name:"), g);
g.gridx=1; form.add(txtName, g);

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
g.gridx=0; g.gridy=y; g.gridwidth=2;
form.add(btns, g);

add(form, BorderLayout.NORTH);

// =======================
// TABLE SECTION
// =======================
model = new DefaultTableModel(new Object[]{"Emp_id","Emp_name"},0){
@Override
public boolean isCellEditable(int r,int c){ return false; }
};

table = new JTable(model);
add(new JScrollPane(table), BorderLayout.CENTER);

// =======================
// ACTIONS
// =======================
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
}
});

loadData();
}

// =======================
// CRUD METHODS
// =======================

private void addEmployee() {
if(txtId.getText().trim().isEmpty()){
msg("Emp_id is required.");
return;
}

try {
int id = Integer.parseInt(txtId.getText().trim());

String sql = "INSERT INTO EMPLOYEE (Emp_id, Emp_name) VALUES (?,?)";

try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement(sql)){

ps.setInt(1,id);
ps.setString(2, txtName.getText().trim());

ps.executeUpdate();
msg("Employee added.");
loadData();
clearFields();
}

} catch(NumberFormatException ex){
msg("Emp_id must be numeric.");
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

try {
int id = Integer.parseInt(txtId.getText().trim());

//String sql = "UPDATE EMPLOYEE SET Emp_name=? WHERE Emp_id=?"

String sql = null;
try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement(sql)){

ps.setString(1, txtName.getText().trim());
ps.setInt(2, id);

ps.executeUpdate();
msg("Employee updated.");
loadData();
clearFields();
}

} catch(NumberFormatException ex){
msg("Emp_id must be numeric.");
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
ResultSet rs = st.executeQuery("SELECT * FROM EMPLOYEE ORDER BY Emp_id")){

while(rs.next()){
Vector<Object> row = new Vector<>();
row.add(rs.getInt("Emp_id"));
row.add(rs.getString("Emp_name"));
model.addRow(row);
}

} catch(SQLException ex){
error(ex);
}
}

private void clearFields(){
txtId.setText("");
txtName.setText("");
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








