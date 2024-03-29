package com.SS.Main;

/**
 * This class is used as a global variables class. with everything static, this class 
 * never has an instance and there has no construct, just simply holds values as 
 * transitions are made thorugh different activities.
 *
 * @author Daniel Moody
 */
public class Values{
    
    public static String CurrentIP;
    public static String HttpCommandsResult;
    public static boolean LoggedIn = false;
    public static String UserLoggedIn = null;
    public static String PassLoggedIn = null;
    public static String NameLoggedIn = null;
    public static String UserToBeEdited;
    public static String ModToBeEdited;
    public static boolean clickdisable = false;
    public static String[] Usernames;
    public static String[] ModNumbers;
    public static boolean DBconnection = false;
    public static String LoggedinUN;
    public static String LoggedinPass;
    public static String LoggedinName;
    public static String LoggedinSec;
    public static String NextModNumber;
    public static boolean MasterUser = false;
    public static String[] DatabaseStrings;
    public static Object SyncToken = new Object();
}