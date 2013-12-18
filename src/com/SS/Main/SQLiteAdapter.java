package com.SS.Main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SQLiteAdapter {

    // CONSTANT KEYS
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

    //CREATE USER TABLE STRING
    private static final String SCRIPT_CREATE_USERTABLE =
    "create table if not exists " + MYDATABASE_TABLE_USERS + " ("
    + KEY_ID + " integer primary key autoincrement, "
    + KEY_USER + " text not null,"
    + KEY_PASS + " text not null,"
    + KEY_EMAIL + " text not null);"; 

    //CREATE MODULE TABLE STRING
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

    //variables
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    // gets the current context for the database connection
    public SQLiteAdapter(Context c){

        context = c;
    }

    // This function will open the SQLite database for reading
    public SQLiteAdapter openToRead() throws android.database.SQLException {

        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    // This function will open the SQLite database for writing
    public SQLiteAdapter openToWrite() throws android.database.SQLException {

    sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
    sqLiteDatabase = sqLiteHelper.getWritableDatabase();
    return this;
    }

    // This will close the database
    public void close(){
        sqLiteHelper.close();
    }

    // This function will insert a user, with the passed user info
    public long UserInsert(String Username, String pass, String email){

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_USER, Username);
        contentValues.put(KEY_PASS, pass);
        contentValues.put(KEY_EMAIL, email);

        return sqLiteDatabase.insert(MYDATABASE_TABLE_USERS, null, contentValues);
    }

    // This function will insert a module with passed info
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

    // This function will delete all the users in the database
    public int deleteAllUsers(){
        return sqLiteDatabase.delete(MYDATABASE_TABLE_USERS, null, null);
    }
    
    // this function will delete all the modules in the database
    public int deleteAllMods(){
        return sqLiteDatabase.delete(MYDATABASE_TABLE_MODS, null, null);
    }

    // This function will delete the passed user from the database
    public void deleteUser(String Username){
        
        sqLiteDatabase.delete(MYDATABASE_TABLE_USERS, KEY_USER + "=?", new String[] {Username});
        return;
    }

    // This function will delete the passed mod from the database
    public void deleteMod(String ModNumber){
        
        sqLiteDatabase.delete(MYDATABASE_TABLE_USERS, KEY_USER + "=?", new String[] {ModNumber});
        return;
    }

    // This get the passed user's email from the database
    public String getEmail(String Username){
    
        Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_USERS, null, KEY_USER + "=?", new String[] {Username}, null, null, null);
        cursor.moveToNext();
        return cursor.getString(3);
    }

    // This function will get the passed users password from the database
    public String getPassword(String Username){
    
        Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_USERS, null, KEY_USER + "=?", new String[] {Username}, null, null, null);
        cursor.moveToNext();
        return cursor.getString(2);
    }

    // This will get the pass mods location from the database
    public String getLocation(String modnumber){
    
        Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_MODS, null, KEY_MODNUM + "=?", new String[] {modnumber}, null, null, null);
        cursor.moveToNext();
        return cursor.getString(2);
    }

    // This function will check if this module set to motion snesor control
    public String getMStimed(String modnumber){
    
        Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_MODS, null, KEY_MODNUM + "=?", new String[] {modnumber}, null, null, null);
        cursor.moveToNext();
        return cursor.getString(3);
    }

    // This will get all the users from the database
    public Cursor queueAll(){
    
        String[] columns = new String[]{KEY_ID, KEY_USER};
        Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_USERS, columns,
            null, null, null, null, null);

        return cursor;
    }

    // This function will get all the modules from the database
    public Cursor queueAllMod(){
        
        Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_MODS, null,
            null, null, null, null, null);

        return cursor;
    }

    // This function will get the number of users in the database
    public int count(){
        
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM " + MYDATABASE_TABLE_USERS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        return count;
    }

    // This function will get the usernames from the database and return them as an array
    public String[] getUsernames(){
        
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_USER + " FROM " + MYDATABASE_TABLE_USERS, null);
        String[] usernames = new String[cursor.getCount()];
        int i = 0;
        while(cursor.moveToNext()){
            usernames[i] = cursor.getString(0);
            i++;
        }
        
        return usernames;
    }

    // This will get the modules numbers from the database and return them as an array
    public String[] getModNumbers(){
        
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_MODNUM + " FROM " + MYDATABASE_TABLE_MODS, null);
        String[] modnumbers = new String[cursor.getCount()];
        int i = 0;
        while(cursor.moveToNext()){
            modnumbers[i] = cursor.getString(0);
            i++;
        }
        
        return modnumbers;
    }

    // This function will get the passwords of the users and return them as an array
    public String[] getPasswords(){
        
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_PASS + " FROM " + MYDATABASE_TABLE_USERS, null);
        String[] passwords = new String[cursor.getCount()];
        int i = 0;
        while(cursor.moveToNext()){ 
            passwords[i] = cursor.getString(0);
            i++;
        }
        
        return passwords;
    }

    // this function will get the names of the users in the database and return them as an array
    public String[] getNames(){
        
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_EMAIL + " FROM " + MYDATABASE_TABLE_USERS, null);
        String[] passwords = new String[cursor.getCount()];
        int i = 0;
        while(cursor.moveToNext()){
            passwords[i] = cursor.getString(0);
            i++;
        }
        
        return passwords;
    }

    // class for maintianing version of the database when opening
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