package com.android.userdetails.model;

public class Product {
    private int id;                 // ✅ Added for database ID
    private String name;
    private String description;
    private double price;
    private String imagePath;

    public Product() {}

    public Product(String name, String description, double price, String imagePath) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imagePath = imagePath;
    }

    // ✅ Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }

    // ✅ Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // ✅ Optional alias (for backward compatibility with DatabaseHelper)
    public String getImage() { return imagePath; }
    public void setImage(String imagePath) { this.imagePath = imagePath; }
}
