package com.example.softwareengineering;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MenuDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "staff_menu.db";
    private static final int DB_VERSION = 7;
    private static final String TABLE_MENU = "menu";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_CATEGORY = "category";
    private static final String COL_TYPE = "type";
    private static final String COL_PRICE = "price";
    private static final String COL_IMAGE = "image";

    public MenuDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MENU + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT UNIQUE, " +
                COL_CATEGORY + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_PRICE + " REAL, " +
                COL_IMAGE + " INTEGER, " +
                "description TEXT)";
        db.execSQL(createTable);

        addSampleData(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        onCreate(db);
    }

    public boolean addMenuItem(MenuItemModel item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, item.getName());
        cv.put(COL_CATEGORY, item.getCategory());
        cv.put(COL_TYPE, item.getType());
        cv.put(COL_PRICE, item.getPrice());
        cv.put(COL_IMAGE, item.getImageUri());
        cv.put("description", item.getDescription());

        long result = db.insert(TABLE_MENU, null, cv);
        db.close();

        return result != -1;
    }

    public boolean isMenuNameExists(String name, @Nullable Integer excludeId) {
        SQLiteDatabase db = getReadableDatabase();

        String selection;
        String[] selectionArgs;

        if (excludeId != null) {
            selection = COL_NAME + "=? AND " + COL_ID + "!=?";
            selectionArgs = new String[]{name, String.valueOf(excludeId)};
        } else {
            // for add
            selection = COL_NAME + "=?";
            selectionArgs = new String[]{name};
        }

        Cursor cursor = db.query(
                TABLE_MENU,
                new String[]{COL_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }



    // Update existing item
    public void updateMenuItem(MenuItemModel item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_CATEGORY, item.getCategory());
        values.put(COL_TYPE, item.getType());
        values.put(COL_PRICE, item.getPrice());
        values.put(COL_IMAGE, item.getImageUri()); // update image
        values.put("description", item.getDescription());

        db.update(TABLE_MENU, values, COL_ID + "=?", new String[]{String.valueOf(item.getId())});
        db.close();
    }


    // Get all items by category
    public List<MenuItemModel> getMenuItemsByCategory(String category) {
        List<MenuItemModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MENU, null, COL_CATEGORY + "=?",
                new String[]{category}, null, null, COL_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE));
                String image= cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                list.add(new MenuItemModel(id, name, category, type, price, image, description));

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void deleteMenuItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MENU, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    private void addSampleData(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        // Food
        cv.put("name", "Crispy Calamari");
        cv.put("category", "food");
        cv.put("type", "Appetizer");
        cv.put("price", 12);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/appetizer1");
        cv.put("description", "Tender calamari, lightly fried, served with marinara sauce.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Bruschetta");
        cv.put("category", "food");
        cv.put("type", "Appetizer");
        cv.put("price", 10);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/appetizer2");
        cv.put("description", "Toasted baguette slices topped with fresh tomatoes, basil, and balsamic glaze.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Grilled Salmon");
        cv.put("category", "food");
        cv.put("type", "Main Course");
        cv.put("price", 25);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/maincourse1");
        cv.put("description", "Fresh salmon fillet grilled to perfection, served with roasted vegetables.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Chicken Parmesan");
        cv.put("category", "food");
        cv.put("type", "Main Course");
        cv.put("price", 22);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/maincourse2");
        cv.put("description", "Breaded chicken topped with marinara and mozzarella, served with pasta.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Filet Mignon");
        cv.put("category", "food");
        cv.put("type", "Main Course");
        cv.put("price", 28);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/maincourse3");
        cv.put("description", "Tender filet mignon grilled to perfection, with mashed potatoes and asparagus.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Tiramisu");
        cv.put("category", "food");
        cv.put("type", "Dessert");
        cv.put("price", 18);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/dessert1");
        cv.put("description", "Classic Italian dessert with coffee-soaked ladyfingers, mascarpone cream, and cocoa.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Chocolate Lava Cake");
        cv.put("category", "food");
        cv.put("type", "Dessert");
        cv.put("price", 16);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/dessert2");
        cv.put("description", "Warm chocolate cake with molten center, served with vanilla ice cream.");
        db.insert(TABLE_MENU, null, cv);


        // Drinks
        cv.put("name", "Espresso");
        cv.put("category", "drink");
        cv.put("type", "Coffee");
        cv.put("price", 10);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/coffee1");
        cv.put("description", "A full-bodied espresso delivering rich aroma, deep flavor, and silky crema.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Latte");
        cv.put("category", "drink");
        cv.put("type", "Coffee");
        cv.put("price", 12);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/coffee2");
        cv.put("description", "Velvety espresso drink with warm milk and a soft, delicate foam crown.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Iced Tea");
        cv.put("category", "drink");
        cv.put("type", "Tea");
        cv.put("price", 10);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/tea1");
        cv.put("description", "Brewed to perfection, chilled, and served with ice and a hint of lemon.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Green Tea Latte");
        cv.put("category", "drink");
        cv.put("type", "Tea");
        cv.put("price", 15);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/tea2");
        cv.put("description", "Smooth matcha blended with steamed milk for a creamy, earthy green tea delight.");
        db.insert(TABLE_MENU, null, cv);



        cv.put("name", "Mojito");
        cv.put("category", "drink");
        cv.put("type", "Cocktail");
        cv.put("price", 20);
        cv.put("image", R.drawable.cocktail1);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/cocktail1");
        cv.put("description", "A refreshing Cuban cocktail blending rum, lime, mint, sugar, and sparkling soda.");
        db.insert(TABLE_MENU, null, cv);


        cv.put("name", "Old Fashioned");
        cv.put("category", "drink");
        cv.put("type", "Cocktail");
        cv.put("price", 14);
        cv.put("image", R.drawable.cocktail2);
        cv.put("image", "android.resource://com.example.softwareengineering/drawable/cocktail2");
        cv.put("description", "A smooth and elegant blend of whiskey, sugar, and bitters served over ice.");
        db.insert(TABLE_MENU, null, cv);

    }

}
