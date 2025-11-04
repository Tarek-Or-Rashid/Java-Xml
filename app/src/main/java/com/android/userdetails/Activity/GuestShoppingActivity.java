package com.android.userdetails.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.android.userdetails.model.Product;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GuestShoppingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GuestProductAdapter adapter;
    private List<Product> productList;
    private DatabaseHelper databaseHelper;
    private LinearLayout llNoProducts;
    private TextView tvProductCount;
    private CardView cvProductCount;
    private TextView tvCartCount;
    private CardView cvCart; // ✅ Fixed: define cvCart

    // Cart items
    private static List<Product> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_shopping);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadProducts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewProducts);
        llNoProducts = findViewById(R.id.llNoProducts);
        tvProductCount = findViewById(R.id.tvProductCount);
        cvProductCount = findViewById(R.id.cvProductCount);
        tvCartCount = findViewById(R.id.tvCartCount);
        cvCart = findViewById(R.id.cvCart); // ✅ Initialize cart button

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new GuestProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(GuestShoppingActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        cvCart.setOnClickListener(v -> {
            Intent intent = new Intent(GuestShoppingActivity.this, CartActivity.class);
            startActivity(intent);
        });

        updateCartCount();
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(databaseHelper.getAllProducts());
        adapter.notifyDataSetChanged();

        if (productList.isEmpty()) {
            llNoProducts.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            cvProductCount.setVisibility(View.GONE);
        } else {
            llNoProducts.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            cvProductCount.setVisibility(View.VISIBLE);
            tvProductCount.setText(String.valueOf(productList.size()));
        }
    }

    private void updateCartCount() {
        if (tvCartCount != null) {
            if (cartItems.size() > 0) {
                tvCartCount.setVisibility(View.VISIBLE);
                tvCartCount.setText(String.valueOf(cartItems.size()));
            } else {
                tvCartCount.setVisibility(View.GONE);
            }
        }
    }

    private void addToCart(Product product) {
        boolean alreadyInCart = false;
        for (Product item : cartItems) {
            if (item.getId() == product.getId()) {
                alreadyInCart = true;
                break;
            }
        }

        if (!alreadyInCart) {
            cartItems.add(product);
            Toast.makeText(this, product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
            updateCartCount();
        } else {
            Toast.makeText(this, "Already in cart!", Toast.LENGTH_SHORT).show();
        }
    }

    public static List<Product> getCartItems() {
        return cartItems;
    }

    public static void clearCart() {
        cartItems.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
        updateCartCount();
    }

    // Recycler Adapter
    private class GuestProductAdapter extends RecyclerView.Adapter<GuestProductAdapter.ProductViewHolder> {

        private List<Product> products;

        public GuestProductAdapter(List<Product> products) {
            this.products = products;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_guest_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            holder.bind(products.get(position));
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView tvProductName, tvProductDescription, tvProductPrice;
            ImageView ivProductImage;
            Button btnAddToCart;

            public ProductViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
                tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
                ivProductImage = itemView.findViewById(R.id.ivProductImage);
                btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            }

            public void bind(final Product product) {
                tvProductName.setText(product.getName());
                tvProductDescription.setText(product.getDescription());
                tvProductPrice.setText("৳ " + String.format("%.2f", product.getPrice()));

                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    File imageFile = new File(product.getImage());
                    if (imageFile.exists()) {
                        Glide.with(itemView.getContext())
                                .load(imageFile)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(ivProductImage);
                    } else {
                        ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }

                btnAddToCart.setOnClickListener(v -> addToCart(product));
            }
        }
    }
}
