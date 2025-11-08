package com.android.userdetails.Activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.android.userdetails.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private TextView tvTotalPrice;
    private Button btnPlaceOrder;
    private CartAdapter cartAdapter;
    private DatabaseHelper databaseHelper;

    private List<Product> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        databaseHelper = new DatabaseHelper(this);

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        cartItems = GuestShoppingActivity.getCartItems();

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems);
        recyclerViewCart.setAdapter(cartAdapter);

        updateTotalPrice();

        btnPlaceOrder.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                showOrderDialog(); // ✅ Dialog দেখাবে
            }
        });
    }

    // ✅ Order Dialog
    private void showOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_order_details, null);
        builder.setView(dialogView);

        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        Button btnConfirmOrder = dialogView.findViewById(R.id.btnConfirmOrder);
        Button btnCancelOrder = dialogView.findViewById(R.id.btnCancelOrder);

        AlertDialog dialog = builder.create();

        btnConfirmOrder.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (phone.isEmpty()) {
                etPhone.setError("Phone number required");
                etPhone.requestFocus();
                return;
            }

            if (address.isEmpty()) {
                etAddress.setError("Address required");
                etAddress.requestFocus();
                return;
            }

            if (phone.length() != 11) {
                etPhone.setError("Enter valid 11 digit phone number");
                etPhone.requestFocus();
                return;
            }

            placeOrder(phone, address);
            dialog.dismiss();
        });

        btnCancelOrder.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ✅ Order database এ save করবে
    private void placeOrder(String phone, String address) {
        try {
            // Products কে JSON format এ convert করুন
            JSONArray productsArray = new JSONArray();
            for (Product product : cartItems) {
                JSONObject productObj = new JSONObject();
                productObj.put("id", product.getId());
                productObj.put("name", product.getName());
                productObj.put("price", product.getPrice());
                productsArray.put(productObj);
            }

            String productsJson = productsArray.toString();
            double total = calculateTotal();

            // Database এ save করুন
            long orderId = databaseHelper.addOrder(phone, address, productsJson, total);

            if (orderId > 0) {
                Toast.makeText(this, "Order placed successfully! Order #" + orderId,
                        Toast.LENGTH_LONG).show();
                GuestShoppingActivity.clearCart();
                finish();
            } else {
                Toast.makeText(this, "Failed to place order!", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error placing order!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalPrice() {
        double total = calculateTotal();
        tvTotalPrice.setText("Total: ৳ " + String.format("%.2f", total));
    }

    private double calculateTotal() {
        double total = 0;
        for (Product product : cartItems) {
            total += product.getPrice();
        }
        return total;
    }

    // Simple adapter for showing cart items
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
        private List<Product> items;

        public CartAdapter(List<Product> items) {
            this.items = items;
        }

        @Override
        public CartViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart_product, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CartViewHolder holder, int position) {
            Product product = items.get(position);
            holder.tvName.setText(product.getName());
            holder.tvPrice.setText("৳ " + String.format("%.2f", product.getPrice()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class CartViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;

            public CartViewHolder(android.view.View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCartProductName);
                tvPrice = itemView.findViewById(R.id.tvCartProductPrice);
            }
        }
    }
}