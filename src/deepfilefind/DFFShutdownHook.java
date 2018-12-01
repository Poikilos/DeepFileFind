/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deepfilefind;

/**
 *
 * @author owner
 */
public class DFFShutdownHook extends Thread {
        DeepFileFind app = null;
	@Override
	public void run() {
            if (app == null) return;
            if (app.dff!=null) app.dff.enable = false;
            app.saveState();
            //System.out.println("Status="+FilesProcessor.status);
            //System.out.println("FileName="+FilesProcessor.fileName);
            //if(!FilesProcessor.status.equals("FINISHED")){
            //	System.out.println("Seems some error, sending alert");
            //}
	}
}