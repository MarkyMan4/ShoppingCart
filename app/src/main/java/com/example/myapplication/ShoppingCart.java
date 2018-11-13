package com.example.myapplication;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ShoppingCart extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {
    ShoppingCartAdapter scAdapter;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private Button checkOut;
    private ArrayList<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference().child("items");

        /*dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                items = getData(dataSnapshot);
                searchItems = new ArrayList<>();
                for(Item i : items) {
                    searchItems.add(i);
                }
                doRecyclerView(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });*/

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

    private void updateRecyclerView(ArrayList<Item> newItems) {
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        scAdapter = new ShoppingCartAdapter(this, newItems);
        scAdapter.setClickListener(this);
        recyclerView.setAdapter(scAdapter);
    }

    private void doRecyclerView(DataSnapshot dataSnapshot){
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        scAdapter = new ShoppingCartAdapter(this, getData(dataSnapshot));
        scAdapter.setClickListener(this);
        recyclerView.setAdapter(scAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private ArrayList<Item> getData(DataSnapshot dataSnapshot) {
        items = new ArrayList<>();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            Item item = new Item();
            item.setName((String)ds.child("name").getValue());
            item.setDescription((String)ds.child("description").getValue());
            item.setPrice(ds.child("price").getValue() + "");
            item.setId(ds.getKey());
            items.add(item);
        }
        return items;
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