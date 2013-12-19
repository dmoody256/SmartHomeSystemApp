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

/**
 * 
 * @author Daniel Moody
 * 
 * Code Description 
 * Written by Daniel Moody - 11/15/2012
 * 
 * This activity is used to show the master user a current list of all users that are in the system.
 * This list will update itself at the begining of this activity so to make itself current with the 
 * of the web server. This list will then allow the user to select another user to make changes to there
 * user info or delete the user in the edit a user screen. This activity uses an async task to talk to
 * the web server and retirve the latest list of users.
 * 
 */

public class EditUsersActivity extends Activity {

    ListView UserList;              // The listview that will display the current users
    Button EditUserBackButton;      // A button to return to the previous screen
    Button EditUserHomeButton;      // A button to return to the home screen
    SQLiteAdapter mySQLiteAdapter;  // The database connection
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Activity Initialization
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Create the instace of this activity
        setContentView(R.layout.editusers); // Connect this activity to the GUI layout
        
        EditUserBackButton = (Button)findViewById(R.id.euback); // Connect the activity objects 
        EditUserHomeButton = (Button)findViewById(R.id.euhome); // to the GUI objects
        UserList = (ListView)findViewById(R.id.euuserlist); 
        
        mySQLiteAdapter = new SQLiteAdapter(this);   // Create the database connection
        
        mySQLiteAdapter.openToRead();                // Open the database for reading
        
        Cursor cursor = mySQLiteAdapter.queueAll();  // Get a cursor of all info in the database
        startManagingCursor(cursor);                 // and set a manager for it

        String[] from = new String[]{SQLiteAdapter.KEY_USER}; // select for usernames
           int[]   to = new int[]   {android.R.id.text1};     // connect the username to a spot in the listview

        SimpleCursorAdapter cursorAdapter =          // Create the cursor adapter to connect ot the list
         new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, from, to);
        UserList.setAdapter(cursorAdapter);
        
        final String[] usernames = mySQLiteAdapter.getUsernames();  // get the usernames for later
        
        mySQLiteAdapter.close(); // we have all the info we need now close the database
        
        new UpdateUsers().execute("http://" + Values.CurrentIP + "/USER.txt"); // Update the current list ot that which is on the web server
        
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////Activity Buttons
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /*
         * This button is used to go back to the previous screen, which in the case will usually be the Home screen.
         * This function sets the click listener that will execute each time the button is clicked. In this case it 
         * create an intent to start the new activty.
         */
        EditUserBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                Intent RegisterIntent = new Intent(getApplicationContext(), RegisterUserActivity.class);
                startActivity(RegisterIntent); // send the user to the previous screen
                finish();
            }
        });

        /*
         * This button is used to go back to the home screen. This function sets the click listener that will execute each 
         * time the button is clicked. In this case it create an intent to start the new activty to the Home Screen.
         */
        EditUserHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        
                Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(SmartHomeSystemIntent); // send the user to the home screen
                finish();
            }
        });
        
        /*
         * This button will allow the user to click a user form the user list in order to switch them to the edit screen. It records the current
         * selection into a global liek variable. Whent eh user gets to edit screen it will know which one to edit.
         */
        UserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                
                Values.UserToBeEdited = usernames[position]; // save the user that was clicked
                Intent EditAUserIntent = new Intent(getApplicationContext(), EditAUserActivity.class);
                startActivity(EditAUserIntent); // send the user to the edit user screen
                finish();
            }
        });
    }
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////UpdateUsers AsyncTask
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /* This class runs an Async task to update the user database when this activity is started. This is run in 
     * a serperate thread as required by the latest Android OS. This is to prevent the UI from freezing while the 
     * data is being transfered. The Async task will retrive the latest database from the web server, and load it local 
     * database with the latest values.
     */
    private class UpdateUsers extends AsyncTask<String, String, String> {
        protected String doInBackground(String... URItoSend) {
        
            int count = URItoSend.length;
            for (int i = 0; i < count; i++) { // for each URI passed to this async task
             
                BufferedReader in = null;
                    
                try {
                    HttpClient client = new DefaultHttpClient();     // Create the new client and request 
                    HttpGet request = new HttpGet();                 // objects
        
                    request.setURI(new URI(URItoSend[i]));           // Set the URI to the request object
                    HttpResponse response = client.execute(request); // Send the URI to access the web server
          
                    // Read in the response data to the buffer from the web server
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    
                    StringBuffer sb = new StringBuffer("");      // We will load the data into a string buffer
                    
                    if(in == null){                              // if no response was recieved the we are 
                        Values.DBconnection = false;             // unable to connect to  the web server
                        return "Database Unavialable";
                    }
                    
                    String line = "";                                 // create some variables for processing
                    String NL = System.getProperty("line.separator"); // the response
                    
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
                    
                    Values.DBconnection = true;                // record that a connection was successful
                    
                    mySQLiteAdapter.openToWrite();             // Open the database and then 
                    mySQLiteAdapter.deleteAllUsers();          // delete all the users to make room for 
                                                               // the new users
                                                               
                    for(int m = 1; m < Values.DatabaseStrings.length; m++){ // for each string returned from
                                                                            // web server database
                        String[] User = Values.DatabaseStrings[m].split(" ");
                        String DBUsername = User[0];           // Parse the data and place the user info
                        String DBPass = User[1];               // into the database into the respective
                        String DBName = User[2];               // fields
                        
                        mySQLiteAdapter.UserInsert(DBUsername, DBPass, DBName);
                    }
                    
                    mySQLiteAdapter.close(); // The data has been inserted now close the database

                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
                finally {
                    if (in != null) {
                        try {
                            in.close(); // make sure the buffer was closed
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Values.DBconnection = false;   // obsolete code check if this should be deleted
            return "Database Unavialable";
        }
        
        protected void onPostExecute(){
             
            mySQLiteAdapter.openToRead();                 // Open the database and get the
            Cursor cursor = mySQLiteAdapter.queueAll(); // current users
            
            SimpleCursorAdapter adapter = (SimpleCursorAdapter)UserList.getAdapter();
            adapter.changeCursor(cursor);               // connect the new adapter to the user list
            adapter.notifyDataSetChanged();             // and tell it to update
        
            mySQLiteAdapter.close();                    // The list has been updated close the database
        }
    }
}