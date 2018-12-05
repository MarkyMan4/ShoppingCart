package com.example.myapplication;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewItemActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private FirebaseDatabase fDatabase;
    private EditText itemNameInput, priceInput, descInput;
    private Spinner promoCodeSpinner;
    private Button doneBtn;
    private DataSnapshot data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        mAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        itemNameInput = findViewById(R.id.item_name_input);
        priceInput = findViewById(R.id.price_input);
        descInput = findViewById(R.id.desc_input);
        promoCodeSpinner = findViewById(R.id.promo_code_spinner);
        doneBtn = findViewById(R.id.add_item_done_btn);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data = dataSnapshot;
                populatePromotionSpinner(dataSnapshot.child("itemDiscount"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = itemNameInput.getText().toString();
                String price = priceInput.getText().toString();
                String desc = descInput.getText().toString();
                String promo = promoCodeSpinner.getSelectedItem().toString();
                if(itemName.matches("") || price.matches("") || desc.matches("")) {
                    toastMessage("Please enter all required fields");
                }
                else {
                    Item item = new Item(itemName, desc, price);
                    String key = data.child("nextItemKey").getValue().toString();
                    String nextKey = (Integer.parseInt(key) + 1) + "";
                    dbRef.child("nextItemKey").setValue(nextKey);
                    /*
                    dbRef.child("items").child(key).child("name").setValue(itemName);
                    dbRef.child("items").child(key).child("description").setValue(desc);
                    double priceValue = Double.parseDouble(price);
                    DecimalFormat df = new DecimalFormat("0.00");
                    dbRef.child("items").child(key).child("price").setValue(Double.parseDouble(df.format(priceValue)));
                    */

                    //only 'issue' is that price values get stored as strings, doesn't seem to affect anything yet
                    dbRef.child("items").child(key).setValue(item);
                    if(!promo.equals("-select-")) {
                        dbRef.child("items").child(key).child("discountCode").setValue(promo);
                    }
                    toastMessage("Item added successfully");
                    Intent intent = new Intent(NewItemActivity.this, AdminDashboard.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void populatePromotionSpinner(DataSnapshot dataSnapshot) {
        ArrayList<String> items = new ArrayList<>();
        items.add("-select-");
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = new Date();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            try {
                Date start = df.parse(ds.child("startDate").getValue().toString());
                Date end = df.parse(ds.child("endDate").getValue().toString());
                if((start.before(today) || datesEqual(today, start)) && (end.after(today) || datesEqual(today, end))) {
                    items.add(ds.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                promoCodeSpinner.setAdapter(adapter);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean datesEqual(Date d1, Date d2) {
        return d1.getDate() == d2.getDate() && d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void toastMessage(String msg) {
        Toast.makeText(NewItemActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
