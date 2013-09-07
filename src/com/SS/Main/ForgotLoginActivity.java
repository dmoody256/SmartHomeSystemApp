package com.SS.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ForgotLoginActivity extends Activity{

	Button ForgotBackButton, ForgotHomeButton, ForgotUserButton;
	
	EditText EmailEdit;

	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotinfo);
        
        ForgotBackButton = (Button)findViewById(R.id.flloginback);
        ForgotHomeButton = (Button)findViewById(R.id.flloginhome);
        ForgotUserButton = (Button)findViewById(R.id.flsendlogin);
        
        
        EmailEdit = (EditText)findViewById(R.id.flemailedit);
        
        ForgotBackButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				Intent LoginIntent = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(LoginIntent);
				finish();
			}
        });

        ForgotHomeButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
						
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
				
			}
        });
	}
}
