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

    private List<ShoppingCartItem> cartList;
    private LayoutInflater myInflater;
    private ItemClickListener clickListener;

    // data is passed into the constructor
    ShoppingCartAdapter(Context context, List<ShoppingCartItem> data) {
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
        String itemName = cartList.get(position).getItem().getName();
        String itemQuantity = "" + cartList.get(position).getQuantity();
        String itemPrice = cartList.get(position).getItem().getPrice();
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
            itemNameTextView = itemView.findViewById(R.id.cartItemName);
            itemQuantityTextView = itemView.findViewById(R.id.itemQuantity);
            itemPriceTextView = itemView.findViewById(R.id.cartItemPrice);
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