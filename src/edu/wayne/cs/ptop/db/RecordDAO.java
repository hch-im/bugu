package edu.wayne.cs.ptop.db;

import java.sql.Timestamp;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.wayne.cs.ptop.db.model.Record;
import edu.wayne.cs.ptop.db.model.ValueObject;

public class RecordDAO extends BaseDAO {
    private String tableName = "records";
    private String[] columns = {"id", "name", "addtime", "state"};
    
    public RecordDAO() {
    }

    public ArrayList<Record> getAvailableRecords()
    {
        ArrayList<Record> vos = new ArrayList<Record>();
        SQLiteDatabase db = this.openConnection();
        if(db == null) return null;
        String args[] = {"0"};
        
        Cursor cursor = db.query(getTableName(),
                getColums(), " state > ? ", args, null, null, " addtime desc");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Record vo = (Record)readObject(cursor);
            vos.add(vo);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return vos;     
    }
    
    @Override
    public ValueObject readObject(Cursor cur) {
        Record r = new Record();
        int i = 0;
        r.setId(cur.getLong(i++));
        r.setName(cur.getString(i++));
        Timestamp t = new Timestamp((long)cur.getFloat(i++));
        r.setAddtime(t);
        r.setState(cur.getInt(i++));
        return r;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String[] getColums() {
        return columns;
    }

    @Override
    public ContentValues wrapValues(ValueObject vo) {
        Record co = (Record)vo;
        ContentValues cv = new ContentValues();
        cv.put(columns[0], co.getId());
        cv.put(columns[1], co.getName());
        cv.put(columns[2], co.getAddtime().getTime());
        cv.put(columns[3], co.getState());
        return cv;
    }

}
