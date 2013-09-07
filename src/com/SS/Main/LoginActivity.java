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
import android.widget.Toast;

/**
 * This class is an activity that creates the log in screen for the user. It will 
 * allow the user to log in by typing in their username and password. 
 * 
 * <p>This class will check to see if it can get a connection to the web server 
 * database and then if a connection is successful it will download the user database 
 * from the web server so that the local database can be populated with the most
 * up to date data. This class uses AsyncTasks to make connections with the web
 * server.</p>
 *
 * <p>Written by Daniel Moody for Senior Design at University of Central Florida  - 11/15/2012</p>
 * 
 * @author Daniel Moody
 * @see AysncTask
 */
public class LoginActivity extends Activity{
	
	/*
	 * Declare the widgets for this activity
	 */
	Button LoginBackButton;
	Button LoginHomeButton; 
	Button ForgotLoginButton;
	Button LoginButton;
	EditText UsernameEdit;
	EditText PasswordEdit;
	
	//this Activity will require SQL operations
	public SQLiteAdapter mySQLiteAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        //first set the SQL adapter for later
        mySQLiteAdapter = new SQLiteAdapter(this);
        
        /*
         * This is a quick check to a global variable that 
         * records if the database at the web server was
         * successfully connected to. This would be set when the
         * user first enters the application. It will quickly
         * alert the user that the database is having problems.
         */
	    if(Values.DBconnection == false)
	    {
	    	Toast.makeText(getBaseContext(),
	    			"Database not Avialable",
					Toast.LENGTH_SHORT).show();
	    }
	    
        /*
         * now set our widgets to thier corresponding views
         */
        LoginBackButton = (Button)findViewById(R.id.logloginback);
        LoginHomeButton = (Button)findViewById(R.id.logloginhome);
        LoginButton = (Button)findViewById(R.id.logsubmit);
        UsernameEdit = (EditText)findViewById(R.id.logusernameedit);
        PasswordEdit = (EditText)findViewById(R.id.logpassedit);
        
