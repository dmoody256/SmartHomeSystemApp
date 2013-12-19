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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * @author Daniel Moody
 * Last updated 11/28/2012
 * 
 * This activity is used to add new users to the system. It can only be performed by the master
 * user, which is the first user to register when using the system. This will do some validation checking 
 * when attempting to insert a new user inot the database, such as string checking, duplicate users, and
 * too many users. It then uses an aysnc task to send the command through HTTP request to the web server.
 *
 */
public class RegisterUserActivity extends Activity{
    
    Button RegisterBackButton;  // Button to go back to previous screen
    Button RegisterHomeButton;  // Button to go to home screen
    Button RegisterUserButton;  // button to register a new user
    Button EditUserButton;      // button to go to user list screen
    EditText UsernameEdit;      // new username field
    EditText PassEdit;          // new user password field
    EditText NameEdit;          // new user name field

    SQLiteAdapter mySQLiteAdapter; // database connection
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Activity Initialization
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    // create the instance of this activity
        setContentView(R.layout.registeruser); // connect the activity to the GUI layout
        
        mySQLiteAdapter = new SQLiteAdapter(this);  // create the database connection
        
        RegisterBackButton = (Button)findViewById(R.id.ruback); //connect each of the activity objects
        RegisterHomeButton = (Button)findViewById(R.id.ruhome); // to the GUI objects
        RegisterUserButton = (Button)findViewById(R.id.rusubmit);
        EditUserButton = (Button)findViewById(R.id.rueditusers);
        UsernameEdit = (EditText)findViewById(R.id.ruusernameedit);
        PassEdit = (EditText)findViewById(R.id.rupassedit);
        NameEdit = (EditText)findViewById(R.id.ruemailedit);
        
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////Activity Buttons
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////       
       
        /*
         * This button simply returns the user to the previous screen, in this case it happen to be the
         * home screen so this button will have the same functionality as HomeStatusButton
         */
        RegisterBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                Intent LoginIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(LoginIntent); // send the user to the previous screen
                finish();
            }
        });
            
        /*
         * This button simply returns the user to the Home Screen, where they can then logout
         * change user information, or return to the module list.
         */
        RegisterHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        
                Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(SmartHomeSystemIntent); // send the user to the home screen
                finish();
            }
        });
        
        /* 
         * This button will bring the user to the user list screen to edit current users.
         */
        EditUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        
                Intent EditUserIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
                startActivity(EditUserIntent);
                finish();
            }
        });
        
        /*
         * This button will allow the user to add new users to the database. It will take the info put into the
         * text fields and check to see if the user exist and the data is valid. Then it will send a command to
         * the web server via HTTP to tell teh web server to add the current user to its database while also 
         * the local database.
         */
        RegisterUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                String UsernameString = UsernameEdit.getText().toString(); // get the new user info
                String PassString = PassEdit.getText().toString();
                String NameString = NameEdit.getText().toString();
                
                if(UsernameString.length() < 1){
                    Toast.makeText(getBaseContext(),
                            "Must enter a username", // make sure the username is valid
                            Toast.LENGTH_SHORT).show();
                }
                else if(PassString.length() < 1){
                    Toast.makeText(getBaseContext(),
                            "Must enter a password", // make sure the user password is valid
                            Toast.LENGTH_SHORT).show();
                }
                else if(NameString.length() < 1){
                    Toast.makeText(getBaseContext(),
                            "Must enter a name",     // make sure the name is valid
                            Toast.LENGTH_SHORT).show();
                }
                else{                                // Info is valid lets create the user
                
                    mySQLiteAdapter.openToRead();                           // open the database and get 
                    String[] usernames = mySQLiteAdapter.getUsernames();    // the current users
                    int DBcount = mySQLiteAdapter.count();                  // and count
                    mySQLiteAdapter.close();
                    
                    boolean userfound = false;
                   
                    for(int i = 0; i < usernames.length; i++){              // search through the name and 
                        if(UsernameString.compareTo(usernames[i]) == 0){    // make sure no duplicates
                            userfound = true;
                        }
                    }
                    
                    if(userfound == true){                    
                        Toast.makeText(getBaseContext(),
                                "User already exist",       // duplicate found notify user
                                Toast.LENGTH_SHORT).show();
                    }
                    else if(DBcount > 10){
                        Toast.makeText(getBaseContext(),
                                "Too many Users, Delete some",  // too many users notify user
                                Toast.LENGTH_SHORT).show();
                    }
                    else{                                       // Everything still looks good lets
                                                                // add this user
                        new RegisterUser().execute("http://" + Values.CurrentIP 
                            + "/reguser.htm?user=" + UsernameString +("%20")    // construct the URI command 
                            + PassString + ("%20")+ NameString + "%0D");        // to send to the web server
                        
                        mySQLiteAdapter.openToWrite();                          // insert the user to the local
                        mySQLiteAdapter.UserInsert(UsernameString, PassString, NameString); //database
                        mySQLiteAdapter.close();
                        
                        Intent LoginIntent= new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                        startActivity(LoginIntent);     // send user to the homepage on success
                        finish();
                    }
                }
            }// onClick
        }); // btnWriteSDFile
    }
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////RegisterUser AsyncTask
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * This async task will send the new user info to the web server to store it in the central database. It runs
     * in a seperate thread as required by the later Android OS, to prevent the UI from feezing while data is transfering.
     */
    private class RegisterUser extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... URItoSend) {
        
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
    }
}
