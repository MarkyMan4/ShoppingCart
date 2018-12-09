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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

//Activity for browsing items in the store.
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
    private ImageView cartIcon;
    private boolean isGuest = false;
    private HashMap<String, Integer> guestCart;

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
        cartIcon = findViewById(R.id.viewCart);

        if(auth.getCurrentUser() == null) {
            isGuest = true;
        }

        if(getIntent().hasExtra("cart")) {
            guestCart = (HashMap<String, Integer>) getIntent().getSerializableExtra("cart");
        }
        else {
            guestCart = new HashMap<>();
        }

        setSignOutButton();

        //sets click listener for sign out button.
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

        //event listener for data changes in the database. Runs if data changes updating what is
        //displayed in the app.
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
                doRecyclerView(dataSnapshot);
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

        //sets click listener for go button. This button searches for items that contain the string
        //in the search bar.
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
                updateRecyclerView(searchItems);
            }
        });

        //sets click listener for cart icon. When pressed it takes you to a screen displaying your
        //shopping cart.
        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BrowseActivity.this, ShoppingCart.class);
                if(isGuest)
                    intent.putExtra("cart", guestCart);
                startActivity(intent);
            }
        });
    }

    //Method that changes the display based is the user is a guest. Changes sign out button to say
    //exit and removes the profile button since guests don't have a profile.
    private void setSignOutButton() {
        FirebaseUser user = auth.getCurrentUser();
        //if no user is signed in, the sign out button is appropriately renamed 'Exit'
        if(user==null){
            signOut.setText("Exit");
            Button profile = findViewById(R.id.userProfile);
            profile.setVisibility(View.INVISIBLE);
        }
    }

    //updates what is displayed in the recycler view.
    private void updateRecyclerView(ArrayList<Item> newItems) {
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter = new MyRecyclerViewAdapter(this, newItems);
        rvAdapter.setClickListener(this);
        recyclerView.setAdapter(rvAdapter);
    }

    //initially fills the recycler view for when the activity is first displayed.
    private void doRecyclerView(DataSnapshot dataSnapshot){
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter = new MyRecyclerViewAdapter(this, getData(dataSnapshot));
        rvAdapter.setClickListener(this);
        recyclerView.setAdapter(rvAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    //method that fills the items array with item instances with data in the database.
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

    //method that is called when an item in the recycler view is clicked. Starts an activity to
    //display the details of the clicked item.
    @Override
    public void onItemClick(View view, int position) {
        Item item = searchItems.get(position);
        Intent intent = new Intent(BrowseActivity.this, ItemDetailActivity.class);
        intent.putExtra("ID", item.getId());
        if(isGuest)
            intent.putExtra("cart", guestCart);
        startActivity(intent);
    }

    //generic method to make a toast message.
    private void toastMessage(String msg) {
        Toast.makeText(BrowseActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    //Method that is called when the profile button is clicked. Starts an activity that displays the
    //profile of the currently signed in user.
    public void viewProfile(View view) {
        Intent intent = new Intent(BrowseActivity.this, HistoryActivity.class);
        startActivity(intent);
    }
}
