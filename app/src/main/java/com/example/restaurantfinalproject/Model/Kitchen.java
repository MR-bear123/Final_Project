package com.example.restaurantfinalproject.Model;

import java.util.List;

public class Kitchen {
    private String id;
    private String numberTable;
    private String userName;
    private String namefood;
    private String Description;
    private String Stastu;

    public Kitchen() {
    }

    public Kitchen(String id, String numberTable, String userName, String namefood, String description, String stastu) {
        this.id = id;
        this.numberTable = numberTable;
        this.userName = userName;
        this.namefood = namefood;
        Description = description;
        Stastu = stastu;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumberTable() {
        return numberTable;
    }

    public void setNumberTable(String numberTable) {
        this.numberTable = numberTable;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNamefood() {
        return namefood;
    }

    public void setNamefood(String namefood) {
        this.namefood = namefood;
    }


    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getStastu() {
        return Stastu;
    }

    public void setStastu(String stastu) {
        Stastu = stastu;
    }
}
