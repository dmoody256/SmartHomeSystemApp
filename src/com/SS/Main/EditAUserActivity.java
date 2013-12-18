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
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author Daniel Moody
 * Last updated 12/18/2013
 * 
 * This activity allows the user to make changes to the current users in the system. The user is selected from the
 * user list activity and then the email and password of a user can be changed. Only the master user can modifiy users
 * and the master user is the first user to login when there is no other master users. The user also has the option to 
 * delete users, and even themselves. After changes are made to a user the current user is sent back to the user list
 * or if they deleted themselves, they are logged out and sent back to the log in screen. This activty uses an async 
 * to communicate with the web server.
 *
 */

public class EditAUserActivity extends Activity{
    
    Button EditAUserBackButton;    // Button to go back to previous screen
    Button EditAUserHomeButton;    // Button to go back to home screen
    Button DeleteUserButton;       // Button to delete the current selected user
    Button EditInfoButton;         // Button to submit the user info changes
    
    EditText PassEdit;             // Text field to change the users password
    EditText EmailEdit;            // Text field to change the users email
    TextView UsernameEdit;         // The current users username can not be changed
    SQLiteAdapter mySQLiteAdapter; // The database adapapter

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Activity Initialization
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Create the instance of the activity
        setContentView(R.layout.editauser); // Set the Activity to the GUI object
        
        mySQLiteAdapter = new SQLiteAdapter(this); // Connect to the database
        
        EditAUserBackButton = (Button)findViewById(R.id.eauback); // Connect the activities objects to
        EditAUserHomeButton = (Button)findViewById(R.id.eauhome); // the corresponding GUI objects
        DeleteUserButton = (Button)findViewById(R.id.eaudelete);
        EditInfoButton = (Button)findViewById(R.id.eaueditusers);
        UsernameEdit = (TextView)findViewById(R.id.eauusernameedit);
        PassEdit = (EditText)findViewById(R.id.eaupassedit);
        EmailEdit = (EditText)findViewById(R.id.eauemailedit);
        
        mySQLiteAdapter.openToRead(); // Open the database to get current user details
        
        PassEdit.setText(mySQLiteAdapter.getPassword(Values.UserToBeEdited)); // Extract the user info
        EmailEdit.setText(mySQLiteAdapter.getEmail(Values.UserToBeEdited));
        UsernameEdit.setText(Values.UserToBeEdited);
        
        mySQLiteAdapter.close(); // User info has been extracted now we can close the database
        
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////Activity Buttons
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /*
         * This button is used to go back to the previous screen, which in the case will usually be the Userlist screen.
         * This function sets the click listener that will execute each time the button is clicked. In this case it 
         * create an intent to start the new activty.
         */
        EditAUserBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                Intent LoginIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
                startActivity(LoginIntent); // Go tot he Edit Users screen 
                finish();
            }
        });

        /*
         * This button is used to go back to the home screen. This function sets the click listener that will execute each 
         * time the button is clicked. In this case it create an intent to start the new activty to the Home Screen.
         */
        EditAUserHomeButton.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                        
                Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(SmartHomeSystemIntent); // Go to the home screen
                finish();
            }
        });
        
        /*
         * This button allows the user to delete other users. It gets the current suer data and sends it tot he web server
         * so that the web server will remove the user from the database. Then it sends the user back to the user list 
         * screen. The user can even delete themselves, in which case the user will be loged out and sent back to
         * the log in screen.
         */
        DeleteUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                String UsernameString = UsernameEdit.getText().toString(); // Get the user data
                String PassString = PassEdit.getText().toString();         // to send to the web server
                String EmailString = EmailEdit.getText().toString();
                
                new EditDelUser().execute("http://" + Values.CurrentIP     // Construct the URI using the 
                    + "/deluser.htm?user=" + UsernameString +("%20")       // current users info
                    + PassString + ("%20")+ EmailString + ("%0D"));
                
                mySQLiteAdapter.openToWrite();                             // Now delete the user from the 
                mySQLiteAdapter.deleteUser(Values.UserToBeEdited);         // local database as well
                mySQLiteAdapter.close();
                
                if(UsernameString.contentEquals(Values.LoggedinUN)){       // Check to see if the current user
                                                                           // is trying to delete themselves
                    Values.LoggedIn = false;                               
                    Values.LoggedinName = null; // Set the Values to no user
                    Values.LoggedinPass = null;
                    Values.LoggedinUN = null;
                    Values.MasterUser = false;
                
                    Intent LoginIntent= new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                    startActivity(LoginIntent); // Send the user back to the home screen were they will
                    finish();                   // need to login again
                }
                else{
                
                    Intent EditUsersIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
                    startActivity(EditUsersIntent); // send the user to the edit user list
                    finish();
                }
            }
        });
        
        /*
         * This click listener allows the user to edit other users info. The user changes the information 
         * the corresponding text fields and then a URI is generated to send to the web server. The user
         * is then sent back to the user list screen.
         */
        EditInfoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                String UsernameString = UsernameEdit.getText().toString(); // Gather any changes the
                String PassString = PassEdit.getText().toString();         // user made to the selected
                String EmailString = EmailEdit.getText().toString();       // user
                
                if(PassString.length() < 1){
                
                    Toast.makeText(getBaseContext(),      // If the password is not long enough
                            "Must enter a password",      // warn the user
                            Toast.LENGTH_SHORT).show();
                }
                else if(EmailString.length() < 1){
                
                    Toast.makeText(getBaseContext(),      // If the name is not long enough 
                            "Must enter a name",          // warn the user
                            Toast.LENGTH_SHORT).show();
                }
                else{
                
                    new EditDelUser().execute("http://" + Values.CurrentIP // If the password and name 
                        + "/edituser.htm?user=" + UsernameString +("%20")  // fields are valid then
                        + PassString + ("%20")+ EmailString + ("%0D"));    // send the changes to 
                                                                           // the web server
                                                                           
                    mySQLiteAdapter.openToRead();                          // We need to replicate
                    String[] usernames = mySQLiteAdapter.getUsernames();   // changes in local database
                    mySQLiteAdapter.close();                               // so get current users
                    
                    mySQLiteAdapter.openToWrite();                         // To make the changes we delete
                    mySQLiteAdapter.deleteUser(UsernameString);            // the modified user and make
                    mySQLiteAdapter.UserInsert(UsernameString, PassString, EmailString); // a new one
                    mySQLiteAdapter.close();
                    
                    Intent EditUsersIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
                    startActivity(EditUsersIntent); // Now send the user back to the user list
                    finish();
                }
            }// onClick
        });
    }
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////EditDelUser AsyncTask
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   

    /* This private class is an asynch task that is used to communicate with the web server in a seperate thread.
     * this was made standard in the later Android OS versions, to prevent a bad conection from freezing up the 
     * the user interface. This Async task creates an HTTP connection to teh web server and sends the command
     * corresponding data through the internet.
     */
    private class EditDelUser extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... URItoSend) {
        
            int count = URItoSend.length;
            for (int i = 0; i < count; i++) { // For each URI that was passed to this AsyncTask
            
                BufferedReader in = null;
                    
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
                    
                    // Need code to check to make sure the response shows success
                    
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
            return null; // we dont need to do any post execute
        }

        protected void onPostExecute(String result){
            // Code use some post process notification maybe like a toast
        }
    }
}