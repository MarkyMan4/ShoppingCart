package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShoppingCart extends AppCompatActivity implements ShoppingCartAdapter.ItemClickListener {
    ShoppingCartAdapter scAdapter;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private Button checkOut;
    private ArrayList<ShoppingCartItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference().child("items");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                FirebaseUser user = auth.getCurrentUser();
                if(user.getUid() != null) {
                    getData(dataSnapshot.child("items"), dataSnapshot.child("shoppingCarts").child(user.getUid()));
                    doRecyclerView();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

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
    }

    private void updateRecyclerView(ArrayList<ShoppingCartItem> newItems) {
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        scAdapter = new ShoppingCartAdapter(this, newItems);
        scAdapter.setClickListener(this);
        recyclerView.setAdapter(scAdapter);
    }

    private void doRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        scAdapter = new ShoppingCartAdapter(this, items);
        scAdapter.setClickListener(this);
        recyclerView.setAdapter(scAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void getData(DataSnapshot itemData, DataSnapshot cartData) {
        items = new ArrayList<>();
        for(DataSnapshot ds : cartData.getChildren()) {
            Item item = new Item();
            String itemId = ds.getKey();
            item.setId(itemId);
            item.setName(itemData.child(itemId).child("name").getValue().toString());
            System.out.println(item.getName());
            item.setPrice(itemData.child(itemId).child("price").getValue().toString());
            item.setDescription(itemData.child(itemId).child("description").getValue().toString());
            int quantity = Integer.parseInt(ds.child("quantity").getValue().toString());
            items.add(new ShoppingCartItem(item, quantity));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    private void toastMessage(String msg) {
        Toast.makeText(ShoppingCart.this, msg, Toast.LENGTH_SHORT).show();
    }
}