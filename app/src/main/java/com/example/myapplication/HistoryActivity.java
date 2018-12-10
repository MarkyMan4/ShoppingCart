package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.text.DecimalFormat;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements OrderHistRows.ItemClickListener, PopupOrderHistRows.ItemClickListener {

    private OrderHistRows orderAdapter;
    private PopupOrderHistRows histPopupAdapter;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private ArrayList<Order> orders;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private String billingAddr, billingCity, billingState, billingZip, shippingAddr, shippingCity, shippingState, shippingZip;
    private String expiration, nameOnCard, cardNumber;
    private boolean histLoaded = false;
    private TextView historyLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        orders = new ArrayList<>();
        historyLabel = findViewById(R.id.orderHistory);

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
                getSavedInfo(dataSnapshot);
                historyLabel.setText("History For " + dataSnapshot.child("users").child(auth.getCurrentUser().getUid()).child("first").getValue().toString());
                if(!histLoaded) {
                    getHistory(dataSnapshot.child("purchaseHistory").child(auth.getCurrentUser().getUid()));
                    doRecyclerView();
                    histLoaded = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getHistory(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            String orderId = ds.getKey();
            String date = ds.child("date").getValue().toString();
            DecimalFormat df = new DecimalFormat("0.00");
            String total = df.format(Double.parseDouble(ds.child("total").getValue().toString()));
            ArrayList<HistoryItem> historyItems = new ArrayList<>();
            for(DataSnapshot d : ds.getChildren()) {
                if(!d.getKey().equals("date") && !d.getKey().equals("total")) {
                    String id = d.getKey();
                    String description = d.child("description").getValue().toString();
                    String name = d.child("name").getValue().toString();
                    double price = Double.parseDouble(d.child("price").getValue().toString());
                    int quantity = Integer.parseInt(d.child("quantity").getValue().toString());
                    HistoryItem historyItem = new HistoryItem(description, name, price, quantity);
                    historyItem.setItemId(id);
                    historyItems.add(historyItem);
                }
            }
            orders.add(new Order(orderId, date, total, historyItems));
        }
    }

    private void doRecyclerView() {
        //code to create and populate recycler view goes here...
        RecyclerView recyclerView = findViewById(R.id.order_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        orderAdapter = new OrderHistRows(this, orders);
        orderAdapter.setClickListener(this);
        recyclerView.setAdapter(orderAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(HistoryActivity.this, OrderHistoryItemActivity.class);
        intent.putExtra("id", orders.get(position).getOrderId());
        startActivity(intent);
    }

    public void shippingClick(View view) {
        createShippingPopup();
    }

    public void paymentClick(View view) {
        createPaymentPopup();
    }

    public void billingClick(View view) {
        createBillingPopup();
    }

    private void createShippingPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_shipping_info, null);
        final EditText addrInput = view.findViewById(R.id.addr_input);
        final EditText cityInput = view.findViewById(R.id.city_input);
        final EditText stateInput = view.findViewById(R.id.state_input);
        final EditText zipInput = view.findViewById(R.id.zip_input);
        Button done = view.findViewById(R.id.shipping_done_btn);

        addrInput.setText(shippingAddr);
        cityInput.setText(shippingCity);
        stateInput.setText(shippingState);
        zipInput.setText(shippingZip);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shippingAddr = addrInput.getText().toString();
                shippingCity = cityInput.getText().toString();
                shippingState = stateInput.getText().toString();
                shippingZip = zipInput.getText().toString();
                if(shippingAddr.matches("") || shippingCity.matches("") || shippingState.matches("") || shippingZip.matches("")) {
                    toastMessage("Please fill in all the required fields");
                }
                else {
                    DatabaseReference billingInfo = dbRef.child("userInfo").child(auth.getCurrentUser().getUid()).child("shippingAddress");
                    billingInfo.child("Street").setValue(shippingAddr);
                    billingInfo.child("City").setValue(shippingCity);
                    billingInfo.child("State").setValue(shippingState);
                    billingInfo.child("Zip").setValue(Integer.parseInt(shippingZip));
                    dialog.hide();
                }
            }
        });
    }

    private void createPaymentPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_payment_info, null);

        final EditText numberInput = view.findViewById(R.id.number_input);
        final EditText nameInput = view.findViewById(R.id.name_input);
        final EditText expirationInput = view.findViewById(R.id.date_input);
        Button done = view.findViewById(R.id.payment_done_btn);
        final CheckBox saveBox = view.findViewById(R.id.save_box);

        numberInput.setText(cardNumber);
        nameInput.setText(nameOnCard);
        expirationInput.setText(expiration);

        saveBox.setVisibility(View.INVISIBLE);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardNumber = numberInput.getText().toString();
                nameOnCard = nameInput.getText().toString();
                expiration = expirationInput.getText().toString();

                if(cardNumber.matches("") || nameOnCard.matches("") || expiration.matches("")) {
                    toastMessage("Please fill in all the required fields");
                }
                else {
                    DatabaseReference paymentInfo = dbRef.child("userInfo").child(auth.getCurrentUser().getUid()).child("creditCard");
                    paymentInfo.child("Expiration").setValue(expiration);
                    paymentInfo.child("NameOnCard").setValue(nameOnCard);
                    paymentInfo.child("CardNumber").setValue(cardNumber);

                    dialog.hide();
                }
            }
        });
    }

    private void createBillingPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_shipping_info, null);
        final EditText addrInput = view.findViewById(R.id.addr_input);
        final EditText cityInput = view.findViewById(R.id.city_input);
        final EditText stateInput = view.findViewById(R.id.state_input);
        final EditText zipInput = view.findViewById(R.id.zip_input);
        Button done = view.findViewById(R.id.shipping_done_btn);

        addrInput.setText(billingAddr);
        cityInput.setText(billingCity);
        stateInput.setText(billingState);
        zipInput.setText(billingZip);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billingAddr = addrInput.getText().toString();
                billingCity = cityInput.getText().toString();
                billingState = stateInput.getText().toString();
                billingZip = zipInput.getText().toString();
                if(billingAddr.matches("") || billingCity.matches("") || billingState.matches("") || billingZip.matches("")) {
                    toastMessage("Please fill in all the required fields");
                }
                else {
                    DatabaseReference billingInfo = dbRef.child("userInfo").child(auth.getCurrentUser().getUid()).child("billingAddress");
                    billingInfo.child("Street").setValue(billingAddr);
                    billingInfo.child("City").setValue(billingCity);
                    billingInfo.child("State").setValue(billingState);
                    billingInfo.child("Zip").setValue(Integer.parseInt(billingZip));
                    dialog.hide();
                }
            }
        });
    }

    private void getSavedInfo(DataSnapshot snapshot) {
        if(snapshot.hasChild("userInfo")) {
            DataSnapshot data = snapshot.child("userInfo");
            if(data.hasChild(auth.getCurrentUser().getUid())) {
                DataSnapshot billingData = data.child(auth.getCurrentUser().getUid()).child("billingAddress");
                DataSnapshot shippingData  = data.child(auth.getCurrentUser().getUid()).child("shippingAddress");

                billingAddr = billingData.child("Street").getValue().toString();
                billingCity = billingData.child("City").getValue().toString();
                billingState = billingData.child("State").getValue().toString();
                billingZip = billingData.child("Zip").getValue().toString();

                shippingAddr = shippingData.child("Street").getValue().toString();
                shippingCity = shippingData.child("City").getValue().toString();
                shippingState = shippingData.child("State").getValue().toString();
                shippingZip = shippingData.child("Zip").getValue().toString();

                if(data.child(auth.getCurrentUser().getUid()).hasChild("creditCard")) {
                    DataSnapshot paymentData = data.child(auth.getCurrentUser().getUid()).child("creditCard");
                    cardNumber = paymentData.child("CardNumber").getValue().toString();
                    expiration = paymentData.child("Expiration").getValue().toString();
                    nameOnCard = paymentData.child("NameOnCard").getValue().toString();

                }
            }
        }
    }

    private void toastMessage(String msg) {
        Toast.makeText(HistoryActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
