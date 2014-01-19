package edu.wayne.cs.bugu.db.model;

public class Config extends ValueObject{
    private String name;
    private String value;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    
}
