package com.example.myapplication;

import java.util.Date;

public class ItemDiscount {

    private String startDate;
    private String endDate;
    private int percent;

    public ItemDiscount(String startDate, String endDate, int percent) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.percent = percent;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
}
