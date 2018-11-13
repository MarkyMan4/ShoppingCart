package com.example.myapplication;

import android.content.Intent;
import android.provider.ContactsContract;
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
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class BrowseActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {
    MyRecyclerViewAdapter rvAdapter;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private int itemIndex = 1;
    private FirebaseAuth.AuthStateListener authListener;
    private Button signOut, go;
    private EditText searchBar;
    private ArrayList<Item> items;
    private ArrayList<Item> searchItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference().child("items");
        signOut = findViewById(R.id.signout);
        go = findViewById(R.id.gobtn);
        searchBar = findViewById(R.id.searchtext);

        setSignOutButton();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                toastMessage("Signed Out");
                Intent intent = new Intent(BrowseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                items = getData(dataSnapshot);
                searchItems = new ArrayList<>();
                for(Item i : items) {
                    searchItems.add(i);
                }
                updateRecylcerView(searchItems);
                //doRecyclerView(dataSnapshot);
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

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = searchBar.getText().toString();
                searchItems = new ArrayList<>();
                for(Item item : items) {
                    if(item.getName().toLowerCase().contains(searchText.toLowerCase())) {
                        searchItems.add(item);
                    }
                }
                updateRecylcerView(searchItems);
            }
        });
    }

    private void setSignOutButton() {
        FirebaseUser user = auth.getCurrentUser();
        //if no user is signed in, the sign out button is appropriately renamed 'Exit'
        if(user==null){
            signOut.setText("Exit");
        }
    }

    private void updateRecylcerView(ArrayList<Item> newItems) {
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter = new MyRecyclerViewAdapter(this, newItems);
        rvAdapter.setClickListener(this);
        recyclerView.setAdapter(rvAdapter);
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
        Item item = searchItems.get(position);
        Intent intent = new Intent(BrowseActivity.this, ItemDetailActivity.class);
        intent.putExtra("ID", item.getId());
        startActivity(intent);
    }

    private void toastMessage(String msg) {
        Toast.makeText(BrowseActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
