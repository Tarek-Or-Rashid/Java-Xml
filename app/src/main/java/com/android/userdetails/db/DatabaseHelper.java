package com.android.userdetails.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.userdetails.model.User;
import com.android.userdetails.model.Product;
import com.android.userdetails.model.Order;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDB";
    private static final int DATABASE_VERSION = 4; // ✅ Version increased

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_DOB = "date_of_birth";

    // Products Table
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_PRODUCT_ID = "id";
    private static final String COLUMN_PRODUCT_NAME = "name";
    private static final String COLUMN_PRODUCT_DESCRIPTION = "description";
    private static final String COLUMN_PRODUCT_PRICE = "price";
    private static final String COLUMN_PRODUCT_IMAGE = "image";

    // ✅ Orders Table
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_ORDER_PHONE = "phone_number";
    private static final String COLUMN_ORDER_ADDRESS = "order_address";
    private static final String COLUMN_ORDER_PRODUCTS = "products";
    private static final String COLUMN_ORDER_TOTAL = "total_price";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_ORDER_STATUS = "status";

    // Create Users Table
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_PHONE + " TEXT NOT NULL,"
            + COLUMN_GENDER + " TEXT NOT NULL,"
            + COLUMN_ADDRESS + " TEXT,"
            + COLUMN_DOB + " TEXT"
            + ");";

    // Create Products Table
    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + TABLE_PRODUCTS + "("
            + COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PRODUCT_NAME + " TEXT NOT NULL,"
            + COLUMN_PRODUCT_DESCRIPTION + " TEXT,"
            + COLUMN_PRODUCT_PRICE + " REAL NOT NULL,"
            + COLUMN_PRODUCT_IMAGE + " TEXT"
            + ");";

    // ✅ Create Orders Table
    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE " + TABLE_ORDERS + "("
            + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ORDER_PHONE + " TEXT,"
            + COLUMN_ORDER_ADDRESS + " TEXT,"
            + COLUMN_ORDER_PRODUCTS + " TEXT,"
            + COLUMN_ORDER_TOTAL + " REAL,"
            + COLUMN_ORDER_DATE + " TEXT,"
            + COLUMN_ORDER_STATUS + " TEXT DEFAULT 'Pending'"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_ORDERS); // ✅ Create orders table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_PRODUCTS);
        }
        if (oldVersion < 4) {
            db.execSQL(CREATE_TABLE_ORDERS); // ✅ Add orders table
        }
    }

    // ==================== USER METHODS ====================

    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    public boolean registerUser(User user) {
        if (checkEmail(user.getEmail())) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, encryptPassword(user.getPassword()));
        values.put(COLUMN_PHONE, user.getPhone());
        values.put(COLUMN_GENDER, user.getGender());
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_ID},
                    COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String encryptedPass = encryptPassword(password);
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_ID},
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{email, encryptedPass},
                    null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_USERS,
                    null,
                    COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)));
                user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                user.setDateOfBirth(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    public boolean updateUserProfile(String email, String address, String dob,
                                     String occupation, String bio, String profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_DOB, dob);

        int rows = db.update(TABLE_USERS, values,
                COLUMN_EMAIL + "=?",
                new String[]{email});

        return rows > 0;
    }

    public boolean updateBasicInfo(String email, String name, String phone, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_GENDER, gender);

        int rows = db.update(TABLE_USERS, values,
                COLUMN_EMAIL + "=?",
                new String[]{email});

        return rows > 0;
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        if (!checkUser(email, oldPassword)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, encryptPassword(newPassword));

        int rows = db.update(TABLE_USERS, values,
                COLUMN_EMAIL + "=?",
                new String[]{email});

        return rows > 0;
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rows = db.delete(TABLE_USERS,
                COLUMN_EMAIL + "=?",
                new String[]{email});

        return rows > 0;
    }

    // ==================== PRODUCT METHODS ====================

    public boolean addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_IMAGE, product.getImage());

        long result = db.insert(TABLE_PRODUCTS, null, values);
        return result != -1;
    }

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_PRODUCTS,
                    null, null, null, null, null,
                    COLUMN_PRODUCT_ID + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Product product = new Product();
                    product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                    product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
                    product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)));
                    product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)));

                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE));
                    product.setImage(imagePath != null ? imagePath : "");

                    productList.add(product);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return productList;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Product product = null;
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_PRODUCTS,
                    null,
                    COLUMN_PRODUCT_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)));

                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE));
                product.setImage(imagePath != null ? imagePath : "");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return product;
    }

    public boolean updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_IMAGE, product.getImage());

        int rows = db.update(TABLE_PRODUCTS, values,
                COLUMN_PRODUCT_ID + "=?",
                new String[]{String.valueOf(product.getId())});

        return rows > 0;
    }

    public boolean deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rows = db.delete(TABLE_PRODUCTS,
                COLUMN_PRODUCT_ID + "=?",
                new String[]{String.valueOf(id)});

        return rows > 0;
    }

    public List<Product> searchProducts(String searchText) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_PRODUCTS,
                    null,
                    COLUMN_PRODUCT_NAME + " LIKE ?",
                    new String[]{"%" + searchText + "%"},
                    null, null,
                    COLUMN_PRODUCT_ID + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Product product = new Product();
                    product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                    product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
                    product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)));
                    product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)));

                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE));
                    product.setImage(imagePath != null ? imagePath : "");

                    productList.add(product);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return productList;
    }

    // ==================== ORDER METHODS ✅ ====================

    // Get current date time
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Add Order
    public long addOrder(String phone, String address, String products, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ORDER_PHONE, phone);
        values.put(COLUMN_ORDER_ADDRESS, address);
        values.put(COLUMN_ORDER_PRODUCTS, products);
        values.put(COLUMN_ORDER_TOTAL, total);
        values.put(COLUMN_ORDER_DATE, getCurrentDateTime());
        values.put(COLUMN_ORDER_STATUS, "Pending");

        long id = db.insert(TABLE_ORDERS, null, values);
        return id;
    }

    // Get All Orders
    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ORDERS + " ORDER BY " + COLUMN_ORDER_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)));
                order.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_PHONE)));
                order.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ADDRESS)));
                order.setProducts(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_PRODUCTS)));
                order.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TOTAL)));
                order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE)));
                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_STATUS)));

                orderList.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return orderList;
    }

    // Update Order Status
    public void updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_STATUS, status);

        db.update(TABLE_ORDERS, values, COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});
    }

    // Delete Order
    public void deleteOrder(int orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDERS, COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});
    }

    // Get Order Count
    public int getOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Get Pending Orders Count
    public int getPendingOrdersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_STATUS + " = 'Pending'", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}