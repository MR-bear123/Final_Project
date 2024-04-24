package com.example.restaurantfinalproject.Model;
import java.util.List;

public class History {
    private String userId;
    private String numberTable;
    private String userName;
    private List<Cart> cartList;
    private double totalPrice;
    private String timestamp;
    private String date;
    private String CodeBill;


    public History() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Cart> getCartList() {
        return cartList;
    }

    public void setCartList(List<Cart> cartList) {
        this.cartList = cartList;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCodeBill() {
        return CodeBill;
    }

    public void setCodeBill(String codeBill) {
        CodeBill = codeBill;
    }

    public String getNumberTable() {
        return numberTable;
    }

    public void setNumberTable(String numberTable) {
        this.numberTable = numberTable;
    }

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}
}
