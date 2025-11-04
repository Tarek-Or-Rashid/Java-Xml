package com.android.userdetails.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.userdetails.R;
import com.android.userdetails.model.Product;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private TextView tvTotalPrice;
    private Button btnPlaceOrder;
    private CartAdapter cartAdapter;

    private List<Product> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Get cart items from GuestShoppingActivity
        cartItems = GuestShoppingActivity.getCartItems();

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems);
        recyclerViewCart.setAdapter(cartAdapter);

        updateTotalPrice();

        btnPlaceOrder.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                // Place order simulation
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                GuestShoppingActivity.clearCart();
                finish();
            }
        });
    }

    private void updateTotalPrice() {
        double total = 0;
        for (Product product : cartItems) {
            total += product.getPrice();
        }
        tvTotalPrice.setText("Total: ৳ " + String.format("%.2f", total));
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
