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
	Button  LoginButton, HomeStatusButton, LogoutButton, UserAdminButton;
	SQLiteAdapter mySQLiteAdapter;
	TextView LoginText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
         * After the instance of the Activity is created, the view objects
         * used by the activity need to be assigned to their respective xml
         * objects.
         */
        
        LoginButton = (Button)findViewById(R.id.mainloginbutton);
        HomeStatusButton = (Button)findViewById(R.id.mainhomestatus);
        LogoutButton = (Button)findViewById(R.id.mainlogout);
        UserAdminButton = (Button)findViewById(R.id.mainuseradmin);
        LoginText = (TextView)findViewById(R.id.mainlogintext);
        
        //create the SQLite Adapter for the instance of this activity
        mySQLiteAdapter = new SQLiteAdapter(this);
        
        
        if(Values.LoggedIn == false){
        	HomeStatusButton.setVisibility(4);
        	LogoutButton.setVisibility(4);
        	UserAdminButton.setVisibility(4);
        	LoginButton.setVisibility(0);
        	LoginText.setText("You are currently not loggin in.");
        	
        	new GetIP().execute("http://dmoody256.servebeer.com");
        	
        }
        else{
        	LoginText.setText("You are currently logged in as " + Values.LoggedinName + ".");
        	HomeStatusButton.setVisibility(0);
        	LogoutButton.setVisibility(0);
        	if(Values.MasterUser == true){
        		UserAdminButton.setVisibility(0);
        	}
        	else{
        		UserAdminButton.setVisibility(4);
        	}
        	LoginButton.setVisibility(4);
        }
    	
        LogoutButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
					
					Values.LoggedIn = false;
					Values.LoggedinName = null;
					Values.LoggedinPass = null;
					Values.LoggedinUN = null;
					
					Values.MasterUser = false;
				
		    		Intent LoginIntent= new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
					startActivity(LoginIntent);
					finish();

			}
        });
        
        UserAdminButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {

				Intent AddUserIntent= new Intent(getApplicationContext(), RegisterUserActivity.class);
				startActivity(AddUserIntent);
				finish();

			}
        });
    	
		 HomeStatusButton.setOnClickListener(new View.OnClickListener() {
		            
					public void onClick(View v) {
		
				    		Intent LoginIntent= new Intent(getApplicationContext(), HomeStatusActivity.class);
							startActivity(LoginIntent);
							finish();
		
					}
		        });
    
   
        LoginButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
					
					
				
		    		Intent LoginIntent= new Intent(getApplicationContext(), LoginActivity.class);
					startActivity(LoginIntent);
					finish();

			}
        });
    
       
        
    }
    
    private class GetIP extends AsyncTask<String, Void, String> {
	     
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
		                String line = "";
		                String IPString = null;
		                String IP = "fail";
		                String NL = System.getProperty("line.separator");
		                
		                while (!(line = in.readLine()).contentEquals("<meta name=\"description\" content=\"\">"))
		                {
		                	System.out.println(line);
		                }
		                
		                IPString = in.readLine();
		                String[] IPStringSplit = IPString.split("/");
		                for(int j = 0; j < IPStringSplit.length;j++)
		                {
		                	if(j == 2)
		                	{
		                		IP = IPStringSplit[j].toString();
		                	}
		                }
		                
		                
		               
		                
		                
		                in.close();
		                
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

	         new UpdateUsers().execute("http://" + Values.CurrentIP + "/USER.txt");
  
	     }

		
		
	 }
    
    private class UpdateUsers extends AsyncTask<String, Void, String> {
	
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
		                
		                
		                return page;
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
	    	 
	    	 
	    	 
	     }

	 }
    
    
}