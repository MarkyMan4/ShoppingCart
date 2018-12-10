package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditItemActivity extends AppCompatActivity {

    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private TextView nameText, descriptionText, priceText;
    private Button descButton, priceButton, doneButton, deleteButton;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private String itemId;
    private Item item;
    private DataSnapshot data;
    private boolean deleted = false;
    private Spinner promoSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        nameText = findViewById(R.id.item_name);
        descriptionText = findViewById(R.id.desc_text);
        priceText = findViewById(R.id.price_text);
        descButton = findViewById(R.id.edit_desc_btn);
        priceButton = findViewById(R.id.edit_price_btn);
        doneButton = findViewById(R.id.done_btn);
        deleteButton = findViewById(R.id.delete_btn);
        promoSpinner = findViewById(R.id.available_promo_spinner);



        item = new Item();
        itemId = getIntent().getStringExtra("ID");

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
                data = dataSnapshot;
                if(!deleted) {
                    getItemData(data.child("items").child(itemId));
                    updateLabels();
                    populateSpinner(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEditPricePopup();
            }
        });

        descButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEditDescriptionPopup();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(promoSpinner.getSelectedItem().toString().equals("No Promotion") && data.child("items").child(itemId).hasChild("discountCode")) {
                    dbRef.child("items").child(itemId).child("discountCode").removeValue();
                }
                else {
                    dbRef.child("items").child(itemId).child("discountCode").setValue(promoSpinner.getSelectedItem().toString());
                }
                Intent intent = new Intent(EditItemActivity.this, AdminDashboard.class);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createConfirmDeletePopup();
            }
        });
    }

    private void populateSpinner(DataSnapshot dataSnapshot) {
        DataSnapshot promotions = dataSnapshot.child("itemDiscount");
        ArrayList<String> promos = new ArrayList<>();
        promos.add("No Promotion");
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = new Date();
        for(DataSnapshot ds : promotions.getChildren()) {
            try {
                Date start = df.parse(ds.child("startDate").getValue().toString());
                Date end = df.parse(ds.child("endDate").getValue().toString());
                if((start.before(today) || datesEqual(today, start)) && (end.after(today) || datesEqual(today, end))) {
                    promos.add(ds.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, promos);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                promoSpinner.setAdapter(adapter);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(dataSnapshot.child("items").child(itemId).hasChild("discountCode")) {
            String code = dataSnapshot.child("items").child(itemId).child("discountCode").getValue().toString();
            promoSpinner.setSelection(promos.indexOf(code));
        }
    }

    private boolean datesEqual(Date d1, Date d2) {
        return d1.getDate() == d2.getDate() && d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear();
    }

    private void getItemData(DataSnapshot dataSnapshot) {
        item.setId(itemId);
        item.setName(dataSnapshot.child("name").getValue().toString());
        item.setDescription(dataSnapshot.child("description").getValue().toString());
        item.setPrice(dataSnapshot.child("price").getValue().toString());
    }

    private void updateLabels() {
        nameText.setText(item.getName());
        double price = Double.parseDouble(item.getPrice());
        DecimalFormat df = new DecimalFormat("0.00");
        priceText.setText("$" + df.format(price));
        descriptionText.setText(item.getDescription());
    }

    private void createEditPricePopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_edit_price, null);

        final EditText newPriceText = view.findViewById(R.id.new_price_text);
        Button priceDoneBtn = view.findViewById(R.id.price_done_btn);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        priceDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!newPriceText.getText().toString().matches("")) {
                    double price = Double.parseDouble(newPriceText.getText().toString());
                    DecimalFormat df = new DecimalFormat("0.00");
                    dbRef.child("items").child(itemId).child("price").setValue(Double.parseDouble(df.format(price)));
                }
                dialog.hide();
            }
        });
    }

    private void createEditDescriptionPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_edit_description, null);

        final EditText newDescText = view.findViewById(R.id.new_desc_text);
        Button descDoneBtn = view.findViewById(R.id.desc_done_btn);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        descDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!newDescText.getText().toString().matches("")) {
                    dbRef.child("items").child(itemId).child("description").setValue(newDescText.getText().toString());
                }
                dialog.hide();
            }
        });
    }

    private void createConfirmDeletePopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_confirm_delete, null);

        Button yes = view.findViewById(R.id.yes_btn);
        Button no = view.findViewById(R.id.no_btn);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleted = true;
                dbRef.child("items").child(itemId).removeValue();
                for(DataSnapshot ds : data.child("shoppingCarts").getChildren()) {
                    if(ds.hasChild(itemId)) {
                        dbRef.child("shoppingCarts").child(ds.getKey()).child(itemId).removeValue();
                    }
                }
                Intent intent = new Intent(EditItemActivity.this, AdminDashboard.class);
                startActivity(intent);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
    }

    private void toastMessage(String msg) {
        Toast.makeText(EditItemActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }
}
