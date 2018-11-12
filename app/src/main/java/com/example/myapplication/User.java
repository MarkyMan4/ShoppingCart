package com.example.myapplication;

public class User {
    private String email;
    private String first;
    private boolean isAdmin;
    private String last;

    public User() {
    }

    public User(String email, String first, String last) {
        this.email = email;
        this.first = first;
        this.last = last;
        isAdmin = false;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }
}
