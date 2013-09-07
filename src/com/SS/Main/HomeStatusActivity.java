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
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Daniel Moody
 * Last updated 11/28/2012
 * 
 * This .java file is used for creation and function or the Home Status Activity in an
 * Android Application created for a Senior Design Project at UCF in Fall Semester 2012.
 * It give the use the ability to control power outler modules, by switching their state
 * from on to off and also takes in sensor data from the system. The Application is used 
 * for User Interface with this system. 
 *
 */

public class HomeStatusActivity extends Activity{
	
	/*
	 * Declare objects that will be used by this Android Activity
	 */
	
	SQLiteAdapter mySQLiteAdapter;
	Button HomeStatusBackButton, HomeStatusHomeButton, UpdateStatusButton, AddModButton;
	ListView ModuleList;
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homestatus);
        
        /*
         * After the instance of the Activity is created, the view objects
         * used by the activity need to be assigned to their respective xml
         * objects.
         */
        
       
        mySQLiteAdapter = new SQLiteAdapter(this); //create a SQLite adapter for the instance of this activity

        ModuleList = (ListView)findViewById(R.id.hsmodlist);
        
        UpdateStatusButton = (Button)findViewById(R.id.hsgetstatus);
        UpdateStatusButton.setEnabled(false); //The update status button will be disabled at first until the initial update is complete
        
        AddModButton = (Button)findViewById(R.id.hsaddmod);
        AddModButton.setEnabled(false);//The update status button will be disabled at first until the initial update is complete
        
        HomeStatusBackButton = (Button)findViewById(R.id.homestatusback);
        HomeStatusHomeButton = (Button)findViewById(R.id.homestatushome);

        /*
         * This if-else statement checks to see if the current user is the master user. 
         * The master user will have access to extra features like adding modules to the system.
         */
        if(Values.MasterUser == true){
        	AddModButton.setVisibility(0);
        }
        else{
        	AddModButton.setVisibility(4);
        }
        /*
         * We need to create a list view for the user to see the modules. The initial list may
         * not be up to date, so until the first update is complete the activity will present
         * what ever data is in the DB from the last update
         */
        mySQLiteAdapter.openToRead();
        
        //Here the cursor adapter is being prepared and the data is retrieved from the DB
        Cursor cursor = mySQLiteAdapter.queueAllMod();
        startManagingCursor(cursor);
        String[] from = new String[]{SQLiteAdapter.KEY_MODNUM, SQLiteAdapter.KEY_LOCATION, SQLiteAdapter.KEY_MODRELAY, SQLiteAdapter.KEY_MODMS, SQLiteAdapter.KEY_MODSS};
        int[] to = new int[]{R.id.modlistmodnumber, R.id.modlistlocation, R.id.modlistrelay, R.id.modlistms, R.id.modlistss};
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.modlist, cursor, from, to);
        
        Values.ModNumbers = mySQLiteAdapter.getModNumbers();//store the module numbers for later use
        
        ModuleList.setAdapter(cursorAdapter);
	    
        mySQLiteAdapter.close();
        
	    /*
	     * After the module list is setup, and AsyncTask update will start to retrieve the current system status.
	     * The AsyncTask is created as a private subclass in this class. It will take in a String which
	     * is a URL to the web server that is monitoring the system. The URL is constructed and then passed the
	     * subclass. CurrentIP is the IP address that is received from NO-IP.com, a DDNS that gathers the current 
	     * IP address of the Web Server from the first activity created when the application is launched. The web server 
	     * is actually a 80MHz low cost micro-controller and can only process simple HTTP commands like GET and POST. 
	     * Here UpdateModules class is passed 2 URLS, the first one gets a status update, the second gets a text file
	     * that contains all the module information.
	     */
       
	    new UpdateModules().execute("http://" + Values.CurrentIP + "/aled.htm?led=statusupdate", "http://" + Values.CurrentIP + "/MODULE.txt");

		
	    UpdateStatusButton.setOnClickListener(new View.OnClickListener() {
            
			public void onClick(View v) {
				
				/*
				 * When an update is started the update and add module buttons need to be disable to prevent the user from 
				 * sending to many update request and slowing down the system and possible causing errors. They will be 
				 * re-enabled in UpdateModules onPostExecute(). 
				 */
				
				UpdateStatusButton.setEnabled(false); 
				AddModButton.setEnabled(false);
				new UpdateModules().execute("http://" + Values.CurrentIP + "/aled.htm?led=statusupdate", "http://" + Values.CurrentIP + "/MODULE.txt");
			}
        });
		        
		ModuleList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    			
    			/*
    			 * if the user is a master user then they will be able to edit modules properties by
    			 * clicking and holding the module in the list. if not then the long click listener will not work
    			 */
    			
    			if(Values.MasterUser == true){
	    			Values.ModToBeEdited = Integer.toString(position+1);
	    			Intent EditAUserIntent = new Intent(getApplicationContext(), EditAModuleActivity.class);
					startActivity(EditAUserIntent);
					finish();
				}
    			return false;
    		}
    	});
    	
    	HomeStatusBackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				/*
				 * This button simply returns the user to the previous screen, in this case it happen to be the
				 * home screen so this button will have the same functionality as HomeStatusButton
				 */
				
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
		    	
			}
        });
        
    	HomeStatusHomeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				/*
				 * This button simply returns the user to the Home Screen, where they can then logout
				 * change user information, or return to the module list.
				 */
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
		    	
		    	
			}
        });
    	
    	ModuleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    			
    			/*
    			 * Another AsyncTask subclass that is used for sending commands to turn lights off and on. It takes the 
    			 * current position that is clicked when the user clicks a button from the list.
    			 */
    			AddModButton.setEnabled(false);
				 UpdateStatusButton.setEnabled(false);
        		new CommandsTask().execute("http://" + Values.CurrentIP + "/aled.htm?led=" + Integer.toString(position+1));
        		
    		} 	
    	});
    	
    	AddModButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				/*
    			 * adding a module will disable the update and add module buttons to prevent the user from sending to many request
    			 * to the web server and causing problems. The AddModuleTask will run the update task when it is finish telling the web server
    			 * to add a new module. It will also check to make sure that there are less then 10 modules, as the web server will began to slow down
    			 * if there are 10 or modules in the system. The URL that is created is a default module string used in the web server text files.
    			 * NextModNumber is +1 of the current modules in the system and checks the current number of modules. 
    			 */
				
				if(Integer.parseInt(Values.NextModNumber) < 10){
					AddModButton.setEnabled(false);
					UpdateStatusButton.setEnabled(false);
					ModuleList.setLongClickable(false);
					new CommandsTask().execute("http://" + Values.CurrentIP + "/addmod.htm?user=" + Values.NextModNumber + "%20Unnamed,0.0%200%200%200%0D");
				}
				else{
					Toast.makeText(getBaseContext(),
        					"Max Modules reached",
        					Toast.LENGTH_SHORT).show();
				}
			}
        });

	}//End of Activity Class

	private class CommandsTask extends AsyncTask<String, Void, String> {
	     
		/*
		 *This class uses apache libraries to send an HTTP GET request to the web server.
		 *It also receives a response from the web server, but does not use the response.
		 *I plan to add some error handling by making sure that a correct response is received
		 *because the web server will echo get request.
		 */
		
		protected String doInBackground(String... URItoSend) {
			 
			/*
			 * The connection is created and received in a background thread to keep the UI from being slowed down while
			 * the connection is made.
			 */
			int count = URItoSend.length;
			for (int i = 0; i < count; i++) {
				
				BufferedReader in = null;
	        	
				try {
	                HttpClient client = new DefaultHttpClient();
	                HttpGet request = new HttpGet();
            
	                request.setURI(new URI(URItoSend[i]));

	                HttpResponse response = client.execute(request);
	          
	                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	                
	                StringBuffer sb = new StringBuffer("");
	                String line = "";
	                String NL = System.getProperty("line.separator");
	                
	                while ((line = in.readLine()) != null) {
	                    sb.append(line + NL);
	                }
	                
	                in.close();

				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally{
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

	    protected void onPostExecute(String result) {
	    	
	    	/*
	    	 * After the command is sent,it will automatically update, to keep the system current.
	    	 * however the amount of updates needs to be limited, so a boolean is used to disable the update if there is already an update
	    	 * processes. 
	    	 */
	    
	    	 if(Values.clickdisable == true){
	    		 
	    		 new UpdateModules().execute("http://" + Values.CurrentIP + "/aled.htm?led=statusupdate", "http://" + Values.CurrentIP + "/MODULE.txt"); 
	    	 }
	    	 else{
	    		 
	    	 }
	    	 Values.clickdisable = false;
	    }
	}//End of CommandsTask Class
	
	
	private class UpdateModules extends AsyncTask<String, String, Cursor> {
			
		/*
		 * this class updates the modules by sending a status update request, which tells the web server to get the modules text file
		 * ready to send an update. Then it request the modules text file from the web sever. It is hard coded to receive 2 URLs; always
		 * the status update first, then the module text file request 2nd.
		 */
		
		protected Cursor doInBackground(String... URItoSend) {
			
			/*
			 * The do in background portion use standard apache http get request to communicate with the web server.
			 * For the status update the request is sent normally and then a response is receive, but I have not set up any
			 * any use for it yet, although it can be used for error handling, to make sure the web server is communicating correctly.
			 * The text file received from the web server is handled slightly differently and is described in more detail below.
			 */
			
			int count = URItoSend.length;
			for (int i = 0; i < count; i++) {
				BufferedReader in = null;
	        	try {
	        		
	        		
	                HttpClient client = new DefaultHttpClient();
	                HttpGet request = new HttpGet();

	                request.setURI(new URI(URItoSend[i]));

	                HttpResponse response = client.execute(request);
	          
	                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

	                StringBuffer sb = new StringBuffer("");
	                String line = "";
	                String NL = System.getProperty("line.separator");
	                
	                if(i == 0){
		                while ((line = in.readLine()) != null) {
		                    sb.append(line + NL);
		                }
		                
		                in.close();

	                }
		                
	                //this is the case where the get request is for the web server text file
	                if(i == 1){
	                	/*
	                	 * When receiving the text file, it is received as a long string and then parsed according to
	                	 * certain characters. At first the string read one line at a time. For some reason, I am not
	                	 * sure why, but the web server was sending the text files with random bytes in between the actual
	                	 * characters. My quick and dirty solution to this was to parse out only the characters that were 
	                	 * correct.
	                	 */
	                	
		                while ((line = in.readLine()) != null) {
		                    
		                	StringBuilder builder = new StringBuilder();
		                	//Here the for loop parses the response looking for characters that are actually 
		                	//ASCII characters and not random bytes
		                	for(int k = 0; k < line.length(); k++){
		                		
		                    	if(line.charAt(k) < 128 && line.charAt(k) > 0){
		                    		
		                    		builder.append(line.charAt(k));
		                    	}
		                    }
		                	
		                	if(builder.length() != 0)
		                		sb.append(builder + NL);
		                	
		                }
		                
		                in.close();
		                
		                String page = sb.toString();
		                
		                Values.DatabaseStrings = page.split("\n");
		               
		                
		                /*
		                 * after the String has been parsed and the correct characters are in place, the Strings need to be 
		                 * parsed and the correct data is then entered into the database.
		                 */
		                try{
		                	
		     	    		String DBModNumber, DBModLocation, DBModRelayStatus, DBModManualStatus, DBModMSStatus, DBModSSStatus, DBModMSTimed;
		     		    	String[] Mod;
		 		    	
		     	    		mySQLiteAdapter.openToWrite();
		         		    
		     	    		mySQLiteAdapter.deleteAllMods();//The Mods table is erased to allow a up to date data to be entered
		     	    		
		     	    		Values.NextModNumber = Integer.toString(Values.DatabaseStrings.length);
		     	    		
		     	    		//for each string parse the corresponding values
	            		    for(int j = 1; j < Values.DatabaseStrings.length; j++){
	            		    	
	            		    	Values.DatabaseStrings[j] = Values.DatabaseStrings[j].replace('.', ' ');
	            		    	Values.DatabaseStrings[j] = Values.DatabaseStrings[j].replace(',', ' ');
	            		    	
	            		    	Mod = Values.DatabaseStrings[j].split(" ");
	            		    	DBModNumber = Mod[0];
	            		    	DBModLocation = Mod[1];
	            		    	DBModMSTimed = Mod[2];
	            		    	DBModRelayStatus = Mod[3];
	            		    	DBModManualStatus = Mod[4];
	            		    	DBModMSStatus = Mod[5];
	            		    	DBModSSStatus = Mod[6];
	            		    	
	            		    	//the web server uses 1 and 0s to store the data, here I convert the binary data to it actual meaning
	            		    	if(DBModRelayStatus.contentEquals("1"))
	            		    		DBModRelayStatus = "Power\nON";
	            		    	else
	            		    		DBModRelayStatus = "Power\nOFF";		            		    	
	            		    	
	            		    	if(DBModMSStatus.contentEquals("1"))   		    
	            		    		DBModMSStatus = "Motion\nDETECTED";		            		    	
	            		    	else
	            		    		DBModMSStatus = "Motion\nUNDETECTED";
	            		    	
	            		    	if(DBModSSStatus.contentEquals("1"))		            		    	
	            		    		DBModSSStatus = "Door/Window\nOPEN";		            		    	
	            		    	else		            		    	
	            		    		DBModSSStatus = "Door/Window\nCLOSED";
	            		    			            		    	
	            		    	mySQLiteAdapter.InsertMod(DBModNumber, DBModLocation, DBModMSTimed, DBModRelayStatus, DBModManualStatus, DBModMSStatus, DBModSSStatus);		            		    
	            		    }

	            		    mySQLiteAdapter.close();
		
		        		}
	     	    	
		     	    	catch(Exception e){
		     	    		e.printStackTrace();
		     	    	}
	                }
               

		                
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


	    protected void onPostExecute(Cursor result) {
	    	 /*
	    	  * now that the module DB has been updated, the List View needs to be updated and the buttons that are 
	    	  * disabled from updating need to be re-enabled. The Adapter is reset and then the view is notified that 
	    	  * its data has been changed and it updates itself.
	    	  */
	    	UpdateStatusButton.setEnabled(true);
	    	AddModButton.setEnabled(true);
	    	ModuleList.setLongClickable(true);
	    	Values.clickdisable = true;
	    	mySQLiteAdapter.openToRead();
      	   
 	        Cursor cursor = mySQLiteAdapter.queueAllMod();      
 	        
 	        SimpleCursorAdapter adapter = (SimpleCursorAdapter) ModuleList.getAdapter();
 	        adapter.changeCursor(cursor);
 	        adapter.notifyDataSetChanged();
		
		    mySQLiteAdapter.close();

	     }

	}//End of Update Modules Task

}


