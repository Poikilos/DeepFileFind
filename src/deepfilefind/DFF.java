/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deepfilefind;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author owner
 */
public class DFF {
    String profilePath = null;
    String appdataPath = null;
    Map<String, String> options = new HashMap<>();  // older Java requires <String, String>
    private String opt_name_string = null;
    private String opt_name_lower = null;
    private String opt_content_string = null;
    private long opt_min_size;
    private long opt_max_size;
    List<String> locations = new ArrayList<>();
    private Thread searchThread = null;
    public boolean enable = false;  // running
    private boolean opt_ifar_enable = false;  // include folders as results
    
    private OnMatchEventListener mListener;  // listener field
    
    long match_count = 0;
    
    public void loadRecent() {
        if (appdataPath==null) return;
        File appdata = new File(appdataPath);
        if (!appdata.exists()) return;
        Path locationsPath = Paths.get(appdataPath, "recent_folders.txt");
        File locationsFile = locationsPath.toFile();
        if (locationsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(locationsFile.getAbsolutePath()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!locations.contains(line.trim()))
                        locations.add(line.trim());
                }
            } catch (IOException ex) {
                Logger.getLogger(DFF.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void saveRecent() {
        if (appdataPath==null) return;
        File appdata = new File(appdataPath);
        if (!appdata.exists()) return;
        Path locationsPath = Paths.get(appdataPath, "recent_folders.txt");
        File locationsFile = locationsPath.toFile();
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(locationsFile.getAbsolutePath()));
            for (String path_s : locations) {
                writer.write(path_s);
                writer.newLine();
            }
            writer.close();        
        } catch (IOException ex) {
            Logger.getLogger(DFF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void saveSettings() {
        
    }
    
    DFF() {
        profilePath = System.getProperty("user.home");
        if (profilePath==null || profilePath.trim().length()==0) {
            profilePath = System.getenv("HOME");
        }
        else System.out.println("user.home: " + profilePath);
        if (profilePath==null || profilePath.trim().length()==0) {
            profilePath = System.getenv("USERPROFILE");
            System.out.println("USERPROFILE: " + profilePath);
        }
        else System.out.println("HOME: " + profilePath);
        
        String appdatasPath = System.getenv("APPDATA");
        if (profilePath!=null) {
            if (appdatasPath==null || appdatasPath.trim().length()==0) {
                Path path = Paths.get(profilePath, ".config");
                appdatasPath = path.toAbsolutePath().toString();
                System.out.println("config: " + appdatasPath);
            }
            else System.out.println("APPDATA: " + appdatasPath);
        }
        
        if (appdatasPath!=null) {
            if (Files.isDirectory(Paths.get(appdatasPath))) { // LinkOption.NOFOLLOW_LINKS
                appdataPath = Paths.get(appdatasPath, "DeepFileFind").toString();
                System.out.println("appdataPath: " + appdataPath);
                File appdata = new File(appdataPath);
                boolean hasConfig = false;
                if (!appdata.exists()) {
                    hasConfig = appdata.mkdir();
                }
                else hasConfig = true;
                if (hasConfig) {
                    loadRecent();
                }
                else System.err.println("ERROR: Failed to create appdataPath '" + appdataPath + "'");
            }
            else {
                System.err.println("ERROR: Missing appdatasPath '" + appdatasPath + "'");
            }
        }
    } 
  
    // setting the listener 
    public void registerOnGeekEventListener(OnMatchEventListener mListener) {
        this.mListener = mListener;
    }
    
    public boolean is_truthy(String s) {
        if (s == null) return false;
        s = s.toLowerCase();
        boolean ret = true;
        switch (s) {
            case "false":
                ret = false;
                break;
            case "no":
                ret = false;
                break;
            case "0":
                ret = false;
                break;
            case "off":
                ret = false;
                break;
            case "null":
                ret = false;
                break;
            case "None":
                ret = false;
                break;
            default:
                break;
        }
        return ret;
    }
    
    ///case-insensitive; converts to lowercase;
    ///returns true if name is like filter (can have 1 star, or start and end with star); false if name is null; else true if filter is null
    public boolean is_like(String name, String filter) {
        if (name == null) return false;
        if (filter == null) return true;
        return is_like_cs(name.toLowerCase(), filter.toLowerCase());
    }
    ///case-sensitive faster version of is_like; assumes non-null params; if has star, has only 1 star, or something without star in between if starts and ends with star
    public boolean is_like_cs(String name, String filter) {
        if (filter.length() == 0) return true;
        if (filter.equals("*")) return true;
        if (filter.startsWith("*")
                && filter.endsWith("*")) {
            return name.contains(filter.substring(1, filter.length()-1));
        }
        if (filter.startsWith("*")) {
            return name.endsWith(filter.substring(1));
        }
        if (filter.endsWith("*")) {
            return name.startsWith(filter.substring(0,filter.length()-1));
        }
        int star_i = filter.indexOf("*");
        if (star_i > -1) {
            return name.startsWith(filter.substring(0, star_i))
                    && name.endsWith(filter.substring(star_i+1));
        }
        return name.contains(filter);
    }
    
    //formats accepted: 1KB, 1K, 1MB, 1M, 1G, 1GB (case insensitive, space allowed)
    //throws exception if any bad number given including null param
    public Long toByteCount(String readableSize) {
        long ret = -1;
        long m = 1; //multiplier
        String sizeLower = readableSize.toLowerCase();
        int s_i = sizeLower.length(); //string portion's length
        if (sizeLower.endsWith("k")) {
            s_i = sizeLower.length() - 1;
            m = 1024;
        }
        else if (sizeLower.endsWith("kb")) {
            s_i = sizeLower.length() - 2;
            m = 1024;
        }
        else if (sizeLower.endsWith("m")) {
            s_i = sizeLower.length() - 1;
            m = 1048576;
        }
        else if (sizeLower.endsWith("mb")) {
            s_i = sizeLower.length() - 2;
            m = 1048576;
        }
        else if (sizeLower.endsWith("g")) {
            s_i = sizeLower.length() - 1;
            m = 1073741824;
        }
        else if (sizeLower.endsWith("gb")) {
            s_i = sizeLower.length() - 2;
            m = 1073741824;
        }
        else if (sizeLower.endsWith("t")) {
            //s_i = sizeLower.length() - 1;
            //m = 1099511627776;  // number is too large for long int
            throw new NumberFormatException("terabytes not accepted");
        }
        else if (sizeLower.endsWith("tb")) {
            //s_i = sizeLower.length() - 2;
            //m = 1099511627776;  // number is too large for long int
            throw new NumberFormatException("terabytes not accepted");
        }
        else if (sizeLower.endsWith("bytes")) {
            if (sizeLower.endsWith("kilobytes")) {
                s_i = sizeLower.length() - 9;
                m = 1024;
            }
            else if (sizeLower.endsWith("megabytes")) {
                s_i = sizeLower.length() - 9;
                m = 1048576;
            }
            else if (sizeLower.endsWith("gigabytes")) {
                s_i = sizeLower.length() - 9;
                m = 1073741824;
            }
            else if (sizeLower.endsWith("terabytes")) {
                //s_i = sizeLower.length() - 9;
                //m = 1099511627776;  // number is too large for long int
                throw new NumberFormatException("terabytes not accepted");
            }
            else {
                // assume bytes
                s_i = sizeLower.length() - 5;
                m = 1;
            }
        }
        // else assume all numbers (bytes) so leave s_i and m at defaults
        ret = Long.parseLong(readableSize.substring(0,s_i).trim()) * m;
        return ret;
    }
  
    // My Asynchronous task
    public void executeSearch() {
        match_count = 0;
        enable = true;
        if (searchThread!=null) {
            if (mListener!=null) {
                mListener.setStatus("Search is busy. Wait or cancel first.");
            }
        }
        opt_name_string = options.get("name_string");
        opt_name_lower = null;
        if (opt_name_string != null) {
            opt_name_string = opt_name_string.trim();
        }
        if ((opt_name_string != null) && (opt_name_string.length() > 0)) {
            opt_name_lower = opt_name_string.toLowerCase();
        }
        if (opt_name_lower != null) {
            int star_i = opt_name_lower.indexOf("*");
            if (star_i > -1) {
                int star2 = opt_name_lower.indexOf("*", star_i+1);
                if (star2 > -1) {
                    if (!((star_i==0) && (star2==opt_name_lower.length()-1))) {
                        // if two stars but not *x*
                        throw new RuntimeException("Unsupported filename filter");
                    }
                }
            }
        }
//        FilenameFilter ff = new FilenameFilter() {
//            public boolean accept(File dir, String name) {
//                String name_lower = name.toLowerCase();
//                if (opt_name_string == null) return true;
//                String name_lower = opt_name_string.toLowerCase();
//                return is_like_cs(name_lower, name_lower);
//            }
//        };        
        opt_content_string = options.get("content_string");
        if (!is_truthy(options.get("content_enable"))) this.opt_content_string = null;
        String ifar_enable_s = options.get("include_folders_as_results_enable");
        opt_ifar_enable = false;
        if (ifar_enable_s != null) {
            if (is_truthy(ifar_enable_s)) {
                opt_ifar_enable = true;
            }
        }
        opt_min_size = 0;
        String opt_min_size_s = options.get("min_size");
        if (!is_truthy(options.get("min_size_enable"))) opt_min_size_s = null;
        else if (opt_min_size_s != null) {
            try {
                opt_min_size = toByteCount(opt_min_size_s);
                System.out.println("max_size: " + Long.toString(opt_min_size));
            }
            catch (NumberFormatException ex) {
                opt_min_size = 0;
                if (mListener != null) {
                    mListener.markBadEntry("min_size");
                    mListener.setStatus("Minimum size is typed incorrectly.");
                    return;
                }
            }
        }
        
        opt_max_size = Long.MAX_VALUE;
        String opt_max_size_s = options.get("max_size");
        if (!is_truthy(options.get("max_size_enable"))) opt_max_size_s = null;
        else if (opt_max_size_s != null) {
            try {
                opt_max_size = toByteCount(opt_max_size_s);
                System.out.println("max_size: " + Long.toString(opt_max_size));
            }
            catch (NumberFormatException ex) {
                opt_max_size = Long.MAX_VALUE;
                if (mListener != null) {
                    mListener.markBadEntry("max_size");
                    mListener.setStatus("Maximum size is typed incorrectly");
                    return;
                }
            }
        }
        
        if (opt_max_size < opt_min_size) {
            mListener.markBadEntry("min_size");
            mListener.markBadEntry("max_size");
            mListener.setStatus("Maximum size must be greater than or equal to minimum.");
            return;
        }
        //System.out.println("opt_ifar_enable: " + (opt_ifar_enable?"true":"false"));
        //if (opt_name_lower == null) {
            // check if listener is registered.
            //if (mListener != null) {
            //    mListener.setStatus("Nothing to do (no filename given).");
            //}
            //return;
        //}
        String locations_s = options.get("location_paths");
        if (locations_s == null || locations_s.trim().length()==0) {
            // check if listener is registered.
            if (mListener != null) {
                mListener.setStatus("Nothing to do (no location given).");
            }
            return;
        }
        // An Async task always executes in new thread
        // lambda version suggested by NetBeans (couldn't parse file
        // due to misspelled variable before that but it didn't know why)
        // based on https://www.geeksforgeeks.org/asynchronous-synchronous-callbacks-java/
        searchThread = new Thread(() -> {
            // perform any operation
            //System.out.println("Performing operation in Asynchronous Task");
            if (mListener != null) {
                mListener.setStatus("Searching...");
            }
            String[] location_paths = locations_s.split(";");
            for (String location_path : location_paths) {
                if (location_path.trim().length()>0) {
                    File major_di = new File(location_path);
                    executeSearchRecursively(major_di, 0);
                }
            }
            String msg = "Finished searching " + Integer.toString(location_paths.length) + " path(s)";
            msg += " (" + Long.toString(match_count) + " result(s))";
            if (!enable) msg = "You cancelled the search.";
            if (mListener != null) {
                mListener.setStatus(msg);
            }
            searchThread = null;
            enable = false;
        });
        searchThread.start();
    }
    
    private boolean get_is_folder_searchable(File this_di, boolean device_allow_enable) {
        return true;  // TODO: implement this
    }
    
    public boolean get_is_match(File this_fi) {
        if (this.opt_content_string != null) {
            File locationsFile = this_fi;
            boolean found = false;
            if (locationsFile.isFile()) {
                try (BufferedReader br = new BufferedReader(new FileReader(locationsFile.getAbsolutePath()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains(this.opt_content_string)) {
                            found = true;
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DFF.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
            if (!found) return false;
        }
        if (this_fi.length() < this.opt_min_size) return false;
        if (this_fi.length() > this.opt_max_size) return false;
        // TODO: check:
//        modified_start_date_enable:false
//        modified_start_time_enable:true
//        modified_start_datetime_utc:2017-12-04 01:43 AM UTC
//        modified_endbefore_date_enable:false
//        modified_endbefore_time_enable:false
//        modified_endbefore_datetime_utc:2017-06-10 04:00 AM UTC
//        content_enable:false
//        recursive_enable:true
//        content_string:book
        
        return true;
    }
    
    //see also FilenameUtils.getExtension from Apache Commons IO
    public static String getExtension(String path) {
        String ret = "";
        String slash = "\\";
        if (path.contains("/")) {
            slash = "/";
        }
        int slash_i = path.lastIndexOf(slash);
        String name = path;
        if (slash_i > -1) name = path.substring(slash_i+1);
        int dot_i = name.lastIndexOf(".");
        if (dot_i > -1) ret = name.substring(dot_i+1);
        return ret;
    }
    
//    private boolean isBinary(String path) {
//        
//    }
    
    private String executeSearchRecursively(File major_di, int depth) {
        String err = null;
        if (major_di != null) {
            if (depth < 3) {
                if (mListener != null) {
                    mListener.setStatus(major_di.getAbsolutePath()+"...");
                }
            }
            if (depth == 0 || get_is_folder_searchable(major_di, true)) {
                File[] subs;
                //boolean filenames_prefiltered_enable = opt_name_string.contains("*");
                if (!enable) return null;
                subs = major_di.listFiles();
                if (subs!=null) {
                    for (File sub : subs) {
                        if (!enable) break;
                        try {
                            if (sub.isDirectory() && get_is_folder_searchable(sub, depth==0)) {
                                if (opt_ifar_enable) { //depth != 0 && 
                                    if (opt_name_lower==null || is_like_cs(sub.getName().toLowerCase(), opt_name_lower)) {
                                        if (this.get_is_match(sub)) {
                                            match_count++;
                                            if (mListener != null) {
                                                Map<String, String> result = new HashMap<>(); //older Java requires: <String, String>
                                                result.put("Path", sub.getAbsolutePath());
                                                result.put("Ext", "<Folder>");
                                                result.put("Modified", Long.toString(sub.lastModified()));
                                                BasicFileAttributes attr;
                                                attr = Files.readAttributes(sub.toPath(), BasicFileAttributes.class);
                                                //result.put("Created", Long.toString(sub.lastModified()));
                                                result.put("Created", Long.toString(attr.creationTime().toMillis()));
                                                result.put("Name", sub.getName());
                                                mListener.onMatchEvent(result);
                                            }
                                        }
                                    }
                                    else System.out.println(sub.getName() + " is not like " + opt_name_lower);
                                }
                                if (is_truthy(options.get("recursive_enable")))
                                    executeSearchRecursively(sub, depth+1);
                            }
                            else {
                                if (opt_name_lower==null || is_like_cs(sub.getName().toLowerCase(), opt_name_lower)) {
                                    if (this.get_is_match(sub)) {
                                        match_count++;
                                        if (mListener != null) {
                                            Map<String, String> result = new HashMap<>();
                                            result.put("Path", sub.getAbsolutePath());
                                            result.put("Ext", getExtension(sub.getName()));
                                            result.put("Modified", Long.toString(sub.lastModified()));
                                            BasicFileAttributes attr;
                                            attr = Files.readAttributes( sub.toPath(), BasicFileAttributes.class);
                                            //attr = Files.readAttributes(sub.toPath(), "*");

                                            //result.put("Created", Long.toString(sub.lastModified()));
                                            result.put("Created", Long.toString(attr.creationTime().toMillis()));
                                            result.put("Name", sub.getName());
                                            mListener.onMatchEvent(result);
                                        }
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(DFF.class.getName()).log(Level.SEVERE, null, ex);
                        }                        
                    }
                }
            }
        }
        return null;
    }  //end executeSearchRecursively

    public void saveState() {
        saveRecent();
        saveSettings();
    }

}  // end class DFF
