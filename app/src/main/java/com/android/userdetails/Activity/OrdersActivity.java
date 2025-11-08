package com.android.userdetails.Activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.android.userdetails.model.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrdersAdapter adapter;
    private List<Order> orderList;
    private DatabaseHelper databaseHelper;
    private LinearLayout llNoOrders;
    private TextView tvOrderCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadOrders();
    }

    private void initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        llNoOrders = findViewById(R.id.llNoOrders);
        tvOrderCount = findViewById(R.id.tvOrderCount);

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList);
        recyclerViewOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        orderList.clear();
        orderList.addAll(databaseHelper.getAllOrders());
        adapter.notifyDataSetChanged();

        if (orderList.isEmpty()) {
            llNoOrders.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            tvOrderCount.setVisibility(View.GONE);
        } else {
            llNoOrders.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
            tvOrderCount.setVisibility(View.VISIBLE);
            tvOrderCount.setText("Total Orders: " + orderList.size());
        }
    }

    // ✅ Order Details Dialog
    private void showOrderDetailsDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_order_details_view, null);
        builder.setView(dialogView);

        TextView tvOrderId = dialogView.findViewById(R.id.tvOrderId);
        TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
        TextView tvAddress = dialogView.findViewById(R.id.tvAddress);
        TextView tvProducts = dialogView.findViewById(R.id.tvProducts);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotal);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvStatus = dialogView.findViewById(R.id.tvStatus);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        // Set data
        tvOrderId.setText("Order #" + order.getId());
        tvPhone.setText(order.getPhoneNumber());
        tvAddress.setText(order.getAddress());
        tvTotal.setText("৳ " + String.format("%.2f", order.getTotalPrice()));
        tvDate.setText(order.getOrderDate());
        tvStatus.setText(order.getStatus());

        // Parse and display products
        try {
            JSONArray productsArray = new JSONArray(order.getProducts());
            StringBuilder productsText = new StringBuilder();
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject product = productsArray.getJSONObject(i);
                productsText.append((i + 1)).append(". ")
                        .append(product.getString("name"))
                        .append(" - ৳")
                        .append(String.format("%.2f", product.getDouble("price")))
                        .append("\n");
            }
            tvProducts.setText(productsText.toString().trim());
        } catch (JSONException e) {
            tvProducts.setText("Error loading products");
        }

        AlertDialog dialog = builder.create();
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ✅ Change Status Dialog
    private void showChangeStatusDialog(Order order, int position) {
        String[] statuses = {"Pending", "Confirmed", "Delivered", "Cancelled"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Order Status");
        builder.setItems(statuses, (dialog, which) -> {
            String newStatus = statuses[which];
            databaseHelper.updateOrderStatus(order.getId(), newStatus);
            order.setStatus(newStatus);
            adapter.notifyItemChanged(position);
            Toast.makeText(this, "Status updated to: " + newStatus, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // ✅ Delete Order Dialog
    private void showDeleteOrderDialog(Order order, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Order");
        builder.setMessage("Are you sure you want to delete Order #" + order.getId() + "?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            databaseHelper.deleteOrder(order.getId());
            orderList.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(this, "Order deleted", Toast.LENGTH_SHORT).show();

            // Update UI if no orders
            if (orderList.isEmpty()) {
                llNoOrders.setVisibility(View.VISIBLE);
                recyclerViewOrders.setVisibility(View.GONE);
                tvOrderCount.setVisibility(View.GONE);
            } else {
                tvOrderCount.setText("Total Orders: " + orderList.size());
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    // ✅ Orders RecyclerView Adapter
    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

        private List<Order> orders;

        public OrdersAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            holder.bind(orders.get(position), position);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvPhone, tvTotal, tvDate, tvStatus;
            Button btnViewDetails, btnChangeStatus, btnDelete;
            CardView cardOrder;

            public OrderViewHolder(View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvPhone = itemView.findViewById(R.id.tvOrderPhone);
                tvTotal = itemView.findViewById(R.id.tvOrderTotal);
                tvDate = itemView.findViewById(R.id.tvOrderDate);
                tvStatus = itemView.findViewById(R.id.tvOrderStatus);
                btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
                btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
                btnDelete = itemView.findViewById(R.id.btnDeleteOrder);
                cardOrder = itemView.findViewById(R.id.cardOrder);
            }

            public void bind(final Order order, final int position) {
                tvOrderId.setText("Order #" + order.getId());
                tvPhone.setText("Phone: " + order.getPhoneNumber());
                tvTotal.setText("Total: ৳ " + String.format("%.2f", order.getTotalPrice()));
                tvDate.setText(order.getOrderDate());
                tvStatus.setText(order.getStatus());

                // Status color
                switch (order.getStatus()) {
                    case "Pending":
                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                        break;
                    case "Confirmed":
                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                        break;
                    case "Delivered":
                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        break;
                    case "Cancelled":
                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        break;
                }

                btnViewDetails.setOnClickListener(v -> showOrderDetailsDialog(order));
                btnChangeStatus.setOnClickListener(v -> showChangeStatusDialog(order, position));
                btnDelete.setOnClickListener(v -> showDeleteOrderDialog(order, position));
            }
        }
    }
}