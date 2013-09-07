package com.SS.Main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SQLiteAdapter {

public static final String MYDATABASE_NAME = "SMART_HOME_DB6";
public static final String MYDATABASE_TABLE_USERS = "USER_TABLE";
public static final String MYDATABASE_TABLE_MODS = "MOD_TABLE";
public static final int MYDATABASE_VERSION = 1;
public static final String KEY_ID = "_id";
public static final String KEY_USER = "Username";
public static final String KEY_PASS = "Password";
public static final String KEY_EMAIL = "Email";
public static final String KEY_MODNUM = "ModNumber";
public static final String KEY_LOCATION = "Location";
public static final String KEY_MODRELAY = "RelayStatus";
public static final String KEY_MODMAN = "ManualStatus";
public static final String KEY_MODMS = "MSStatus";
public static final String KEY_MODSS = "SSStatus";
public static final String KEY_MSTIME = "MStimed";

//create table MY_DATABASE (ID integer primary key, Content text not null);
private static final String SCRIPT_CREATE_USERTABLE =
"create table if not exists " + MYDATABASE_TABLE_USERS + " ("
+ KEY_ID + " integer primary key autoincrement, "
+ KEY_USER + " text not null,"
+ KEY_PASS + " text not null,"
+ KEY_EMAIL + " text not null);"; 

private static final String SCRIPT_CREATE_MODTABLE =
"create table if not exists " + MYDATABASE_TABLE_MODS + " ("
+ KEY_ID + " integer primary key autoincrement, "
+ KEY_MODNUM + " text not null,"
+ KEY_LOCATION + " text not null," 
+ KEY_MSTIME + " text not null,"
+ KEY_MODRELAY + " text not null,"
+ KEY_MODMAN + " text not null," 
+ KEY_MODMS + " text not null," 
+ KEY_MODSS + " text not null);";


private SQLiteHelper sqLiteHelper;
private SQLiteDatabase sqLiteDatabase;

private Context context;

public SQLiteAdapter(Context c){
context = c;
}

public SQLiteAdapter openToRead() throws android.database.SQLException {
sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
sqLiteDatabase = sqLiteHelper.getReadableDatabase();
return this;
}

public SQLiteAdapter openToWrite() throws android.database.SQLException {
sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
sqLiteDatabase = sqLiteHelper.getWritableDatabase();
return this;
}

public void close(){
sqLiteHelper.close();
}

public long UserInsert(String Username, String pass, String email){

ContentValues contentValues = new ContentValues();
contentValues.put(KEY_USER, Username);
contentValues.put(KEY_PASS, pass);
contentValues.put(KEY_EMAIL, email);

return sqLiteDatabase.insert(MYDATABASE_TABLE_USERS, null, contentValues);
}

public long InsertMod(String ModNumber, String Location, String MSTimed, String ModRelay, String ModManual, String ModMS, String ModSS){

	ContentValues contentValues = new ContentValues();
	contentValues.put(KEY_MODNUM, ModNumber);
	contentValues.put(KEY_LOCATION, Location);
	contentValues.put(KEY_MSTIME, MSTimed);
	contentValues.put(KEY_MODRELAY, ModRelay);
	contentValues.put(KEY_MODMAN, ModManual);
	contentValues.put(KEY_MODMS, ModMS);
	contentValues.put(KEY_MODSS, ModSS);
	

	return sqLiteDatabase.insert(MYDATABASE_TABLE_MODS, null, contentValues);
	}

public int deleteAllUsers(){
return sqLiteDatabase.delete(MYDATABASE_TABLE_USERS, null, null);
}

public int deleteAllMods(){
	return sqLiteDatabase.delete(MYDATABASE_TABLE_MODS, null, null);
	}

public void deleteUser(String Username){
	
	sqLiteDatabase.delete(MYDATABASE_TABLE_USERS, KEY_USER + "=?", new String[] {Username});
	return;
	}

public void deleteMod(String ModNumber){
	
	sqLiteDatabase.delete(MYDATABASE_TABLE_USERS, KEY_USER + "=?", new String[] {ModNumber});
	return;
	}

public String getEmail(String Username)
{
	Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_USERS, null, KEY_USER + "=?", new String[] {Username}, null, null, null);
	cursor.moveToNext();
	return cursor.getString(3);

}

public String getPassword(String Username)
{
	Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_USERS, null, KEY_USER + "=?", new String[] {Username}, null, null, null);
	cursor.moveToNext();
	return cursor.getString(2);
}

public String getLocation(String modnumber)
{
	Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_MODS, null, KEY_MODNUM + "=?", new String[] {modnumber}, null, null, null);
	cursor.moveToNext();
	return cursor.getString(2);
}

public String getMStimed(String modnumber)
{
	Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_MODS, null, KEY_MODNUM + "=?", new String[] {modnumber}, null, null, null);
	cursor.moveToNext();
	return cursor.getString(3);
}

public Cursor queueAll(){
String[] columns = new String[]{KEY_ID, KEY_USER};
Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_USERS, columns,
  null, null, null, null, null);

return cursor;

}

public Cursor queueAllMod(){
	
	Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_MODS, null,
	  null, null, null, null, null);

	return cursor;

	}

public int count(){
	
	Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM " + MYDATABASE_TABLE_USERS, null);
	cursor.moveToFirst();
	int count = cursor.getInt(0);
	return count;

	}

public String[] getUsernames(){
	
	Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_USER + " FROM " + MYDATABASE_TABLE_USERS, null);
	String[] usernames = new String[cursor.getCount()];
	int i = 0;
	while(cursor.moveToNext())
	{
		
		usernames[i] = cursor.getString(0);
		i++;
	}
	
	return usernames;

	}

public String[] getModNumbers(){
	
	Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_MODNUM + " FROM " + MYDATABASE_TABLE_MODS, null);
	String[] modnumbers = new String[cursor.getCount()];
	int i = 0;
	while(cursor.moveToNext())
	{
		
		modnumbers[i] = cursor.getString(0);
		i++;
	}
	
	return modnumbers;

	}

public String[] getPasswords(){
	
	Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_PASS + " FROM " + MYDATABASE_TABLE_USERS, null);
	String[] passwords = new String[cursor.getCount()];
	int i = 0;
	while(cursor.moveToNext())
	{
		
		passwords[i] = cursor.getString(0);
		i++;
	}
	
	return passwords;

	}

public String[] getNames(){
	
	Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_EMAIL + " FROM " + MYDATABASE_TABLE_USERS, null);
	String[] passwords = new String[cursor.getCount()];
	int i = 0;
	while(cursor.moveToNext())
	{
		
		passwords[i] = cursor.getString(0);
		i++;
	}
	
	return passwords;

	}

	


public class SQLiteHelper extends SQLiteOpenHelper {

	
    
public SQLiteHelper(Context context, String name,
  CursorFactory factory, int version) {
 super(context, name, factory, version);
}

@Override
public void onCreate(SQLiteDatabase db) {
 // TODO Auto-generated method stub
	
   
    db.execSQL(SCRIPT_CREATE_USERTABLE);
    db.execSQL(SCRIPT_CREATE_MODTABLE);
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 // TODO Auto-generated method stub
	db.execSQL(SCRIPT_CREATE_USERTABLE);
    db.execSQL(SCRIPT_CREATE_MODTABLE);

}

}

}