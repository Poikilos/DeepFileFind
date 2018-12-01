/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deepfilefind;

import java.util.Map;
import javax.swing.JTable;

/**
 *
 * @author owner
 */
public interface OnMatchEventListener {
    // this can be any type of method
    JTable target = null;
    void onMatchEvent(Map<String, String> result);

    public void setStatus(String nothing_to_do);
}
