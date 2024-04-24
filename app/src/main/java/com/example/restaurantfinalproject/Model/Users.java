package com.example.restaurantfinalproject.Model;

public class Users {
    private  String Uid;
    private  String Name;
    private  String  PhoneNumber;
    private String Email;
    private String Password;
    private String Role;
    private String ImageUrl;
    private String key;
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public Users(String uid) {
        Uid = uid;
    }

    public String getName() {
        return Name;
    }

    public Users() {
    }

    public Users(String uid, String name, String  phoneNumber, String email, String password,String role) {
        Uid = uid;
        Name = name;
        PhoneNumber = phoneNumber;
        Email = email;
        Password = password;
        Role = role;
    }

    public void setName(String name) {
        Name = name;
    }

    public String  getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String  phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

}
