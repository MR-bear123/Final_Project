package com.example.restaurantfinalproject.Model;

public class Cart {
    private String idcart;
    private String nameStaff;
    private String namefood;
    private String ImageUrl;
    private double price;
    private int quanlity;

    public Cart() {
    }

    public Cart(String namefood, double price, int quantity) {
        this.namefood = namefood;
        this.price = price;
        this.quanlity = quantity;
    }

    public Cart(String idcart, String nameStaff, String namefood, String imageUrl, double price, int quantity) {
        this.idcart = idcart;
        this.nameStaff = nameStaff;
        this.namefood = namefood;
        ImageUrl = imageUrl;
        this.price = price;
        this.quanlity = quantity;
    }

    // Getters and setters
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

//    public int getQuantity() {
//        return quantity;
//    }
//
//    public void setQuantity(int quantity) {
//        this.quantity = quantity;
//    }

    public String getNameStaff() {
        return nameStaff;
    }

    public void setNameStaff(String nameStaff) {
        this.nameStaff = nameStaff;
    }

    public String getNamefood() {
        return namefood;
    }

    public void setNamefood(String namefood) {
        this.namefood = namefood;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getIdcart() {
        return idcart;
    }

    public void setIdcart(String idcart) {
        this.idcart = idcart;
    }

    public int getQuanlity() {
        return quanlity;
    }

    public void setQuanlity(int quanlity) {
        this.quanlity = quanlity;
    }
}

