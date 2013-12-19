package com.SS.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * 
 * @author Daniel Moody
 * Last updated 11/28/2012
 * 
 * This .java file is used for creation and function or the Home Status Activity in an
 * Android Application created for a Senior Design Project at UCF in Fall Semester 2012.
 * It give the use the ability to control power outler modules, by switching their state
 * from on to off and also takes in sensor data from the system. The Application is used 
 * for User Interface with this system. 
 *
 */

public class HomeStatusActivity extends Activity{
    
    /*
     * Declare objects that will be used by this Android Activity
     */
    SQLiteAdapter mySQLiteAdapter; // The SQLite database that will store users and modules
    Button HomeStatusBackButton;   // Button to take the user back to previous screen
    Button HomeStatusHomeButton;   // Button to take the user to the home screen
    Button UpdateStatusButton;     // Button to update the list of modules to get a current reading
    Button AddModButton;           // Button to add modules to the current list
    ListView ModuleList;           // This list will hold all the current modules and thier readings
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Activity Initialization
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);   // Create the activity
        setContentView(R.layout.homestatus);  // Set the layout and content
        
        /*
         * After the instance of the Activity is created, the view objects
         * used by the activity need to be assigned to their respective xml
         * objects.
         */
        
        mySQLiteAdapter = new SQLiteAdapter(this);           // create a SQLite adapter object
        ModuleList = (ListView)findViewById(R.id.hsmodlist); // Connect to GUI
        
        UpdateStatusButton = (Button)findViewById(R.id.hsgetstatus); // Connect to GUI
        UpdateStatusButton.setEnabled(false); // The update status button will be disabled at first 
                                              // until the initial update is complete
        
        AddModButton = (Button)findViewById(R.id.hsaddmod); // Connect to GUI
        AddModButton.setEnabled(false);  // The update status button will be disabled at first 
                                         // until the initial update is complete
                                         
        HomeStatusBackButton = (Button)findViewById(R.id.homestatusback); // Connect the mobility 
        HomeStatusHomeButton = (Button)findViewById(R.id.homestatushome); // buttons to the GUI

        /*
         * This if-else statement checks to see if the current user is the master user. 
         * The master user will have access to extra features like adding modules to the system.
         */
        if(Values.MasterUser == true){
            AddModButton.setVisibility(0); // Set the button to visible for Master User
        }
        else{
            AddModButton.setVisibility(4); // Set the button to invisible for other users
        }
        
        /*
         * We need to create a list view for the user to see the modules. The initial list may
         * not be up to date, so until the first update is complete the activity will present
         * what ever data is in the DB from the last update
         */
         
        mySQLiteAdapter.openToRead(); // Store the module numbers for quick later use
        
        Cursor cursor = mySQLiteAdapter.queueAllMod(); // Get all the current mods from the database
        startManagingCursor(cursor);                   // Set to be managed
        
        String[] from = new String[]{SQLiteAdapter.KEY_MODNUM, SQLiteAdapter.KEY_LOCATION, // Select what data from the 
            SQLiteAdapter.KEY_MODRELAY, SQLiteAdapter.KEY_MODMS, SQLiteAdapter.KEY_MODSS}; // database we are going to get
            
        int[] to = new int[]{R.id.modlistmodnumber, R.id.modlistlocation, // Set the data to the locations
            R.id.modlistrelay, R.id.modlistms, R.id.modlistss};           // within each row of the list
            
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, // Create the adapter to link 
            R.layout.modlist, cursor, from, to);                          // to the listview and set the   
        ModuleList.setAdapter(cursorAdapter);                             // adapter to list so the modules are displayed 
       
        Values.ModNumbers = mySQLiteAdapter.getModNumbers();  // Store the module numbers for quick later use
        mySQLiteAdapter.close();                              // Close the database
        
        /*
         * After the module list is setup, and AsyncTask update will start to retrieve the current system status.
         * The AsyncTask is created as a private subclass in this class. It will take in a String which
         * is a URL to the web server that is monitoring the system. The URL is constructed and then passed the
         * subclass. CurrentIP is the IP address that is received from NO-IP.com, a DDNS that gathers the current 
         * IP address of the Web Server from the first activity created when the application is launched. The web server 
         * is actually a 80MHz low cost micro-controller and can only process simple HTTP commands like GET and POST. 
         * Here UpdateModules class is passed 2 URLS, the first one gets a status update, the second gets a text file
         * that contains all the module information.
         */
       
        new UpdateModules().execute(                                     // Start an async task to tell the server
            "http://" + Values.CurrentIP + "/aled.htm?led=statusupdate", // to update the status and return a list
            "http://" + Values.CurrentIP + "/MODULE.txt");               // of current module stats
       
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////Activity Buttons
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        
        
        /*
         * When an update is started the update and add module buttons need to be disable to prevent the user from 
         * sending to many update request and slowing down the system and possible causing errors. They will be 
         * re-enabled in UpdateModules onPostExecute(). 
         */
        UpdateStatusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            
                UpdateStatusButton.setEnabled(false); // disable the UpdateStatusButton
                AddModButton.setEnabled(false);       // and the AddModButton
                
                new UpdateModules().execute(                                     // Start an async task to tell the server
                    "http://" + Values.CurrentIP + "/aled.htm?led=statusupdate", // to update the status and return a list
                    "http://" + Values.CurrentIP + "/MODULE.txt");               // of current module stats
            }
        });
             
        /*
         * If the user is a master user then they will be able to edit modules properties by
         * clicking and holding the module in the list. if not then the long click listener will not work
         */
        ModuleList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                
                if(Values.MasterUser == true){ // Check to see if current user is master
                
                    Values.ModToBeEdited = Integer.toString(position+1); // This will pass the mod being edited 
                    Intent EditAUserIntent = new Intent(getApplicationContext(), EditAModuleActivity.class);
                    startActivity(EditAUserIntent);                      // Start the next activity to edit mods
                    finish();
                }
                return false;
            }
        });
        
        /*
         * Another AsyncTask subclass that is used for sending commands to turn lights off and on. It takes the 
         * current position that is clicked when the user clicks a button from the list.
         */
        ModuleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                
                AddModButton.setEnabled(false);       // Disable the buttons because 
                UpdateStatusButton.setEnabled(false); // this button will also run an update
                
                new CommandsTask().execute(           // Send a command to the web server to turn a light off
                    "http://" + Values.CurrentIP + "/aled.htm?led=" + Integer.toString(position+1));
            }     
        });
        
        /*
         * adding a module will disable the update and add module buttons to prevent the user from sending to many request
         * to the web server and causing problems. The AddModuleTask will run the update task when it is finish telling the 
         * web server to add a new module. It will also check to make sure that there are less then 10 modules, as the web 
         * server will began to slow down if there are 10 or modules in the system. The URL that is created is a default 
         * module string used in the web server text files. NextModNumber is +1 of the current modules in the system and 
         * checks the current number of modules. 
         */    
        AddModButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                if(Integer.parseInt(Values.NextModNumber) < 10){ // Make sure there aren't too many mods
                    AddModButton.setEnabled(false);              // Disable buttons becuase this will update
                    UpdateStatusButton.setEnabled(false);        // the modlist, also disable the list becuase we 
                    ModuleList.setLongClickable(false);          // need to reorganize the list on the web server
                    
                    new CommandsTask().execute(                                    // Send a command to the web server 
                        "http://" + Values.CurrentIP + "/addmod.htm?user="         // telling it to add a mod and pass 
                        + Values.NextModNumber + "%20Unnamed,0.0%200%200%200%0D"); // the initializer string
                }
                else{
                    Toast.makeText(getBaseContext(), // If there are already to many modules 
                        "Max Modules reached",       // notify the user and don't add a mod
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        /*
         * This button simply returns the user to the previous screen, in this case it happen to be the
         * home screen so this button will have the same functionality as HomeStatusButton
         */
        HomeStatusBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(SmartHomeSystemIntent);  // Return to the previous screen
                finish();
            }
        });
        
        /*
         * This button simply returns the user to the Home Screen, where they can then logout
         * change user information, or return to the module list.
         */
        HomeStatusHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(SmartHomeSystemIntent); // Return to the home screen
                finish();
            }
        });

    }//End of Activity Class
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////CommandsTask AsyncTask
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private class CommandsTask extends AsyncTask<String, Void, String> {
         
        /*
         * This class uses apache libraries to send an HTTP GET request to the web server.
         * It also receives a response from the web server, but does not use the response.
         * I plan to add some error handling by making sure that a correct response is received
         * because the web server will echo get request.
         */
        protected String doInBackground(String... URItoSend) {
        
            /*
             * The connection is created and received in a background thread to keep the UI from being slowed down while
             * the connection is made.
             */
            int count = URItoSend.length;
            for (int i = 0; i < count; i++) { // For each URI pass to this AsyncTask (first the update status command
                                              // then the get Modules stats command
                BufferedReader in = null;     // Hold our return values while we read in the data
                
                try {
                    HttpClient client = new DefaultHttpClient();     // Create the new client and request 
                    HttpGet request = new HttpGet();                 // objects
            
                    request.setURI(new URI(URItoSend[i]));           // Set the URI to the request object
                    HttpResponse response = client.execute(request); // Send the URI to access the web server
              
                    // Read in the response data to the buffer from the web server
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    
                    StringBuffer sb = new StringBuffer("");           // Set up some variables to
                    String line = "";                                 // use for sorting through the data
                    String NL = System.getProperty("line.separator");
                    
                    while ((line = in.readLine()) != null) {          // Read through each line of data and 
                        sb.append(line + NL);                         // stick it the StringBuffer
                    }
                    
                    in.close(); // close the buffer
                    
                    // Need to code some form of response checking here to 
                    // make sure the response actually went through

                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally{
                    if (in != null) { // make sure to close the buffer no matter what
                        try {
                            in.close();
                               } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null; // Since this was a command we do not need to pass any data to the post execute
        }

        protected void onPostExecute(String result) {
            
            /*
             * After the command is sent,it will automatically update, to keep the system current.
             * however the amount of updates needs to be limited, so a boolean is used to disable the update if 
             * there is already an update process. 
             */
            if(Values.clickdisable == true){ // Check to make sure that there are't already ongoing updates
            
                new UpdateModules().execute(                                      // Start an async task to tell the server
                    "http://" + Values.CurrentIP + "/aled.htm?led=statusupdate",  // to update the status and return a list
                    "http://" + Values.CurrentIP + "/MODULE.txt");                // of current module stats
            }
        }
    }//End of CommandsTask Class
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////UpdateModules AsyncTask
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private class UpdateModules extends AsyncTask<String, String, Cursor> {
            
        /*
         * this class updates the modules by sending a status update request, which tells the web server to get the modules 
         * text file ready to send an update. Then it request the modules text file from the web sever. It is hard coded to 
         * receive 2 URLs; always the status update first, then the module text file request 2nd.
         */
        protected Cursor doInBackground(String... URItoSend) {
            
            /*
             * The do in background portion use standard apache http get request to communicate with the web server.
             * For the status update the request is sent normally and then a response is receive, but I have not set up any
             * any use for it yet, although it can be used for error handling, to make sure the web server is communicating 
             * correctly. The text file received from the web server is handled slightly differently and is described in more 
             * detail below.
             */
            int count = URItoSend.length;
            for (int i = 0; i < count; i++) { // For each URI pass to this AsyncTask (first the update status command
                                              // then the get Modules stats command
                BufferedReader in = null;     // Hold our return values while we read in the data
                try {
                    
                    HttpClient client = new DefaultHttpClient();     // Create the new client and request 
                    HttpGet request = new HttpGet();                 // objects
            
                    request.setURI(new URI(URItoSend[i]));           // Set the URI to the request object
                    HttpResponse response = client.execute(request); // Send the URI to access the web server
              
                    // Read in the response data to the buffer from the web server
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    StringBuffer sb = new StringBuffer("");           // Set up some variables to
                    String line = "";                                 // use for sorting through the data
                    String NL = System.getProperty("line.separator");
                    
                    if(i == 0){     //we are just sending a command in thise case
                        while ((line = in.readLine()) != null) {      // Read through each line of data and 
                            sb.append(line + NL);                     // stick it the StringBuffer
                        }
                        in.close(); // Close the buffer
                    }
                        
                    //this is the case where the get request is for the web server text file
                    if(i == 1){
                    
                        /*
                         * When receiving the text file, it is received as a long string and then parsed according to
                         * certain characters. At first the string read one line at a time. For some reason, I am not
                         * sure why, but the web server was sending the text files with random bytes in between the actual
                         * characters. My quick and dirty solution to this was to parse out only the characters that were 
                         * correct.
                         */
                        while ((line = in.readLine()) != null) {
                            
                            StringBuilder builder = new StringBuilder(); // Create the string builder for the module list
                            
                            for(int k = 0; k < line.length(); k++){             // Here the for loop parses the response  
                                if(line.charAt(k) < 128 && line.charAt(k) > 0){ // lookingfor characters that are 
                                                                                // looking ASCII characters and not 
                                    builder.append(line.charAt(k));             // random bytes
                                }
                            }
                            
                            if(builder.length() != 0){   // Stick a new line at the end of the whole thing
                                sb.append(builder + NL);
                            }
                        } // End of while read line in loop
                        
                        in.close(); // close the buffer
                        
                        String page = sb.toString();               // Create the module string
                        Values.DatabaseStrings = page.split("\n"); // and parse it for each module
                        
                        /*
                         * after the String has been parsed and the correct characters are in place, the Strings need to be 
                         * parsed and the correct data is then entered into the database.
                         */
                        try{
                            String DBModNumber, DBModLocation, DBModRelayStatus;   // These strings are used to store
                            String DBModManualStatus, DBModMSStatus, DBModSSStatus; // each of the mods corresponding
                            String DBModMSTimed;                                    // values to create insert string
                            String[] Mod;                                           // for the database
                         
                            mySQLiteAdapter.openToWrite();   // open the database and delete the current
                            mySQLiteAdapter.deleteAllMods(); // database to inser current modules
                             
                            Values.NextModNumber = Integer.toString(Values.DatabaseStrings.length); // Get the current total 
                                                                                    // number of modules to update our Values
                            for(int j = 1; j < Values.DatabaseStrings.length; j++){ // For each string process the values 
                                                                                    // and insert into the database
                          
                                Values.DatabaseStrings[j] = 
                                    Values.DatabaseStrings[j].replace('.', ' ');    // Replace the web servers delimitors                                                          
                                Values.DatabaseStrings[j] =                         // with a space for simplicity
                                    Values.DatabaseStrings[j].replace(',', ' ');
                                    
                                Mod = Values.DatabaseStrings[j].split(" ");         // seperate the values and insert
                                DBModNumber = Mod[0];                               // by position into the database
                                DBModLocation = Mod[1];
                                DBModMSTimed = Mod[2];
                                DBModRelayStatus = Mod[3];
                                DBModManualStatus = Mod[4];
                                DBModMSStatus = Mod[5];
                                DBModSSStatus = Mod[6];

                                if(DBModRelayStatus.contentEquals("1")){ // the web server uses 1 and 0s to store 
                                    DBModRelayStatus = "Power\nON";      // the data, here I convert the binary data 
                                } else {                                 // to it actual meaning
                                    DBModRelayStatus = "Power\nOFF";                                    
                                }
                                
                                if(DBModMSStatus.contentEquals("1")){               
                                    DBModMSStatus = "Motion\nDETECTED";                                    
                                } else {  
                                    DBModMSStatus = "Motion\nUNDETECTED";
                                }
                                
                                if(DBModSSStatus.contentEquals("1")){                                    
                                    DBModSSStatus = "Door/Window\nOPEN";                                    
                                } else {                                      
                                    DBModSSStatus = "Door/Window\nCLOSED";
                                }
                                
                                mySQLiteAdapter.InsertMod(DBModNumber, DBModLocation,  // The data is ready to be inserted 
                                    DBModMSTimed, DBModRelayStatus, DBModManualStatus, // into the database
                                    DBModMSStatus, DBModSSStatus);                                
                            } // End of for each module loop

                            mySQLiteAdapter.close(); // close the database
                        }
                         catch(Exception e){
                             e.printStackTrace();
                         }
                    } //End if(i==1)
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (in != null) { // make sure the buffer is closed
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null; // Don't pass anything to post execute
        }

        protected void onPostExecute(Cursor result) {
             /*
              * now that the module DB has been updated, the List View needs to be updated and the buttons that are 
              * disabled from updating need to be re-enabled. The Adapter is reset and then the view is notified that 
              * its data has been changed and it updates itself.
              */
            UpdateStatusButton.setEnabled(true);                // Enable the buttons
            AddModButton.setEnabled(true);
            ModuleList.setLongClickable(true);
            Values.clickdisable = true;                         // Enable updates
            
            mySQLiteAdapter.openToRead();                       // Open the database to get the new values
             
            Cursor cursor = mySQLiteAdapter.queueAllMod();      // Get the cursor from the database
            SimpleCursorAdapter adapter = 
                (SimpleCursorAdapter) ModuleList.getAdapter();  // Get the list adapter from the list to update it
            
            adapter.changeCursor(cursor);                       // update the list to a new cursor
            adapter.notifyDataSetChanged();                     // and notify it of changes
        
            mySQLiteAdapter.close();                            // close the database

         }
    }//End of Update Modules Task
}