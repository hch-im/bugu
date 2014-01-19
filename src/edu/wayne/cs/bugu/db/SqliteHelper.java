package edu.wayne.cs.bugu.db;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class SqliteHelper{

    public static final String TABLE_CONFIG = "configs";
    public static final String TABLE_RECORD = "records";
    
    private static final String DATABASE_NAME = "ptopa.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase database = null;
    private static SqliteHelper instance = null;
    
    // Database creation sql statement
    private static final String CONFIG_CREATE = "create table "
            + TABLE_CONFIG + "(id integer primary key autoincrement not null, name text not null unique, value text not null)";
    private static final String RECORD_CREATE = "create table "
            + TABLE_RECORD + "(id integer primary key autoincrement not null, name text not null unique, addtime real, state integer)";
    
    private SqliteHelper() {
    }
    
    public static SqliteHelper getInstance()
    {
        if(instance == null)
            instance = new SqliteHelper();
        
        return instance;
    }
    
    private void open(){
        try{            
            File root = Environment.getExternalStorageDirectory();
            File ptopa = new File(root, "ptopa");
            if(ptopa.exists() == false) ptopa.mkdir();
            database = SQLiteDatabase.openOrCreateDatabase(root.getPath() + "/ptopa/" + DATABASE_NAME, null);
            switch(database.getVersion())
            {
                case 0:
                    database.execSQL(CONFIG_CREATE);
                    database.execSQL(RECORD_CREATE);
                    database.setVersion(DATABASE_VERSION);
                default:
                    break;
            }
        }catch( Exception ex){            
        }
    }

    public void close() {
        if(database != null)
            database.close();
    }    
    
    public SQLiteDatabase getConnection()
    {
        if(database == null || database.isOpen() == false)
            open();
        
        return database;
    }
}
