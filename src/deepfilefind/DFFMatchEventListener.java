/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deepfilefind;

import java.util.Map;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jake Gustafson
 */
public class DFFMatchEventListener implements OnMatchEventListener {
    JTable table = null;
    JTextField statusTextField = null;
    DeepFileFind app = null;
    @Override
    public void onMatchEvent(Map<String, String> result) {
        if (table == null)
            throw new NullPointerException("Missing target JTable.");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{result.get("Path"), result.get("Ext"), result.get("Modified"), result.get("Created"), result.get("Name")});
    }

    @Override
    public void setStatus(String s) {
        if (statusTextField != null) statusTextField.setText(s);
        else System.err.println(s);
    }

    @Override
    public void markBadEntry(String fieldname) {
        if (app != null) {
            app.markEntry(fieldname, false);
        }
    }
    
}
