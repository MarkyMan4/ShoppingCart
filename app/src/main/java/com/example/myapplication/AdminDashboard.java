package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AdminDashboard extends AppCompatActivity {

    private Button signOut;
    private Button addCode;
    private DataSnapshot data;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private FirebaseDatabase fDatabase;
    private Button next;
    private Button close;
    private Button finish;
    private Button cancel;
    private Spinner promoSpinner;
    private Spinner itemSpinner;
    private EditText percentInput;
    private EditText startDate;
    private EditText endDate;
    private EditText codeName;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        signOut = findViewById(R.id.signout);
        addCode = findViewById(R.id.addcode);
        mAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                toastMessage("Signed Out");
                Intent intent = new Intent(AdminDashboard.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPromotionDialog();
            }
        });

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data = dataSnapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void selectPromotionDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_code_popup, null);
        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        promoSpinner = view.findViewById(R.id.selectpromotion);
        DataSnapshot promotions = data.child("promotion");
        ArrayList<String> items = new ArrayList<>();
        for(DataSnapshot ds : promotions.getChildren()) {
            items.add((String)ds.child("name").getValue());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promoSpinner.setAdapter(adapter);

        next = view.findViewById(R.id.nextbtn);
        close = view.findViewById(R.id.closebtn);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                String promotion = promoSpinner.getSelectedItem().toString();
                if(promotion.equals("Item % Discount"))
                    createItemPercentPopup();
            }
        });
    }

    private void createItemPercentPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.item_percent_popup, null);
        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        itemSpinner = view.findViewById(R.id.itemspinner);
        percentInput = view.findViewById(R.id.percentinput);
        finish = view.findViewById(R.id.finishbtn);
        cancel = view.findViewById(R.id.cancelbtn);
        startDate = view.findViewById(R.id.start);
        endDate = view.findViewById(R.id.end);
        codeName = view.findViewById(R.id.name);

        DataSnapshot items = data.child("items");
        final ArrayList<String> itemList = new ArrayList<>(); //make sure these being final doesn't mess anything up
        final ArrayList<Integer> itemIDs = new ArrayList<>();
        for(DataSnapshot ds : items.getChildren()) {
            itemList.add((String)ds.child("name").getValue());
            itemIDs.add(Integer.parseInt(ds.getKey()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(adapter);

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(codeName.getText().toString().matches("") ||percentInput.getText().toString().matches("")
                        || startDate.getText().toString().matches("") || endDate.getText().toString().matches("")) {
                    toastMessage("Complete All Fields");
                }
                else {
                    String name = codeName.getText().toString();
                    String item = itemSpinner.getSelectedItem().toString();
                    String id = itemIDs.get(itemList.indexOf(item)).toString();
                    int percent = Integer.parseInt(percentInput.getText().toString());
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    String start = startDate.getText().toString();
                    String end = endDate.getText().toString();
                    ItemDiscount discount = new ItemDiscount(start, end, percent);
                    dbRef.child("itemDiscount").child(name).setValue(discount);
                    dbRef.child("items").child(id).child("discountCode").setValue(name);
                    //dbRef.child("items").child(id).child("discount").setValue(percent);
                    dialog.hide();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void toastMessage(String msg) {
        Toast.makeText(AdminDashboard.this, msg, Toast.LENGTH_SHORT).show();
    }
}