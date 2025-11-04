package com.android.userdetails.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.bumptech.glide.Glide;
import com.android.userdetails.model.Product;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ViewProductActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private DatabaseHelper databaseHelper;
    private LinearLayout tvNoProducts;
    private TextView tvProductCount;
    private View cvProductCount;

    // For Edit Image
    private ActivityResultLauncher<Intent> editImagePickerLauncher;
    private String tempEditImagePath = "";
    private Product editingProduct = null;
    private ImageView currentEditImageView = null; // Keep reference to ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        // Initialize Edit Image Picker
        initEditImagePicker();

        // Initialize Views
        initViews();

        // Add First Product Button Click
        Button btnAddFirstProduct = findViewById(R.id.btnAddFirstProduct);
        if (btnAddFirstProduct != null) {
            btnAddFirstProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to AddProductActivity
                    Intent intent = new Intent(ViewProductActivity.this, AddProductActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Load Products
        loadProducts();
    }

    // Edit Image Picker - Fixed version
    private void initEditImagePicker() {
        editImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null && editingProduct != null) {
                            tempEditImagePath = saveImageToInternalStorage(imageUri);
                            if (tempEditImagePath != null && !tempEditImagePath.isEmpty()) {
                                Toast.makeText(this, "New image selected", Toast.LENGTH_SHORT).show();

                                // Update the ImageView directly instead of recreating dialog
                                if (currentEditImageView != null) {
                                    File imageFile = new File(tempEditImagePath);
                                    if (imageFile.exists()) {
                                        Glide.with(this)
                                                .load(imageFile)
                                                .placeholder(android.R.drawable.ic_menu_gallery)
                                                .error(android.R.drawable.ic_menu_gallery)
                                                .into(currentEditImageView);
                                        currentEditImageView.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

    // Save image to internal storage
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            File directory = new File(getFilesDir(), "product_images");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);

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
        recyclerView = findViewById(R.id.recyclerViewProducts);
        tvNoProducts = findViewById(R.id.tvNoProducts);
        tvProductCount = findViewById(R.id.tvProductCount);
        cvProductCount = findViewById(R.id.cvProductCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(databaseHelper.getAllProducts());
        adapter.notifyDataSetChanged();

        // Show/hide views based on product count
        if (productList.isEmpty()) {
            tvNoProducts.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (cvProductCount != null) {
                cvProductCount.setVisibility(View.GONE);
            }
        } else {
            tvNoProducts.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Show product count card
            if (cvProductCount != null && tvProductCount != null) {
                cvProductCount.setVisibility(View.VISIBLE);
                tvProductCount.setText(String.valueOf(productList.size()));
            }
        }
    }

    // RecyclerView Adapter
    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private List<Product> products;

        public ProductAdapter(List<Product> products) {
            this.products = products;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {

            TextView tvProductName, tvProductDescription, tvProductPrice;
            ImageView ivProductImage;
            Button btnEdit, btnDelete;

            public ProductViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
                tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
                ivProductImage = itemView.findViewById(R.id.ivProductImage);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }

            public void bind(final Product product) {
                tvProductName.setText(product.getName());
                tvProductDescription.setText(product.getDescription());
                tvProductPrice.setText("৳ " + String.format("%.2f", product.getPrice()));

                // Load image from file path
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    File imageFile = new File(product.getImage());

                    if (imageFile.exists()) {
                        Glide.with(itemView.getContext())
                                .load(imageFile)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .into(ivProductImage);
                    } else {
                        ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }

                    ivProductImage.setVisibility(View.VISIBLE);
                } else {
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    ivProductImage.setVisibility(View.VISIBLE);
                }

                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(product);
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteDialog(product);
                    }
                });
            }
        }
    }

    // Edit Dialog with Image Selection - Fixed version
    private void showEditDialog(final Product product) {
        editingProduct = product;

        // Initialize temp path with current image
        tempEditImagePath = product.getImage() != null ? product.getImage() : "";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null);
        builder.setView(dialogView);

        final EditText etName = dialogView.findViewById(R.id.etEditProductName);
        final EditText etDescription = dialogView.findViewById(R.id.etEditProductDescription);
        final EditText etPrice = dialogView.findViewById(R.id.etEditProductPrice);
        final Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);
        final ImageView ivEditImage = dialogView.findViewById(R.id.ivEditProductImage);

        // Store reference to ImageView for later updates
        currentEditImageView = ivEditImage;

        // Set current values
        etName.setText(product.getName());
        etDescription.setText(product.getDescription());
        etPrice.setText(String.valueOf(product.getPrice()));

        // Load current image
        if (tempEditImagePath != null && !tempEditImagePath.isEmpty()) {
            File imageFile = new File(tempEditImagePath);
            if (imageFile.exists()) {
                Glide.with(this)
                        .load(imageFile)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivEditImage);
                ivEditImage.setVisibility(View.VISIBLE);
            }
        }

        // Change Image Button
        if (btnChangeImage != null) {
            btnChangeImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    editImagePickerLauncher.launch(intent);
                }
            });
        }

        builder.setTitle("Edit Product");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(ViewProductActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (priceStr.isEmpty()) {
                    Toast.makeText(ViewProductActivity.this, "Price cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                double price;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(ViewProductActivity.this, "Invalid price", Toast.LENGTH_SHORT).show();
                    return;
                }

                product.setName(name);
                product.setDescription(description);
                product.setPrice(price);
                product.setImage(tempEditImagePath);

                boolean success = databaseHelper.updateProduct(product);
                if (success) {
                    Toast.makeText(ViewProductActivity.this, "Product updated", Toast.LENGTH_SHORT).show();
                    loadProducts();
                } else {
                    Toast.makeText(ViewProductActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }

                // Clear references
                editingProduct = null;
                tempEditImagePath = "";
                currentEditImageView = null;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Clear references
                editingProduct = null;
                tempEditImagePath = "";
                currentEditImageView = null;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Clear references when dialog is dismissed
                currentEditImageView = null;
            }
        });

        dialog.show();
    }

    private void showDeleteDialog(final Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Product");
        builder.setMessage("Are you sure you want to delete '" + product.getName() + "'?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete image file if exists
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    File imageFile = new File(product.getImage());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }

                boolean success = databaseHelper.deleteProduct(product.getId());
                if (success) {
                    Toast.makeText(ViewProductActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                    loadProducts();
                } else {
                    Toast.makeText(ViewProductActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}