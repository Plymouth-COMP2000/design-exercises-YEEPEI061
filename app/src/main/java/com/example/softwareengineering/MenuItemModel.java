package com.example.softwareengineering;

public class MenuItemModel {
    private int id;
    private final String name;
    private final String category;
    private final String type;
    private final double price;
    private final String imageUri;
    private final String description;

    public MenuItemModel(int id, String name, String category, String type, double price, String imageUri, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.type = type;
        this.price = price;
        this.imageUri = imageUri;
        this.description = description;
    }

    public MenuItemModel(String name, String category, String type, double price, String imageUri, String description) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.price = price;
        this.imageUri = imageUri;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public String getType() {
        return type;
    }
    public double getPrice() {
        return price;
    }
    public String getImageUri() {
        return imageUri;
    }
    public String getDescription() {
        return description;
    }
}
