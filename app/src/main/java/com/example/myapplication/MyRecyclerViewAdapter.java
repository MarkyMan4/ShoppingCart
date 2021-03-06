package com.example.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.List;

//adapter class for the recycler view in browse activity.
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<Item> itemList;
    private LayoutInflater myInflater;
    private ItemClickListener clickListener;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<Item> data) {
        this.myInflater = LayoutInflater.from(context);
        this.itemList = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = myInflater.inflate(R.layout.rvitems_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String itemName = itemList.get(position).getName();
        String itemDescription = itemList.get(position).getDescription();
        String itemPrice = itemList.get(position).getPrice();
        DecimalFormat df = new DecimalFormat("0.00");
        holder.itemNameTextView.setText(itemName);
        holder.itemDescriptionTextView.setText(itemDescription);
        holder.itemPriceTextView.setText("$" + df.format(Double.parseDouble(itemPrice)));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return itemList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView itemNameTextView, itemDescriptionTextView, itemPriceTextView;

        ViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemName);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescription);
            itemPriceTextView = itemView.findViewById(R.id.itemPrice);
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