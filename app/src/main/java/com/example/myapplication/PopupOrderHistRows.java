package com.example.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class PopupOrderHistRows extends RecyclerView.Adapter<PopupOrderHistRows.ViewHolder>{

    private List<HistoryItem> itemList;
    private LayoutInflater myInflater;
    private PopupOrderHistRows.ItemClickListener clickListener;

    // data is passed into the constructor
    PopupOrderHistRows(Context context, List<HistoryItem> data) {
        this.myInflater = LayoutInflater.from(context);
        this.itemList = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public PopupOrderHistRows.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = myInflater.inflate(R.layout.popup_order_hist_rows, parent, false);
        return new PopupOrderHistRows.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(PopupOrderHistRows.ViewHolder holder, int position) {
        String iName = itemList.get(position).getName();
        String iDescription = itemList.get(position).getDescription();
        String iPrice = (itemList.get(position).getPrice() * itemList.get(position).getQuantity()) + "";
        String iQty = itemList.get(position).getQuantity() + "";
        DecimalFormat df = new DecimalFormat("0.00");
        holder.iNameTextView.setText(iName);
        holder.iDescriptionTextView.setText(iDescription);
        holder.iPriceTextView.setText("$" + df.format(Double.parseDouble(iPrice)));
        holder.iQuantityTextView.setText("Quantity Purchased: " + iQty);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return itemList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView iNameTextView, iDescriptionTextView, iPriceTextView, iQuantityTextView;

        ViewHolder(View itemView) {
            super(itemView);
            iNameTextView = itemView.findViewById(R.id.iName);
            iDescriptionTextView = itemView.findViewById(R.id.iDesc);
            iPriceTextView = itemView.findViewById(R.id.iPrice);
            iQuantityTextView = itemView.findViewById(R.id.iQty);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(PopupOrderHistRows.ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
