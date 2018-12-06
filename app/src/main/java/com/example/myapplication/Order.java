package com.example.myapplication;

import java.util.ArrayList;
import java.util.Date;

public class Order {

    private String orderId;
    private Date date;
    private ArrayList<HistoryItem> histItems;

    public Order(String orderId, Date date, ArrayList<HistoryItem> histItems) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<HistoryItem> getHistItems() {
        return histItems;
    }

    public void setHistItems(ArrayList<HistoryItem> histItems) {
        this.histItems = histItems;
    }
}
