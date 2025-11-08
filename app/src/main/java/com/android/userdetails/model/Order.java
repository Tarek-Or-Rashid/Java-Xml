package com.android.userdetails.model;

public class Order {
    private int id;
    private String phoneNumber;
    private String address;
    private String products; // JSON format এ products
    private double totalPrice;
    private String orderDate;
    private String status; // "Pending", "Confirmed", "Delivered"

    // Constructor
    public Order() {
    }

    public Order(int id, String phoneNumber, String address, String products,
                 double totalPrice, String orderDate, String status) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.products = products;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}