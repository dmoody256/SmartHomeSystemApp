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

public class EditAModuleActivity extends Activity{
	
	Button EditAModBackButton, EditAModHomeButton, DeleteModButton, EditModButton;
	
	CheckBox MScontrolled;
	
	EditText LocationEdit;
	TextView ModnumberText;
	public SQLiteAdapter mySQLiteAdapter;

	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editamod);
        
        mySQLiteAdapter = new SQLiteAdapter(this);
        
        
        MScontrolled = (CheckBox)findViewById(R.id.eammscheck);
        
        EditAModBackButton = (Button)findViewById(R.id.eamback);
        EditAModHomeButton = (Button)findViewById(R.id.eamhome);
        DeleteModButton = (Button)findViewById(R.id.eamdelete);
        EditModButton = (Button)findViewById(R.id.eameditusers);
        
        ModnumberText = (TextView)findViewById(R.id.eammodnumedit);
        LocationEdit = (EditText)findViewById(R.id.eamlocationedit);
        
        
        
        mySQLiteAdapter.openToRead();
        
        LocationEdit.setText(mySQLiteAdapter.getLocation(Values.ModToBeEdited));
        ModnumberText.setText(Values.ModToBeEdited);
        String MSControlledValue = mySQLiteAdapter.getMStimed(Values.ModToBeEdited);
        if(MSControlledValue.contentEquals("1"))
        {
        	 MScontrolled.setChecked(true);
        	 
        }
        else
        {
        	 MScontrolled.setChecked(false);
        }
        
        
        
        mySQLiteAdapter.close();
        
        if(Integer.parseInt(Values.NextModNumber)-1 != Integer.parseInt(Values.ModToBeEdited))
        {
        	DeleteModButton.setEnabled(false);
        }
        else
        {
        	DeleteModButton.setEnabled(true);
        }
        
        
        EditAModBackButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				Intent LoginIntent = new Intent(getApplicationContext(), HomeStatusActivity.class);
				startActivity(LoginIntent);
				finish();
			}
        });

        EditAModHomeButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
						
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
				
			}
        });
    	
        DeleteModButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				String ModNumberString = ModnumberText.getText().toString();
    			String LocationString = LocationEdit.getText().toString();
    			
				
    			new EditDelModule().execute("http://" + Values.CurrentIP + "/delmod.htm?user=" + ModNumberString +("%20") + LocationString + ("%0D"));
    		
				
				Intent EditUsersIntent = new Intent(getApplicationContext(), HomeStatusActivity.class);
				startActivity(EditUsersIntent);
				finish();
				
			}
        });
    	
    	EditModButton.setOnClickListener(new View.OnClickListener() {

    		public void onClick(View v) {
    			// write on SD card file data in the text box
    			String MSControlledChanged;
    			String ModNumberString  = ModnumberText.getText().toString();
    			String LocationString = LocationEdit.getText().toString();
    			if(MScontrolled.isChecked())
    			{
    			   MSControlledChanged = "1";
    			}
    			else
    			{
    				MSControlledChanged = "0";
    			}
    			
    			new EditDelModule().execute("http://" + Values.CurrentIP + "/editmod.htm?user=" + ModNumberString + ("%20")+ LocationString + (",") +  MSControlledChanged + ".0%200%200%200%0D");

    		    Intent EditUsersIntent = new Intent(getApplicationContext(), HomeStatusActivity.class);
				startActivity(EditUsersIntent);
				finish();
    		    
    		}// onClick
		}); // btnWriteSDFile
		
    
	}
	private class EditDelModule extends AsyncTask<String, Void, Void> {
	     
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
