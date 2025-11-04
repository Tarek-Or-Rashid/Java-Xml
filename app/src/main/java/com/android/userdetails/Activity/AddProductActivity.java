package com.android.userdetails.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.bumptech.glide.Glide;
import com.android.userdetails.model.Product;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddProductActivity extends AppCompatActivity {

    private EditText etProductName, etProductDescription, etProductPrice;
    private ImageView ivProductImage;
    private Button btnSelectImage, btnAddProduct, btnCancel;
    private DatabaseHelper databaseHelper;
    private String selectedImagePath = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        // Initialize Image Picker
        initImagePicker();

        // Initialize Views
        initViews();

        // Set Click Listeners
        setClickListeners();
    }

    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // ✅ Save image to internal storage
                            selectedImagePath = saveImageToInternalStorage(imageUri);

                            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                                // Load image using Glide
                                Glide.with(AddProductActivity.this)
                                        .load(new File(selectedImagePath))
                                        .placeholder(android.R.drawable.ic_menu_gallery)
                                        .error(android.R.drawable.ic_menu_gallery)
                                        .into(ivProductImage);

                                ivProductImage.setVisibility(View.VISIBLE);
                                Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    // ✅ Save image to internal storage permanently
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            // Create unique filename
            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            File directory = new File(getFilesDir(), "product_images");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);

            // Copy image to internal storage
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initViews() {
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        ivProductImage = findViewById(R.id.ivProductImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setClickListeners() {
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProduct();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void addProduct() {
        String name = etProductName.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etProductName.setError("Product name is required");
            etProductName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etProductPrice.setError("Price is required");
            etProductPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etProductPrice.setError("Price must be greater than 0");
                etProductPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etProductPrice.setError("Invalid price format");
            etProductPrice.requestFocus();
            return;
        }

        // Create Product Object
        Product product = new Product(name, description, price, selectedImagePath);

        // Add to Database
        boolean success = databaseHelper.addProduct(product);

        if (success) {
            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
            clearFields();
            finish();
        } else {
            Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etProductName.setText("");
        etProductDescription.setText("");
        etProductPrice.setText("");
        selectedImagePath = "";
        ivProductImage.setImageDrawable(null);
        ivProductImage.setVisibility(View.GONE);
    }
}