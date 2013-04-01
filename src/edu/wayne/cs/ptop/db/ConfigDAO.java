package edu.wayne.cs.ptop.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.wayne.cs.ptop.db.model.Config;
import edu.wayne.cs.ptop.db.model.ValueObject;

public class ConfigDAO extends BaseDAO {
    private String tableName = "configs";
    private String[] columns = {"id", "name", "value"};
    
    public ConfigDAO() {
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public Config get(String name)
    {
        SQLiteDatabase db = this.openConnection();
        if(db == null) return null;
        String[] args = {name};
        ArrayList<ValueObject> vos = select(getColums(), "name = ?", args, null, null, null);
        if(vos.size() == 0) return null;
        else
            return (Config)vos.get(0);
    }
    
    @Override
    public ValueObject readObject(Cursor cur) {
        Config co = new Config();
        co.setId(cur.getLong(0));
        co.setName(cur.getString(1));
        co.setValue(cur.getString(2));
        return co;
    }

    @Override
    public String[] getColums() {
        return columns;
    }

    @Override
    public ContentValues wrapValues(ValueObject vo) {
        Config co = (Config)vo;
        ContentValues cv = new ContentValues();
        cv.put(columns[0], co.getId());
        cv.put(columns[1], co.getName());
        cv.put(columns[2], co.getValue());
        
        return cv;
    }



    
}
