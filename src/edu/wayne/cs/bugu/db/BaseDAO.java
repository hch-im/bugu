package edu.wayne.cs.bugu.db;

import java.util.ArrayList;

import edu.wayne.cs.bugu.db.model.ValueObject;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class BaseDAO {
    private SqliteHelper helper = null;
    
    public BaseDAO()
    {
        helper = SqliteHelper.getInstance();
    }
    
    public SQLiteDatabase openConnection()
    {
        if(helper == null) return null;
        return helper.getConnection();
    }
    
    public void closeConnection()
    {
        if(helper != null)
            helper.close();
    }
    
    
    public boolean remove(long pk)
    {
        SQLiteDatabase db = this.openConnection();
        if(db == null) return false;
        String[] args = {pk+""};
        if(db.delete(getTableName(), "id=?", args) > 0)
            return true;
        else
            return false;
    }

    public boolean remove(String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = this.openConnection();
        if(db == null) return false;

        if(db.delete(getTableName(), selection, selectionArgs) > 0)
            return true;
        else
            return false;
    }
    
    public ValueObject get(int pk)
    {
        SQLiteDatabase db = this.openConnection();
        if(db == null) return null;
        String[] args = {pk+""};
        ArrayList<ValueObject> vos = select(getColums(), "id = ?", args, null, null, null);
        if(vos.size() == 0) return null;
        else
            return vos.get(0);
    }
    
    public ArrayList<ValueObject> getAllObject(String order)
    {        
        ArrayList<ValueObject> vos = new ArrayList<ValueObject>();
        SQLiteDatabase db = this.openConnection();
        if(db == null) return null;
        
        Cursor cursor = db.query(getTableName(),
                getColums(), null, null, null, null, order);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ValueObject vo = readObject(cursor);
            vos.add(vo);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return vos;
    }

    public ArrayList<ValueObject> select(String[] columns, String selection, String[] selectionArgs, 
            String groupBy, String having, String orderBy)
    {
        ArrayList<ValueObject> vos = new ArrayList<ValueObject>();
        SQLiteDatabase db = this.openConnection();
        if(db == null) return null;
        if(columns == null) columns = getColums();
        
        Cursor cursor = db.query(getTableName(),
                columns, selection, selectionArgs, groupBy, having, orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ValueObject vo = readObject(cursor);
            vos.add(vo);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return vos;
    }
    
    public int totalCount()
    {
        return count(null, null);
    }
    
    public int count(String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = this.openConnection();
        if(db == null) return 0;
        String[] cols = {"count(*)"};
        Cursor cursor = db.query(getTableName(),
                cols, selection, selectionArgs, null, null, null);

        int result = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result = cursor.getInt(0);
            break;
        }
        // Make sure to close the cursor
        cursor.close();
        return result;
    }
    
    public boolean insert(ValueObject vo) {
        SQLiteDatabase db = this.openConnection();
        if(db == null) return false;
        
        ContentValues values = wrapValues(vo);   
        values.remove("id");
        vo.setId(db.insert(getTableName(), null, values));
        
        if(vo.getId() != -1)
            return true;
        else
            return false;
    }
    
    public boolean update(ValueObject vo)
    {
        SQLiteDatabase db = this.openConnection();
        if(db == null) return false;
        
        ContentValues values = wrapValues(vo);        
        String[] args = {vo.getId()+""};
        
        if(db.update(getTableName(), values, " id = ?", args) > 0)        
            return true;
        else
            return false;        
    }
    
    public abstract ValueObject readObject(Cursor cur);
    public abstract String getTableName();
    public abstract String[] getColums();    
    public abstract ContentValues wrapValues(ValueObject vo);    
}
