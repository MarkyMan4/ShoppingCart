package com.example.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class OrderHistRows extends RecyclerView.Adapter<OrderHistRows.ViewHolder> {

    private List<Order> OrderList;
    private LayoutInflater myInflater;
    private ItemClickListener clickListener;

    // data is passed into the constructor
    OrderHistRows(Context context, List<Order> data) {
        this.myInflater = LayoutInflater.from(context);
        this.OrderList = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = myInflater.inflate(R.layout.activity_order_hist_rows, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String orderId = OrderList.get(position).getOrderId();
        String orderDate = OrderList.get(position).getDate().toString();
        holder.orderIdField.setText(orderId);
        holder.orderDateField.setText(orderDate);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return OrderList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView orderIdField, orderDateField;

        ViewHolder(View itemView) {
            super(itemView);
            orderIdField = itemView.findViewById(R.id.orderIDField);
            orderDateField = itemView.findViewById(R.id.orderDateField);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}