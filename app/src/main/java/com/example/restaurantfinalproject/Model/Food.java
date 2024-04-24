package com.example.restaurantfinalproject.Model;

public class Food {
    private  String Fid;
    private String name;
    private String description;
    private String type;
    private double price;
    private String ImageUrl;

    public Food() {
    }

    public Food(String fid, String name, String description, String type, double price, String imageUrl) {
        Fid = fid;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        ImageUrl = imageUrl;
    }



    public String getFid() {
        return Fid;
    }

    public void setFid(String fid) {
        Fid = fid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }
}
