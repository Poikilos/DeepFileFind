//
// Translated by CS2J (http://www.cs2j.com): 11/28/2018 3:01:24 PM
//

package deepfilefind;

//import CS2JNet.System.DateTimeSupport;
//import CS2JNet.System.DateTZ;
//import DeepFileFind.DFF;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/*
 * Created by SharpDevelop.
 * User: jgustafson
 * Date: 3/23/2017
 * Time: 5:48 PM
 *
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
//using System.Diagnostics; //System.Diagnostics.Debug.WriteLine etc
/**
* Description of DFFSearchOptions.
*/
public class DFFSearchOptions
{
    /**
    * MUST be UTC
    */
    public Date modified_start_datetime_utc = null;
    /**
    * MUST be UTC
    */
    public Date modified_endbefore_datetime_utc = null;
    public boolean modified_start_date_enable = false;
    public boolean modified_endbefore_date_enable = false;
    public boolean modified_start_time_enable = false;
    public boolean modified_endbefore_time_enable = false;
    public ArrayList start_directoryinfos = null;
    public ArrayList never_use_names = null;
    public String name_string = null;
    public String content_string = null;
    public boolean recursive_enable = true;
    public boolean include_folders_as_results_enable = true;
    public boolean case_sensitive_enable = false;
    public boolean threading_enable = false;
    public boolean min_size_enable = false;
    public boolean max_size_enable = false;
    public long min_size = 0;
    public long max_size = Long.MAX_VALUE;
    //public System.Windows.Forms.TextBox statusTextField = null;
    public boolean follow_folder_symlinks_enable = false;
    public boolean search_inside_hidden_files_enable = false;
    public boolean follow_dot_folders_enable = true;
    public boolean follow_hidden_folders_enable = true;
    public boolean follow_system_folders_enable = false;
    public boolean follow_temporary_folders_enable = false;
    public DFFSearchOptions() throws Exception {
        start_directoryinfos = new ArrayList();
        never_use_names = new ArrayList();
    }

    // ignore list (see also exclude_names; set by application while initializing each search)
    //never_use_names.Add(".cache");
    //never_use_names.Add("Trash");
    public void dumpToDebug() throws Exception {
        System.err.println("Dumping options:");
        for (Object __dummyForeachVar0 : dump())
        {
            String s = (String)__dummyForeachVar0;
            System.err.println(s);
        }
        System.err.println("");
    }

    public ArrayList dump() throws Exception {
        ArrayList results = new ArrayList();
        //NOTE: these MUST match the values from MainFormLoad in order for all settings to save and load
        results.add("start_date_enable = " + (modified_start_date_enable ? "true" : "false"));
        results.add("start_time_enable = " + (modified_start_time_enable ? "true" : "false"));
        //results.add("start_datetime_utc = " + DateTimeSupport.ToString((new DateTZ(modified_start_datetime_utc.getTime(), TimeZone.getTimeZone("UTC"))), DFF.datetime_sortable_format_string));
        results.add("endbefore_date_enable = " + (modified_endbefore_date_enable ? "true" : "false"));
        results.add("endbefore_time_enable = " + (modified_endbefore_time_enable ? "true" : "false"));
        //results.add("endbefore_datetime_utc = " + DateTimeSupport.ToString((new DateTZ(modified_endbefore_datetime_utc.getTime(), TimeZone.getTimeZone("UTC"))), DFF.datetime_sortable_format_string));
        results.add("recursive_enable = " + (recursive_enable ? "true" : "false"));
        results.add("include_folders_as_results_enable = " + (include_folders_as_results_enable ? "true" : "false"));
        results.add("case_sensitive_enable = " + (case_sensitive_enable ? "true" : "false"));
        results.add("min_size_enable = " + (min_size_enable ? "true" : "false"));
        results.add("max_size_enable = " + (max_size_enable ? "true" : "false"));
        results.add("min_size = " + String.valueOf(min_size));
        results.add("max_size = " + String.valueOf(max_size));
        String line = "start_directoryinfos = ";
        //for (Object __dummyForeachVar1 : start_directoryinfos)
        //{
        //    File di = (File)__dummyForeachVar1;
        //    line += di.FullName + ";";
        //}
        results.add(line);
        results.add("name_string = " + (name_string != null ? "\"" + name_string + "\"" : "null"));
        results.add("content_string = " + (content_string != null ? "\"" + content_string + "\"" : "null"));
        return results;
    }

}


