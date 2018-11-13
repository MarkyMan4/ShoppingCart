package com.example.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

    private List<Item> cartList;
    private LayoutInflater myInflater;
    private ItemClickListener clickListener;

    // data is passed into the constructor
    ShoppingCartAdapter(Context context, List<Item> data) {
        this.myInflater = LayoutInflater.from(context);
        this.cartList = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = myInflater.inflate(R.layout.activity_shopping_cart_rows, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String itemName = cartList.get(position).getName();
        //need to get quantity
        String itemQuantity = cartList.get(position).;
        String itemPrice = cartList.get(position).getPrice();
        DecimalFormat df = new DecimalFormat("0.00");
        holder.itemNameTextView.setText(itemName);
        holder.itemQuantityTextView.setText(itemQuantity);
        holder.itemPriceTextView.setText("$" + df.format(Double.parseDouble(itemPrice)));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return cartList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView itemNameTextView, itemQuantityTextView, itemPriceTextView;;

        ViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.item);
            itemQuantityTextView = itemView.findViewById(R.id.itemQuantity);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return cartList.get(id).getName();
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