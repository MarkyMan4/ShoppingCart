package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements OrderHistRows.ItemClickListener, PopupOrderHistRows.ItemClickListener {

    private OrderHistRows orderAdapter;
    private PopupOrderHistRows histPopupAdapter;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private ArrayList<Order> orders;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        orders = new ArrayList<>();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null){
                    //User signed in
                }else{
                    //user is signed out
                    Log.d("Here: ", "user not signed in");
                }
            }
        };

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getHistory(dataSnapshot.child("purchaseHistory").child(auth.getCurrentUser().getUid()));
                doRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getHistory(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            String orderId = ds.getKey();
            String date = ds.child("date").getValue().toString();
            ArrayList<HistoryItem> historyItems = new ArrayList<>();
            for(DataSnapshot d : ds.getChildren()) {
                if(!d.getKey().equals("date")) {
                    String id = d.getKey();
                    String description = d.child("description").getValue().toString();
                    String name = d.child("name").getValue().toString();
                    double price = Double.parseDouble(d.child("price").getValue().toString());
                    int quantity = Integer.parseInt(d.child("quantity").getValue().toString());
                    HistoryItem historyItem = new HistoryItem(description, name, price, quantity);
                    historyItem.setItemId(id);
                    historyItems.add(historyItem);
                }
            }
            orders.add(new Order(orderId, date, historyItems));
        }
    }

    private void doRecyclerView() {
        //code to create and populate recycler view goes here...
        RecyclerView recyclerView = findViewById(R.id.order_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        orderAdapter = new OrderHistRows(this, orders);
        orderAdapter.setClickListener(this);
        recyclerView.setAdapter(orderAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(HistoryActivity.this, OrderHistoryItemActivity.class);
        intent.putExtra("id", orders.get(position).getOrderId());
        startActivity(intent);
    }

    public void shippingClick(View view) {
        //TODO: code to launch edit shipping address popup
    }

    public void paymentClick(View view) {
        //TODO: code to launch edit payment card popup
    }

    public void billingClick(View view) {
    }
}
