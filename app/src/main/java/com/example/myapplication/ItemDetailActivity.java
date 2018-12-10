package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

//class that is the activity that displays the details for a specific item.
public class ItemDetailActivity extends AppCompatActivity {

    private TextView title, priceLabel, description;
    private EditText quantity;
    private Button addToCart, back;
    private ImageView productImage;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DataSnapshot data;
    private Item item;
    private boolean isGuest = false;
    private HashMap<String, Integer> guestCart;

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
        back = findViewById(R.id.backbtn);
        productImage = findViewById(R.id.prodImg);
        final String ID = getIntent().getStringExtra("ID");

        if(auth.getCurrentUser() == null) {
            isGuest = true;
        }

        if(isGuest) {
            guestCart = (HashMap<String, Integer>)getIntent().getSerializableExtra("cart");
        }

        //event listener for data change in the data base. Updates the display if data is changed in
        //the database.
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                data = dataSnapshot;
                item = getItemData(ID);
                if(item != null) {
                    title.setText(item.getName());
                    String itemPicName = item.getName().replaceAll("\\s", "");
                    itemPicName = itemPicName.toLowerCase();
                    int resID = getResources().getIdentifier(itemPicName, "drawable", getPackageName());
                    productImage.setImageResource(resID);
                    DecimalFormat df = new DecimalFormat("0.00");
                    priceLabel.setText("Price: $" + df.format(Double.parseDouble(item.getPrice())));
                    description.setText(item.getDescription());
                    quantity.setText("1");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        //click listener for add to cart button. Adds the amount that is in the quantity field of
        // the item whose page is currently displayed to the user's shopping cart.
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quant = 0;
                if(!quantity.getText().toString().matches(""))
                    quant = Integer.parseInt(quantity.getText().toString());
                if(quant > 0) {
                    if(isGuest) {
                        guestCart.put(item.getId(), quant);
                    }
                    else {
                        FirebaseUser user = auth.getCurrentUser();
                        dbRef.child("shoppingCarts").child(user.getUid()).child(item.getId()).child("quantity").setValue(quant);
                    }
                    toastMessage("Added " + quant + " " + item.getName() + " to your cart");
                }
                else {
                    toastMessage("Please Enter a Valid Quantity");
                }
            }
        });

        //click listener for the back button. Takes the user back to the browse activity.
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemDetailActivity.this, BrowseActivity.class);
                if(isGuest) {
                    intent.putExtra("cart", guestCart);
                }
                startActivity(intent);
                finish();
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

    //method that creates and returns an item object containing the data of the item specified by
    // the itemID parameter.
    private Item getItemData(String itemId) {
        Item item = new Item();
        if(data.child("items").hasChild(itemId)) {
            DataSnapshot itemData = data.child("items").child(itemId);
            item.setName(itemData.child("name").getValue().toString());
            item.setDescription(itemData.child("description").getValue().toString());
            item.setPrice(itemData.child("price").getValue().toString());
            item.setId(itemId);
            return item;
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    //generic method to make a toast method.
    private void toastMessage(String msg) {
        Toast.makeText(ItemDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
