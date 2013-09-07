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

public class EditAUserActivity extends Activity{
	
	Button EditAUserBackButton, EditAUserHomeButton, DeleteUserButton, EditInfoButton;
	
	EditText PassEdit, EmailEdit;
	TextView UsernameEdit;
	public SQLiteAdapter mySQLiteAdapter;

	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editauser);
        
        mySQLiteAdapter = new SQLiteAdapter(this);
        
        
	    
        
        EditAUserBackButton = (Button)findViewById(R.id.eauback);
        EditAUserHomeButton = (Button)findViewById(R.id.eauhome);
        DeleteUserButton = (Button)findViewById(R.id.eaudelete);
        EditInfoButton = (Button)findViewById(R.id.eaueditusers);
        
        UsernameEdit = (TextView)findViewById(R.id.eauusernameedit);
        PassEdit = (EditText)findViewById(R.id.eaupassedit);
        EmailEdit = (EditText)findViewById(R.id.eauemailedit);
        
        mySQLiteAdapter.openToRead();
        
        PassEdit.setText(mySQLiteAdapter.getPassword(Values.UserToBeEdited));
        EmailEdit.setText(mySQLiteAdapter.getEmail(Values.UserToBeEdited));
        UsernameEdit.setText(Values.UserToBeEdited);
        
        mySQLiteAdapter.close();
        
        EditAUserBackButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				Intent LoginIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
				startActivity(LoginIntent);
				finish();
			}
        });

        EditAUserHomeButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
						
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
				
			}
        });
    	
        DeleteUserButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				String UsernameString = UsernameEdit.getText().toString();
    			String PassString = PassEdit.getText().toString();
    			String EmailString = EmailEdit.getText().toString();
				
    			new EditDelUser().execute("http://" + Values.CurrentIP + "/deluser.htm?user=" + UsernameString +("%20") + PassString + ("%20")+ EmailString + ("%0D"));
    			
				mySQLiteAdapter.openToWrite();
				mySQLiteAdapter.deleteUser(Values.UserToBeEdited);
				
				mySQLiteAdapter.close();
				
				if(UsernameString.contentEquals(Values.LoggedinUN))
				{

					Values.LoggedIn = false;
					Values.LoggedinName = null;
					Values.LoggedinPass = null;
					Values.LoggedinUN = null;
					
					Values.MasterUser = false;
				
		    		Intent LoginIntent= new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
					startActivity(LoginIntent);
					finish();
				}
				else
				{
				
					Intent EditUsersIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
					startActivity(EditUsersIntent);
					finish();
				}
				
			}
        });
    	
    	EditInfoButton.setOnClickListener(new View.OnClickListener() {

    		public void onClick(View v) {
    			// write on SD card file data in the text box
    			
    			String UsernameString = UsernameEdit.getText().toString();
    			String PassString = PassEdit.getText().toString();
    			String EmailString = EmailEdit.getText().toString();
    			
    			if(PassString.length() < 1)
    			{
    				Toast.makeText(getBaseContext(),
    						"Must enter a password",
    						Toast.LENGTH_SHORT).show();
    			}
    			else if(EmailString.length() < 1)
    			{
    				Toast.makeText(getBaseContext(),
    						"Must enter a name",
    						Toast.LENGTH_SHORT).show();
    			}
    			
    			else
    			{
    				new EditDelUser().execute("http://" + Values.CurrentIP + "/edituser.htm?user=" + UsernameString +("%20") + PassString + ("%20")+ EmailString + ("%0D"));
	    			
	    	        mySQLiteAdapter.openToRead();
	    	        
	    	        
	    	        
	    	        String[] usernames = mySQLiteAdapter.getUsernames();
	    	        mySQLiteAdapter.close();
	    			
	    		    mySQLiteAdapter.openToWrite();
	 
	    		    mySQLiteAdapter.deleteUser(UsernameString);
	    		    mySQLiteAdapter.UserInsert(UsernameString, PassString, EmailString);
	    		    
	    		    
	    		    mySQLiteAdapter.close();
	    		    Intent EditUsersIntent = new Intent(getApplicationContext(), EditUsersActivity.class);
					startActivity(EditUsersIntent);
					finish();
    			}
    		}// onClick
		}); // btnWriteSDFile
		
    
	}
	private class EditDelUser extends AsyncTask<String, Void, Void> {
	     
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