        /*
         * the next to buttons are given click listeners that allow the 
         * user to navigate through the application. In this case they
         * bring the user to the home screen.
         */
        LoginBackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), 
						SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
			}
        });//end of LoginBackButton
        LoginHomeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent SmartHomeSystemIntent = new Intent(getApplicationContext(), 
						SmartHomeSystemActivity.class);
				startActivity(SmartHomeSystemIntent);
				finish();
			}
        });//end of LoginHomeButton
        
        /*
         * The login button will get the most recent user data from the 
         * web server database so that the application will be up to
         * date.
         */
        LoginButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		
        		/*
        		 * we will do a quick check to see if the database is has recently
        		 * had a successful connection
        		 */
        		if(Values.DBconnection == false )
            	{
        			/*
        			 * if the database had problems connecting last time
        			 * alert the user then try to connect again
        			 */
        			Toast.makeText(getBaseContext(),
        					"Database not Avialable, Please Try again",
        					Toast.LENGTH_SHORT).show();
        			
        			//this executes a separate thread to connect to the domain name
        			new GetIP().execute("http://dmoody256.servebeer.com");
            	}
        		else
        		{
        			/*
        			 * the database was successfully connected to recent and the
        			 * web server IP address was successfully obtained now we will 
        			 * get the user data stored in a text file on the spinneret web
        			 * server.
        			 */
        			new UpdateUsers().execute("http://" + Values.CurrentIP + "/USER.txt");

        			/*
        			 * this checks the most recent global variable for to see how many 
        			 * users were gathered from the spinneret web server. if there
        			 * was only one string then that means that there are no currently
        			 * registered users.
        			 */
        	    	if(Values.DatabaseStrings.length == 1)
    	    		{
    	    			/*
    	    			 * we need to alert the user that there are no
    	    			 * users and a master user needs to be created
    	    			 */
        	    		Toast.makeText(getBaseContext(),
            					"Create Master User!!!",
            					Toast.LENGTH_SHORT).show();

    	    			//go to the register user screen to register the first user
    	    			Intent HomeStatusIntent = new Intent(getApplicationContext(), 
    	    					RegisterUserActivity.class);
						startActivity(HomeStatusIntent);
						finish();
    	    			
    	    		}
        	    	else
        	    	{
        	    		/*
        	    		 * users have been found and now we need to write them to
        	    		 * the local database
        	    		 */
	        	    	if(Values.DBconnection == true)
	        	    	{
	        	    		//declare the variables that we are going to use
	        	    		String DBUsername;
	        		    	String DBPass;
	        		    	String DBName;
	        		    	String[] User;
	        		    	
	        		    	/*
	        		    	 * the database needs to be as close to web server 
	        		    	 * database as possible, first we open the database for 
	        		    	 * and then we need to clear the database users, since we 
	        		    	 * are about to load the current database into the local
	        		    	 */
	        	    		mySQLiteAdapter.openToWrite();
	        	    		mySQLiteAdapter.deleteAllUsers();
	 
	        	    		/*
	        	    		 * the users are stored in string on the web server database and 
	        	    		 * can be parsed using spaces. Then the data is put into separate 
	        	    		 * variables so that it can be inserted into the database
	        	    		 */
	            		    for(int i = 1; i < Values.DatabaseStrings.length; i++)
	            		    {
	            		    	User = Values.DatabaseStrings[i].split(" ");
	            		    	DBUsername = User[0];
	            		    	DBPass = User[1];
	            		    	DBName = User[2];
	            		    	
	            		    	mySQLiteAdapter.UserInsert(DBUsername, DBPass, DBName);
	            		    }
                            
	            		    /*
	            		     * now that we are finished updating the data base we want to check and
	            		     * and see if the user that is trying to log in is a user in the database 
	            		     * was able to provide the correct user information. we close the database
	            		     * because it was open for writing and now we want to open it to read from 
	            		     * it.
	            		     */
	            		    mySQLiteAdapter.close();
		        	    	mySQLiteAdapter.openToRead();
		        	        
		        	    	/*
		        	    	 * we simply grab all the information from the database and 
		        	    	 * put them into there own arrays to be searched through. Then we close
		        	    	 * the database.
		        	    	 */
		        	        String[] usernames = mySQLiteAdapter.getUsernames();
		        	        String[] passwords = mySQLiteAdapter.getPasswords();
		        	        String[] names = mySQLiteAdapter.getNames();
		        	        mySQLiteAdapter.close();
		        	        
		        	        //this variable will is a flag that represents when the user has been found
		        	        boolean userfound = false;
		        		    
		        	        /*
		        	         * search through the arrays and compare them to the current user log in 
		        	         * information
		        	         */
		        	        for(int i = 0; i < usernames.length; i++)
		        		    {
		        	        	//this if will check if there is a match on the current iteration
		        		    	if(UsernameEdit.getText().toString().compareTo(usernames[i]) == 0
		        		    			&& PasswordEdit.getText().toString().compareTo(passwords[i]) == 0)
		        		    	{
		        		    		/*
		        		    		 * a match has been found, set the flag, and also set the global
		        		    		 * variables so they can be called quickly later
		        		    		 */
		        		    		userfound = true;
		        		    		Values.LoggedIn = true;
			        		    	Values.LoggedinUN = UsernameEdit.getText().toString();
			        		    	Values.LoggedinPass = PasswordEdit.getText().toString();
			        		    	Values.LoggedinName = names[i];
			        		    	
			        		    	//this will set a global flag to mark if there are currently no users in the database
			        		    	if(i == 0)
			        		    	{
			        		    		Values.MasterUser = true;
			        		    	}
		        		    	}
		        		    }
		        	        
		        	        //the case that a user is found.
		        		    if(userfound)
		        		    {
		       	                /*
		       	                 * if the user is found then we can send the user to the smart home system
		       	                 * home screen, and now that the global flags have been set we can change the home screen
		       	                 * to allow the user to access the system
		       	                 */
			        		    Intent HomeStatusIntent= new Intent(getApplicationContext(), SmartHomeSystemActivity.class);
								startActivity(HomeStatusIntent);
								finish();
		        		    }
		        		    else
		        		    {
		        		    	//alert the user that thier information was not found
		            			Toast.makeText(getBaseContext(),
		            					"Username/Password not found",
		            					Toast.LENGTH_SHORT).show();
		                	}
	        	    	}//end of if(Values.DBconnection == true)
        	    	}//end of if(Values.DatabaseStrings.length == 1)
        		}//end of if(Values.DBconnection == false)
			}//end of public void onClick(View v)
        });//end of LogonButton
	}//end of public void onCreate(Bundle savedInstanceState)
	
	/**
	 * This class is an AsyncTask that gets the IP address of web server from the
	 * DDNS. It will also call the another AsyncTask on completion that gets the 
	 * current users from the web server.
	 * 
	 * <p>To use this class, you call new GetIP().execute("http://dmoody256.servebeer.com");
	 * this will start a new thread that will extract the IP address of the web server
	 * and place it in the global variables to be used later.</p>
	 * 
	 * @param <String> Each string should be a URL that will be executed and separated by commas 
	 * @author Daniel Moody
	 * @see AsyncTask
	 * @see org.apache.http
	 */
	private class GetIP extends AsyncTask<String, Void, String> {
		
		protected String doInBackground(String... URItoSend) {
			/*
			 * for the thread that is executed in the back ground
			 * we first need to extract the number of strings that were 
			 * passed to this class.
			 */
			int count = URItoSend.length;
			for (int i = 0; i < count; i++) {
			 
				
				//initialize the buffered reader
				BufferedReader in = null;
	        	
				/*
				 * this try is going to attempt to communicate with the server and get the
				 * the ip address from the returned html string 
				 */
	        	try {
	        		//first setup all our objects before sending a request to the URL
	                HttpClient client = new DefaultHttpClient();
	                HttpGet request = new HttpGet();
	                request.setURI(new URI(URItoSend[i]));
	                
	                //now send the request and get the response
	                HttpResponse response = client.execute(request);
	          
	                //read the response into a buffer
	                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	                
	                /*
	                 * now we are prepared to parse the response for the IP address we are looking
	                 * for. First we setup a few initial variables.  
	                 */
	                String IPString = null;
	                String IP = "fail";
	                
	                /*
	                 * now read each line of the response html and check to see if the 
	                 * IP address is in the next line. When the string <meta name=\"description\" content=\"\">
	                 * is found that means that the next line will contain the IP address
	                 */
	                while (!(in.readLine()).contentEquals("<meta name=\"description\" content=\"\">")){
	                }
	                
	                /*
	                 * we now have the string that contains the IP address, 
	                 * we split it and extract the IP address
	                 */
	                IPString = in.readLine();
	                String[] IPStringSplit = IPString.split("/");
	                
	                //a for loop is used for error purposes becuase we know the IP addresses location
	                for(int j = 0; j < IPStringSplit.length;j++)
	                {
	                	if(j == 2)
	                	{
	                		IP = IPStringSplit[j].toString();
	                	}
	                }
	
	                in.close();
	                //record the IP address to the global variables
	                Values.CurrentIP = IP;
	                
	                return IP;
	                } catch (URISyntaxException e) {
						e.printStackTrace();
					} catch (IOException e) {
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

	    protected void onPostExecute(String result) {
	    	//on completion we now have a working IP address, we will get the users
	        new UpdateUsers().execute("http://" + Values.CurrentIP + "/USER.txt");
	    }    
	}
	 
	/**
	 * This class is a AsyncTask that is used to get the text file from the web server. The file
	 * must be parsed correctly and then is placed in a local database.
	 * 
	 * <p>To use this class you call new UpdateUsers().execute("http://" + Values.CurrentIP + "/USER.txt");
	 * and this will use http get to receive the text file as a string. The web server has a quirck will it 
	 * will send the text file with unknown characters in between the real characters. This needs to be parsed 
	 * correctly and then stored in the local database.</p>
	 * 
	 * @param <String> Each string should be a URL that will be executed and separated by commas 
	 * @author Daniel Moody
	 * @see AsyncTask
	 * @see org.apache.http
	 */
	private class UpdateUsers extends AsyncTask<String, Void, String> {
		
		protected String doInBackground(String... URItoSend) {
			/*
			 * for the thread that is executed in the back ground
			 * we first need to extract the number of strings that were 
			 * passed to this class.
			 */
			int count = URItoSend.length;
			for (int i = 0; i < count; i++) {
				//initialize the buffered reader
				BufferedReader in = null;
	        	
				/*
				 * this try is going to attempt to communicate with the server and get the
				 * the ip address from the returned html string 
				 */
	        	try {
	        		//first setup all our objects before sending a request to the URL
	                HttpClient client = new DefaultHttpClient();
	                HttpGet request = new HttpGet();
	                request.setURI(new URI(URItoSend[i]));
	                
	                //now send the request and get the response
	                HttpResponse response = client.execute(request);
	                
	                //read the response into a buffer
	                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	              
	                //set up some intial variables for reading in the user data
	                StringBuffer sb = new StringBuffer("");
	                String line = "";
	                String NL = System.getProperty("line.separator");

	                /*
	                 * when reading in data from the web server, there is a problem were extra
	                 * unknown characters are produced. I was unable to find a reason as to
	                 * why the web server was sending the characters like this so i created
	                 * a work around that will sift through and get the correct characters and not 
	                 * remove the unknown characters. A StringBuilder was used because of the ease 
	                 * of appending characters to it.
	                 */
	                while ((line = in.readLine()) != null) 
	                {
	                	StringBuilder builder = new StringBuilder();
	                	
	                	//this for loop will iterate through the string and check each character
	                	for(int k = 0; k < line.length(); k++)
	                    {
	                		//if the character is actually a character then put it into the new string
	                    	if(line.charAt(k) < 128 && line.charAt(k) > 0)
	                    	{		
	                    		builder.append(line.charAt(k));
	                    	}
	                    }
	                	//insert eh new lines too, as they are used to seperate users
	                	if(builder.length() != 0)
	                		sb.append(builder + NL);
	                }
	                
	                in.close();
	                
	                //put the stringbuilder into a giant string
	                String page = sb.toString();
	                
	                //store the global variables
	                Values.DatabaseStrings = page.split("\n");
	                Values.DBconnection = true;
	                
	                /*
	                 * because this is a new thread we will use this time to insert
	                 * the user values we just got immediately into the database. we delete
	                 * the current database so that we can have an up to date local database
	                 */
	                mySQLiteAdapter.openToWrite();
		    		mySQLiteAdapter.deleteAllUsers();
		    		
		    		//parse each string and place the corresponding values into the correct positions
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
						e.printStackTrace();
					} catch (IOException e) {
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
	 }
}
