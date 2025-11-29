

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PaymentPanel extends JPanel {

private JTextField txtInv, txtDate, txtTotal, txtRental;
private JTable table;
private DefaultTableModel model;

public PaymentPanel(){

setLayout(new BorderLayout(8,8));

JPanel form = new JPanel(new GridBagLayout());
GridBagConstraints g = new GridBagConstraints();
g.insets = new Insets(5,5,5,5);
g.fill = GridBagConstraints.HORIZONTAL;

txtInv = new JTextField(10);
txtDate = new JTextField(10);
txtTotal = new JTextField(10);
txtRental = new JTextField(10);

int y = 0;

addField(form,g,"Invoice (PK):", txtInv, y++);
addField(form,g,"Pay_date:", txtDate, y++);
addField(form,g,"Total_price:", txtTotal, y++);
addField(form,g,"P_rental:", txtRental, y++);

JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
JButton addBtn = new JButton("Add");
JButton updateBtn = new JButton("Update");
JButton deleteBtn = new JButton("Delete");
JButton clearBtn = new JButton("Clear");
JButton refreshBtn = new JButton("Refresh");

btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn);
btns.add(clearBtn); btns.add(refreshBtn);

g.gridx=0; g.gridy=y; g.gridwidth=2;
form.add(btns,g);

add(form, BorderLayout.NORTH);

model = new DefaultTableModel(
new Object[]{"Invoice","Pay_date","Total_price","P_rental"},0){
@Override public boolean isCellEditable(int r, int c){return false;}
};

table = new JTable(model);
add(new JScrollPane(table), BorderLayout.CENTER);

addBtn.addActionListener(e -> addPayment());
updateBtn.addActionListener(e -> updatePayment());
deleteBtn.addActionListener(e -> deletePayment());
clearBtn.addActionListener(e -> clearFields());
refreshBtn.addActionListener(e -> loadData());

table.getSelectionModel().addListSelectionListener(e -> {
if(!e.getValueIsAdjusting() && table.getSelectedRow()!=-1){
int row = table.getSelectedRow();

txtInv.setText(model.getValueAt(row,0).toString());
txtDate.setText(model.getValueAt(row,1).toString());
txtTotal.setText(model.getValueAt(row,2).toString());
txtRental.setText(model.getValueAt(row,3).toString());
}
});

loadData();
}

private void addField(JPanel p, GridBagConstraints g, String label, JTextField f, int y){
g.gridx=0; g.gridy=y; p.add(new JLabel(label),g);
g.gridx=1; p.add(f,g);
}

private void addPayment(){
try {
int inv = Integer.parseInt(txtInv.getText().trim());
double total = Double.parseDouble(txtTotal.getText().trim());
int rental = Integer.parseInt(txtRental.getText().trim());

String sql = "INSERT INTO PAYMENT VALUES (?,?,?,?)";

try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement(sql)){

ps.setInt(1,inv);
ps.setString(2,txtDate.getText().trim());
ps.setDouble(3,total);
ps.setInt(4,rental);

ps.executeUpdate();
msg("Payment added.");
loadData();
clearFields();
}

} catch(Exception ex){
error(ex);
}
}

private void updatePayment(){
try {
String sql =
"UPDATE PAYMENT SET Pay_date=?, Total_price=?, P_rental=? WHERE Invoice=?";

try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement(sql)){

ps.setString(1,txtDate.getText().trim());
ps.setDouble(2,Double.parseDouble(txtTotal.getText().trim()));
ps.setInt(3,Integer.parseInt(txtRental.getText().trim()));
ps.setInt(4,Integer.parseInt(txtInv.getText().trim()));

ps.executeUpdate();
msg("Payment updated.");
loadData();
clearFields();
}

} catch(Exception ex){
error(ex);
}
}

private void deletePayment(){
int row = table.getSelectedRow();
if(row == -1){ msg("Select row to delete."); return; }

int inv = Integer.parseInt(model.getValueAt(row,0).toString());

if(JOptionPane.showConfirmDialog(this,
"Delete invoice " + inv + " ?",
"Confirm", JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
return;

try(Connection c = DBConnection.getConnection();
PreparedStatement ps = c.prepareStatement("DELETE FROM PAYMENT WHERE Invoice=?")){

ps.setInt(1,inv);
ps.executeUpdate();
msg("Payment deleted.");
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
ResultSet rs = st.executeQuery("SELECT * FROM PAYMENT ORDER BY Invoice")){

while(rs.next()){
Vector<Object> row = new Vector<>();

row.add(rs.getInt("Invoice"));
row.add(rs.getString("Pay_date"));
row.add(rs.getDouble("Total_price"));
row.add(rs.getInt("P_rental"));

model.addRow(row);
}

} catch(Exception ex){
error(ex);
}
}

private void clearFields(){
txtInv.setText("");
txtDate.setText("");
txtTotal.setText("");
txtRental.setText("");
table.clearSelection();
}

private void msg(String m){ JOptionPane.showMessageDialog(this,m); }
private void error(Exception ex){
ex.printStackTrace();
JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());
}
}










