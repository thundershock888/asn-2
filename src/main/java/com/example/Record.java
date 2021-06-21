package com.example;

public class Record {
    private int id;
    private String name;
    private String color;
    private int area;
    private int wid;
    private int len;

    public Record(int id, String name, String color, int area, int len, int wid) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.area = area;
        this.len = len;
        this.wid = wid;


    }





    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public void setWid(int wid) {
        this.wid = wid;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getArea() {
        return area;
    }

    public int getWid() {
        return wid;
    }

    public int getLen() {
        return len;
    }
}
