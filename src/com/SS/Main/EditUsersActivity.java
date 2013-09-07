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
 * This activity gets the users from the database and displays them on a list
 * for the user to select. Upon selection the user is then can then modify 
 * the user from another activity.
 * 
 */

public class EditUsersActivity extends Activity {

	ListView UserList;
	
	Button EditUserBackButton, EditUserHomeButton;
	
	SQLiteAdapter mySQLiteAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editusers);
        
        EditUserBackButton = (Button)findViewById(R.id.euback);
        EditUserHomeButton = (Button)findViewById(R.id.euhome);
        
        UserList = (ListView)findViewById(R.id.euuserlist);
        
        mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToRead();
        
        
        
        Cursor cursor = mySQLiteAdapter.queueAll();
        startManagingCursor(cursor);

        String[] from = new String[]{SQLiteAdapter.KEY_USER};
        
        final String[] usernames = mySQLiteAdapter.getUsernames();
        
       
        
        int[] to = new int[]{android.R.id.text1};

        SimpleCursorAdapter cursorAdapter =
         new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, from, to);

        UserList.setAdapter(cursorAdapter);
      
        mySQLiteAdapter.close();
        
        new UpdateUsers().execute("http://" + Values.CurrentIP + "/USER.txt");
        
        EditUserBackButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				Intent RegisterIntent = new Intent(getApplicationContext(), RegisterUserActivity.class);
				startActivity(RegisterIntent);
				finish();
			}
        });

    	EditUserHomeButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
						
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
				
			}
        });
    	
    	UserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    			
    			Values.UserToBeEdited = usernames[position];
    			Intent EditAUserIntent = new Intent(getApplicationContext(), EditAUserActivity.class);
				startActivity(EditAUserIntent);
				finish();
    			
    		}
    	});


	}
	
	private class UpdateUsers extends AsyncTask<String, String, String> {

		
		
		protected String doInBackground(String... URItoSend) {
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
		                if(in == null)
		                {
		                	Values.DBconnection = false;
		                	return "Database Unavialable";
		                }
		                String line = "";
		                String NL = System.getProperty("line.separator");
		                
		                
		                while ((line = in.readLine()) != null) {
		                    
		                	StringBuilder builder = new StringBuilder();
		                	for(int k = 0; k < line.length(); k++)
		                    {
		                		
		                    	if(line.charAt(k) < 128 && line.charAt(k) > 0)
		                    	{
		                    		
		                    		builder.append(line.charAt(k));
		                    	}
		                    }
		                	
		                	
		                	
		                	if(builder.length() != 0)
		                		sb.append(builder + NL);
		                	
		                }
		                in.close();
		                System.out.println(sb);
		                String page = sb.toString();
		                
		                Values.DatabaseStrings = page.split("\n");
		                
		                Values.DBconnection = true;
		                mySQLiteAdapter.openToWrite();
            		    
        	    		mySQLiteAdapter.deleteAllUsers();
        	    		
        	    		
        	    		
            		    for(int m = 1; m < Values.DatabaseStrings.length; m++)
            		    {
            		    	
            		    	String[] User = Values.DatabaseStrings[m].split(" ");
            		    	String DBUsername = User[0];
            		    	String DBPass = User[1];
            		    	String DBName = User[2];
            		    	
            		    	
            		    	mySQLiteAdapter.UserInsert(DBUsername, DBPass, DBName);
            		    	}
            		    

            		    
            		    mySQLiteAdapter.close();
 
	        	    
		                
		               
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
			Values.DBconnection = false;
			return "Database Unavialable";
		 }

		 protected void onProgressUpdate(Integer... progress) {
			 
	     }

	     protected void onPostExecute() {
	    	 
	    	 	mySQLiteAdapter.openToRead();
	      	   
	 	        
	 	        Cursor cursor = mySQLiteAdapter.queueAll();
	 	        
	 	        
	 	        SimpleCursorAdapter adapter = (SimpleCursorAdapter)UserList.getAdapter();
	 	        adapter.changeCursor(cursor);
	 	        adapter.notifyDataSetChanged();
			
			    mySQLiteAdapter.close();
	    	 
	     }

	 }
	
}
