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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Daniel Moody
 * Last updated 12/18/2013
 * 
 * This activity is used to edit modules in the modules list. It is accessed by clicking and holding a module
 * in the module list in the HomeStatusActivty. This activity allows the user to change values of the modules
 * location and if the module should be motion sensor controlled. The user can also choose to delete the
 * if it is the last module on the list. The reason only the last module can be deleted is that the web server
 * database is limited from memory and needs code to support removing modules from the middle of the list. Read
 * below for more details about this. After the user has deleted the module or made changes they are sent back
 * the module list screen and an update starts to change there list.
 * 
 */

public class EditAModuleActivity extends Activity{
    
    Button EditAModBackButton;     // Button to go back to previous screen
    Button EditAModHomeButton;     // Button to go back to home screen
    Button DeleteModButton;        // Button to delete the current module
    Button EditModButton;          // Button to submit the edits of this module
    
    CheckBox MScontrolled;         // A check box to let module be switched off or 
                                   // by a connected motion sensor
    
    EditText LocationEdit;         // A text field to enter a location of this module
    TextView ModnumberText;        // The module number can not be changed
    SQLiteAdapter mySQLiteAdapter; // The database to update the new module details

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Activity Initialization
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);         // Create the instance of the activity
        setContentView(R.layout.editamod);          // Connect the GUI layout to this activity
        
        mySQLiteAdapter = new SQLiteAdapter(this);  // Create a database connection instance
        
        MScontrolled = (CheckBox)findViewById(R.id.eammscheck);      // Connect each of the variables to
        EditAModBackButton = (Button)findViewById(R.id.eamback);     // the corresponding GUI object
        EditAModHomeButton = (Button)findViewById(R.id.eamhome);
        DeleteModButton = (Button)findViewById(R.id.eamdelete);
        EditModButton = (Button)findViewById(R.id.eameditusers);
        ModnumberText = (TextView)findViewById(R.id.eammodnumedit);
        LocationEdit = (EditText)findViewById(R.id.eamlocationedit);
        
        mySQLiteAdapter.openToRead(); // Open the database to get the current modules details
        
        LocationEdit.setText(mySQLiteAdapter.getLocation(Values.ModToBeEdited));     // Fill in the current modules 
        ModnumberText.setText(Values.ModToBeEdited);                                 // details into each of the 
        String MSControlledValue = mySQLiteAdapter.getMStimed(Values.ModToBeEdited); // corresponding fields
        
        if(MSControlledValue.contentEquals("1")){ // Convert the string into a checkbox value
             MScontrolled.setChecked(true);
        } else {
             MScontrolled.setChecked(false);
        }
        
        mySQLiteAdapter.close(); // We have extracted the current modules details now close the database 
        
        if(Integer.parseInt(Values.NextModNumber)-1      // If the current module is not the last in the list
            != Integer.parseInt(Values.ModToBeEdited)){  // then we can not delete it becuase this would break
            DeleteModButton.setEnabled(false);           // the database on the web server. Needs a solution!
        } else{
            DeleteModButton.setEnabled(true); // This module is the last in the list so we can delete it
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////Activity Buttons
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        /*
         * This button is used to go back to the previous screen, which in the case will usually be the HomeStatusScreen.
         * This function sets the click listener that will execute each time the button is clicked. In this case it 
         * create an intent to start the new activty.
         */
        EditAModBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                Intent LoginIntent = new Intent(getApplicationContext(), HomeStatusActivity.class);
                startActivity(LoginIntent); // Start the new activity to return to previous screen
                finish();
            }
        });

        /*
         * This button is used to go back to the home screen. This function sets the click listener that will execute each 
         * time the button is clicked. In this case it create an intent to start the new activty to the Home Screen.
         */
        EditAModHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        
                Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(SmartHomeSystemIntent); // Start the new activity to return to home screen
                finish();
            }
        });
        
        /*
         * This sets the click listener for the DeleteModButton, allowing the user to delete the current module if it
         * is the last in the list. The web server database uses a simple text file with character delimited fields
         * so the database is unable to remove modules unless it is at the end. This can be fixed, but requires modifing
         * the spin code for the web server. The soltuion will also require, either upgrading the Spinnerets ram size,
         * or rewriting the spin code more effeciently as the web server was maxed on ram as is. After the user deletes
         * a module, they are return to the Module list screen to see there updated changes.
         */
        DeleteModButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                String ModNumberString = ModnumberText.getText().toString(); // Get the info of the module to send to 
                String LocationString = LocationEdit.getText().toString();   // so it knows which one to delete

                new EditDelModule().execute(                                 // Start the AsyncTask to delete the 
                    "http://" + Values.CurrentIP + "/delmod.htm?user="       // module
                    + ModNumberString +("%20") + LocationString + ("%0D"));
            
                Intent EditUsersIntent = new Intent(getApplicationContext(), HomeStatusActivity.class);
                startActivity(EditUsersIntent); // Send the user back to the module list screen on deletion
                finish();
            }
        });
        
        /*
         * This click listener will allow the user to save changes currently made to the module. The user can make 
         * these changes by editing the text and check box fields within this activity. The user is only able to 
         * change the Motion Sensor control, or the location. This button will get the current value fo these 
         * fields, then generate the apropriate command to send to the web server. After the command is sent the
         * user is sent back to the module list.
         */
        EditModButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                String MSControlledChanged;                                     // Get the current values of 
                String ModNumberString  = ModnumberText.getText().toString();   // the fields to generate the 
                String LocationString = LocationEdit.getText().toString();      // URI
                
                if(MScontrolled.isChecked()){   // Create a string representation of the check box
                    MSControlledChanged = "1";
                } else {
                    MSControlledChanged = "0";
                }
                
                new EditDelModule().execute("http://" + Values.CurrentIP // Send the generated command to the 
                    + "/editmod.htm?user=" + ModNumberString + ("%20")   // web server to process
                    + LocationString + (",") +  MSControlledChanged + ".0%200%200%200%0D");

                Intent EditUsersIntent = new Intent(getApplicationContext(), HomeStatusActivity.class);
                startActivity(EditUsersIntent); // Send the user back to the module list
                finish();
            }// onClick
        }); // btnWriteSDFile
    }
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////EditDelModule AsyncTask
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /* 
     * This private class is an asynch task that is used to communicate with the web server in a seperate thread.
     * this was made standard in the later Android OS versions, to prevent a bad conection from freezing up the 
     * the user interface. This Async task creates an HTTP connection to teh web server and sends the command
     * corresponding data through the internet.
     */
    private class EditDelModule extends AsyncTask<String, Void, Void> {
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