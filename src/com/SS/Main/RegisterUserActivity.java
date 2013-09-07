package com.SS.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterUserActivity extends Activity{
	
	Button RegisterBackButton, RegisterHomeButton, RegisterUserButton, EditUserButton;
	
	EditText UsernameEdit, PassEdit, NameEdit;

	public SQLiteAdapter mySQLiteAdapter;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registeruser);
        
        mySQLiteAdapter = new SQLiteAdapter(this);
	    
        
        RegisterBackButton = (Button)findViewById(R.id.ruback);
        RegisterHomeButton = (Button)findViewById(R.id.ruhome);
        RegisterUserButton = (Button)findViewById(R.id.rusubmit);
        EditUserButton = (Button)findViewById(R.id.rueditusers);
        
        UsernameEdit = (EditText)findViewById(R.id.ruusernameedit);
        PassEdit = (EditText)findViewById(R.id.rupassedit);
        NameEdit = (EditText)findViewById(R.id.ruemailedit);
        
        RegisterBackButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				Intent LoginIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(LoginIntent);
				finish();
			}
        });

    	RegisterHomeButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
						
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
				
			}
        });
    	
    	EditUserButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
						
				Intent EditUserIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
				startActivity(EditUserIntent);
				finish();
				
			}
        });
    	
    	RegisterUserButton.setOnClickListener(new View.OnClickListener() {

    		public void onClick(View v) {
    			// write on SD card file data in the text box
    			
    			String UsernameString = UsernameEdit.getText().toString();
    			String PassString = PassEdit.getText().toString();
    			String NameString = NameEdit.getText().toString();
    			
    			
    			if(UsernameString.length() < 1)
    			{
    				Toast.makeText(getBaseContext(),
    						"Must enter a username",
    						Toast.LENGTH_SHORT).show();
    			}
    			else if(PassString.length() < 1)
    			{
    				Toast.makeText(getBaseContext(),
    						"Must enter a password",
    						Toast.LENGTH_SHORT).show();
    			}
    			else if(NameString.length() < 1)
    			{
    				Toast.makeText(getBaseContext(),
    						"Must enter a name",
    						Toast.LENGTH_SHORT).show();
    			}
    			else
    			{
	    	        mySQLiteAdapter.openToRead();
	    	        String[] usernames = mySQLiteAdapter.getUsernames();
	    	        int DBcount = mySQLiteAdapter.count();
	    	        mySQLiteAdapter.close();
	    	        
	    	        boolean userfound = false;
	    		   
	    	        for(int i = 0; i < usernames.length; i++){
	    		    	if(UsernameString.compareTo(usernames[i]) == 0){
	    		    		userfound = true;
	    		    	}
	    		    }
	    		    
	    		    if(userfound == true){
	    		    	Toast.makeText(getBaseContext(),
	    		    			"User already exist",
	    		    			Toast.LENGTH_SHORT).show();
	    		    }
	    		    else if(DBcount > 10){
	    		    	Toast.makeText(getBaseContext(),
	    						"Too many Users, Delete some",
	    						Toast.LENGTH_SHORT).show();
	    		    }
	    		    else{
	    		    	new RegisterUser().execute("http://" + Values.CurrentIP + "/reguser.htm?user=" + UsernameString +("%20") + PassString + ("%20")+ NameString + "%0D");
	    		    	
	    		    	mySQLiteAdapter.openToWrite();
	    		    	mySQLiteAdapter.UserInsert(UsernameString, PassString, NameString);
	    		    	mySQLiteAdapter.close();
	    		    	
	    		    	Intent LoginIntent= new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
	    				startActivity(LoginIntent);
	    				finish();
	    		    }
	    		    
	    		    
	    		    
    			}
    			
    			
    		}// onClick
    		}); // btnWriteSDFile
  
	}
	
	private class RegisterUser extends AsyncTask<String, Void, Void> {
	     
		protected Void doInBackground(String... URItoSend) {
			 int count = URItoSend.length;
			 for (int i = 0; i < count; i++) {
				 BufferedReader in = null;
		        	
		        	try {
		                HttpClient client = new DefaultHttpClient();
		                HttpGet request = new HttpGet();
		                
		                //HttpProtocolParams.setUseExpectContinue(client.getParams(), false);
	
		                request.setURI(new URI(URItoSend[i]));
	
		                HttpResponse response = client.execute(request);
		          
		                in = new BufferedReader
		                (new InputStreamReader(response.getEntity().getContent()));
		                
		                StringBuffer sb = new StringBuffer("");
		                String line = "";
		                String NL = System.getProperty("line.separator");
		                
		                while ((line = in.readLine()) != null) {
		                    sb.append(line + NL);
		                }
		                in.close();
		                
		                String page = sb.toString();
		                System.out.println(page);
		                
		                
		                } catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
		                if (in != null) {
		                    try {
		                        in.close();
		                        } catch (IOException e) {
		                        e.printStackTrace();
		                    }
		                }
		            }
		        }
			return null;
		 }

		 protected void onProgressUpdate(Integer... progress) {
			 
	     }

	     protected void onPostExecute(String result) {

	       
	     }

		
		
	 }

}
