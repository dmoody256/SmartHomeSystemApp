package com.SS.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * @author Daniel Moody
 * 
 * Code Description 
 * Written by Daniel Moody - 11/15/2012
 * 
 * This activity is the main activity which is launched into by the application.
 * the user can go to the login screen, or if already logged in will be able to see
 * choose from a range of options to navigate through the application.
 * 
 */
public class SmartHomeSystemActivity extends Activity {
    
    /*
     * Declare objects that will be used by this Android Activity
     */
    Button LoginButton;             // Button to send log in inof to server
    Button HomeStatusButton;        // Button to go to home status screen
    Button LogoutButton;            // button to log current user out
    Button UserAdminButton;         // Button to do admin functions on users
    SQLiteAdapter mySQLiteAdapter;  // database connection
    TextView LoginText;             // Displays the current users name

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Activity Initialization
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Create the instance of the activity
        setContentView(R.layout.main);      // Connect the activity to the GUI layout
        
        /*
         * After the instance of the Activity is created, the view objects
         * used by the activity need to be assigned to their respective xml
         * objects.
         */
        LoginButton = (Button)findViewById(R.id.mainloginbutton);     // connect the activity objects
        HomeStatusButton = (Button)findViewById(R.id.mainhomestatus); // to the GUI objects
        LogoutButton = (Button)findViewById(R.id.mainlogout);
        UserAdminButton = (Button)findViewById(R.id.mainuseradmin);
        LoginText = (TextView)findViewById(R.id.mainlogintext);
        
        //create the SQLite Adapter for the instance of this activity
        mySQLiteAdapter = new SQLiteAdapter(this);
        
        if(Values.LoggedIn == false){          // if no one is logged in
            HomeStatusButton.setVisibility(4); // then hide all the UI buttons
            LogoutButton.setVisibility(4);     // except the login button
            UserAdminButton.setVisibility(4);
            LoginButton.setVisibility(0);
            LoginText.setText("You are currently not loggin in.");
            
            new GetIP().execute("http://dmoody256.servebeer.com"); // check to see if there
                                                                   // is connection
        }
        else{   // There is a user logged in, show the UI buttons and username
            LoginText.setText("You are currently logged in as " + Values.LoggedinName + ".");
            HomeStatusButton.setVisibility(0);
            LogoutButton.setVisibility(0);
            
            if(Values.MasterUser == true){  // only show this button if its the master user
                UserAdminButton.setVisibility(0);
            }
            else{
                UserAdminButton.setVisibility(4);
            }
            LoginButton.setVisibility(4);
        }
        
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////Activity Buttons
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /*
         * This button will log the user out, by setting all the global logged in
         * variable to false/null and then sending the user back to this same screen, which will
         * display in a logged out form.
         */
        LogoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    
                Values.LoggedIn = false;     // Set the global logged in variables to 
                Values.LoggedinName = null;  // false or null to effectivly log the user out
                Values.LoggedinPass = null;
                Values.LoggedinUN = null;
                Values.MasterUser = false;
            
                Intent LoginIntent= new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(LoginIntent); // now send them to a new instance of this activity to have it
                finish();                   // reload in a logged off state
            }
        });
        
        /* 
         * This button will send the user to the user admin screen where they can register new user
         * or view a list of current users in the system. Only the logged in master user can see this
         * button.
         */
        UserAdminButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent AddUserIntent= new Intent(getApplicationContext(), RegisterUserActivity.class);
                startActivity(AddUserIntent); // send the user to the useradmin screen
                finish();
            }
        });
        
        /*
         * This button will send the user to the home status screen, which will display the current modules
         * connected to the system. 
         */
        HomeStatusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent LoginIntent= new Intent(getApplicationContext(), HomeStatusActivity.class);
                startActivity(LoginIntent); // send the user to the homestatus screen
                finish();
            }
        });
    
        /*
         * This button will sent the user to the login screen where the can login to the system.
         */
        LoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    
                Intent LoginIntent= new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(LoginIntent); // Send the user to the login screen
                finish();
            }
        });
    }
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////GetIP AsyncTask
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /*
     * This async task takes place in another thread and starts to get current information as soon as
     * the activity is launched, this is to reduce time waiting for the sytme to retrive info when logging in,
     * This will speed up the login time for the user. After the Getip task is finished it will call another
     * thread that will update the user list so when the user tries to log in it will be verified quickly.
     */
    private class GetIP extends AsyncTask<String, Void, String> {
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
                    String IPString = null;
                    String IP = "fail";
                    
                    // read through the redirect to find the IP
                    while (!(line = in.readLine()).contentEquals("<meta name=\"description\" content=\"\">")){
                       // Keep cycling
                    }
                    
                    
                    IPString = in.readLine();                       // IP string should be the next part
                    String[] IPStringSplit = IPString.split("/");   // get and split it
                    for(int j = 0; j < IPStringSplit.length;j++){   // collect just the IP
                        if(j == 2){
                            IP = IPStringSplit[j].toString();
                        }
                    }
                    
                    in.close();             // close the buffer and save the IP
                    Values.CurrentIP = IP; 
                    return IP;
                    
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close(); // make sure the buffer is closed
                            } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }
        
        protected void onPostExecute(String result) {
            // Now we got the IP lets get the users
            new UpdateUsers().execute("http://" + Values.CurrentIP + "/USER.txt");
        }
    }
   
package com.SS.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

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
    }
}