package edu.wayne.cs.ptop.db.model;

import java.sql.Timestamp;

public class Record extends ValueObject {
    private String name;
    private Timestamp addtime;
    private int state;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Timestamp getAddtime() {
        return addtime;
    }
    public void setAddtime(Timestamp addtime) {
        this.addtime = addtime;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    
    
}
