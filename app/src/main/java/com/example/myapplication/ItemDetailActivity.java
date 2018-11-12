package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView title, priceLabel, description;
    private EditText quantity;
    private Button addToCart;

    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DataSnapshot data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();

        title = findViewById(R.id.itemname);
        priceLabel = findViewById(R.id.pricelabel);
        description = findViewById(R.id.desclabel);
        quantity = findViewById(R.id.editquantity);
        addToCart = findViewById(R.id.addbtn);
        final String ID = getIntent().getStringExtra("ID");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                System.out.println("******************************************");
                data = dataSnapshot;
                Item item = getItemData(ID);
                title.setText(item.getName());
                priceLabel.setText("Price: $" + Double.parseDouble(item.getPrice()));
                description.setText(item.getDescription());
                quantity.setText("0");
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

    private Item getItemData(String itemId) {
        Item item = new Item();
        DataSnapshot itemData = data.child("items").child(itemId);
        item.setName(itemData.child("name").getValue().toString());
        item.setDescription(itemData.child("description").getValue().toString());
        item.setPrice(itemData.child("price").getValue().toString());
        return item;
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    private void toastMessage(String msg) {
        Toast.makeText(ItemDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
