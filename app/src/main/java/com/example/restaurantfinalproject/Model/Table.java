package com.example.restaurantfinalproject.Model;

public class Table {
    private boolean SMSSent;
    private String id;
    private String cuname;
    private String cuphone;
    private String description;
    private String date;
    private String time;
    private String randomCode;
    private String status;

    public Table() {
    }

    public Table(String id, String cuname, String cuphone, String description, String date, String time, String randomCode,String status) {
        this.id = id;
        this.cuname = cuname;
        this.cuphone = cuphone;
        this.description = description;
        this.date = date;
        this.time = time;
        this.randomCode = randomCode;
        this.status = status;
    }

    public boolean SMSSent() {
        return SMSSent;
    }

    public void setSMSSent(boolean SMSSent) {
        SMSSent = SMSSent;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCuname() {
        return cuname;
    }

    public void setCuname(String cuname) {
        this.cuname = cuname;
    }

    public String getCuphone() {
        return cuphone;
    }

    public void setCuphone(String cuphone) {
        this.cuphone = cuphone;
    }

    public String getRandomCode() {
        return randomCode;
    }

    public void setRandomCode(String randomCode) {
        this.randomCode = randomCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}