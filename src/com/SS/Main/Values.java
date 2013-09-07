package com.SS.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.io.*;
import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import android.os.Environment;
import android.widget.Toast;

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