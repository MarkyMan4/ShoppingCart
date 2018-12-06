package com.example.myapplication;

import java.util.ArrayList;
import java.util.Date;

public class Order {

    private String orderId;
    private String date;
    private ArrayList<HistoryItem> histItems;

    public Order(String orderId, String date, ArrayList<HistoryItem> histItems) {
        this.orderId = orderId;
        this.date = date;
        this.histItems = histItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<HistoryItem> getHistItems() {
        return histItems;
    }

    public void setHistItems(ArrayList<HistoryItem> histItems) {
        this.histItems = histItems;
    }
}
