package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class OrderHistoryItemActivity  extends AppCompatActivity implements PopupOrderHistRows.ItemClickListener{

    private PopupOrderHistRows itemHistAdapter;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private ArrayList<HistoryItem> histItems;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_item);

        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        histItems = new ArrayList<>();
        orderId = getIntent().getStringExtra("id");

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
                getHistItems(dataSnapshot.child("purchaseHistory").child(auth.getCurrentUser().getUid()).child(orderId));
                doRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getHistItems(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            if(!ds.getKey().equals("date") && !ds.getKey().equals("total")) {
                String description = ds.child("description").getValue().toString();
                String name = ds.child("name").getValue().toString();
                double price = Double.parseDouble(ds.child("price").getValue().toString());
                int quantity = Integer.parseInt(ds.child("quantity").getValue().toString());
                histItems.add(new HistoryItem(description, name, price, quantity));
            }
        }
    }

    private void doRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.hist_item_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        itemHistAdapter = new PopupOrderHistRows(this, histItems);
        itemHistAdapter.setClickListener(this);
        recyclerView.setAdapter(itemHistAdapter);
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

    }

    public void backClicked(View view) {
        finish();
//        Intent intent = new Intent(OrderHistoryItemActivity.this, HistoryActivity.class);
//        startActivity(intent);
    }
}
