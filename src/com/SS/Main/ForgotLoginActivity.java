package com.SS.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *
 * @author Daniel Moody
 * Last updated 12/18/2013
 * 
 * This activity was never completed. As of now it stands as a place holder for future work. The point
 * of this activity is to allow the user to send there password in an email in case the forgot it. 
 * The user will enter the email address of an already registered account, then the system should
 * check to make sure that email is a user, retirve the password for that user and then send it to
 * that email.
 *
 */
public class ForgotLoginActivity extends Activity{

    Button ForgotBackButton; // Button to go back to previous screen
    Button ForgotHomeButton; // Button to go to home screen
    Button ForgotUserButton; // Button to send email with password
    EditText EmailEdit;      // Email that is registered already
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Activity Initialization
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Create the instance of this activity
        setContentView(R.layout.forgotinfo); // connect the activity to the GUI layout
        
        ForgotBackButton = (Button)findViewById(R.id.flloginback); // Connect the Activity
        ForgotHomeButton = (Button)findViewById(R.id.flloginhome); // objects to the GUI
        ForgotUserButton = (Button)findViewById(R.id.flsendlogin); // objects
        EmailEdit = (EditText)findViewById(R.id.flemailedit);
        
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////Activity Buttons
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /*
         * This button is used to go back to the previous screen, which in the case will usually be the login screen.
         * This function sets the click listener that will execute each time the button is clicked. In this case it 
         * create an intent to start the new activty.
         */
        ForgotBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                Intent LoginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(LoginIntent); // send the user to the previous screen
                finish();
            }
        });
        
        /*
         * This button is used to go back to the home screen. This function sets the click listener that will execute each 
         * time the button is clicked. In this case it create an intent to start the new activty to the Home Screen.
         */
        ForgotHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        
                Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
                startActivity(SmartHomeSystemIntent); // send the user to the home screen
                finish();
            }
        });
    }
}