package com.example.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private ArrayList<Order> orders;
    private ArrayList<HistoryItem> historyItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        orders = new ArrayList<>();
        historyItems = new ArrayList<>();

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
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            String orderId = ds.getKey();

            String id = ds.getKey();
            String description = ds.child("description").getValue().toString();
            String name = ds.child("name").getValue().toString();
            double price = Double.parseDouble(ds.child("price").getValue().toString());
            int quantity = Integer.parseInt(ds.child("quantity").getValue().toString());
            HistoryItem historyItem = new HistoryItem(description, name, price, quantity);
            historyItem.setItemId(id);
            historyItems.add(historyItem);
        }
    }

    private void doRecyclerView() {
        //code to create and populate recycler view goes here...
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }
}
