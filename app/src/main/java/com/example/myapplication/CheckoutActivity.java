package com.example.myapplication;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//activity for checkout page.
public class CheckoutActivity extends AppCompatActivity {

    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private LinearLayout firstScreen;
    private LinearLayout secondScreen;
    private Button billingBtn, shippingBtn, paymentBtn, placeOrder, backToBrowse, historyButton;
    private ImageView billingCheck, shippingCheck, paymentCheck;
    private TextView subtotalText, taxText, shippingText, totalText;
    private boolean billingDone, shippingDone, paymentDone;
    private String billingAddr, billingCity, billingState, billingZip, shippingAddr, shippingCity, shippingState, shippingZip = "";
    private String expiration, nameOnCard, cardNumber = "";
    private boolean isGuest = false;
    private DataSnapshot snapshot;
    private final double shippingCost = 5.0;
    private double subtotal = 0.0;
    private double tax = 0.0;
    private boolean dataSaved = false;
    private ArrayList<HistoryItem> historyItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        firstScreen = findViewById(R.id.firstLayout);
        secondScreen = findViewById(R.id.secondLayout);
        billingBtn = findViewById(R.id.billingInfoBtn);
        shippingBtn = findViewById(R.id.shippingInfoBtn);
        paymentBtn = findViewById(R.id.paymentInfoBtn);
        placeOrder = findViewById(R.id.placeOrderBtn);
        backToBrowse = findViewById(R.id.backBtn);
        historyButton = findViewById(R.id.history_btn);
        billingCheck = findViewById(R.id.billingCheckImg);
        shippingCheck = findViewById(R.id.shippingCheckImg);
        paymentCheck = findViewById(R.id.paymentCheckImg);
        subtotalText = findViewById(R.id.subtotal_text);
        taxText = findViewById(R.id.tax_text);
        shippingText = findViewById(R.id.shipping_text);
        totalText = findViewById(R.id.total_text);
        shippingState = "";
        billingState = "";
        historyItems = new ArrayList<>();

        if(auth.getCurrentUser() == null) {
            isGuest = true;
            subtotal = getIntent().getDoubleExtra("total", 0.0);
            if(!billingState.matches("")) {
                tax = subtotal * getSalesTax(billingState);
            }
            updateLabels(subtotal, tax, shippingCost, subtotal + tax + shippingCost);
        }

        if(isGuest) {
            historyButton.setVisibility(View.INVISIBLE);
        }

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

        //event listener for data change in the database. Updates displayed information.
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                snapshot = dataSnapshot;
                if(!isGuest) {
                    if(!dataSaved)
                        getSavedInfo();
                    subtotal = getIntent().getDoubleExtra("total", 0.0);
                    if(!billingState.matches("")) {
                        tax = subtotal * getSalesTax(billingState);
                    }
                    updateLabels(subtotal, tax, shippingCost, subtotal + tax + shippingCost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sets click listener for billing button. Lunches billing popup.
        billingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBillingPopup();
            }
        });

        //sets click listener for shipping button. Lunches shipping popup.
        shippingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createShippingPopup();
            }
        });

        //sets click listener for payment button. Lunches payment popup.
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPaymentPopup();
            }
        });

        ///sets click listener for place order button. If the proper information has been added it,
        //submits the order.
        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // first make sure all information has been entered
                if(billingCheck.getVisibility() == View.VISIBLE && shippingCheck.getVisibility() == View.VISIBLE && paymentCheck.getVisibility() == View.VISIBLE) {
                    if(!isGuest) {
                        DataSnapshot cart = snapshot.child("shoppingCarts").child(auth.getCurrentUser().getUid());
                        for(DataSnapshot ds : cart.getChildren()) {
                            String id = ds.getKey();
                            int quantity = Integer.parseInt(ds.child("quantity").getValue().toString());
                            String description = snapshot.child("items").child(id).child("description").getValue().toString();
                            String name = snapshot.child("items").child(id).child("name").getValue().toString();
                            double price = Double.parseDouble(snapshot.child("items").child(id).child("price").getValue().toString());
                            HistoryItem histItem = new HistoryItem(description, name, price, quantity);
                            histItem.setItemId(id);
                            historyItems.add(histItem);
                        }
                        String orderId = auth.getCurrentUser().getUid();
                        if(snapshot.child("purchaseHistory").hasChild(auth.getCurrentUser().getUid())) {
                            orderId = snapshot.child("purchaseHistory").child(auth.getCurrentUser().getUid()).getChildrenCount() + orderId;
                        }
                        else {
                            orderId = "0" + orderId;
                        }
                        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        dbRef.child("purchaseHistory").child(auth.getCurrentUser().getUid()).child(orderId).child("date").setValue(df.format(new Date()));
                        for(HistoryItem hi : historyItems) {
                            dbRef.child("purchaseHistory").child(auth.getCurrentUser().getUid()).child(orderId).child(hi.getItemId()).setValue(hi);
                        }
                        dbRef.child("purchaseHistory").child(auth.getCurrentUser().getUid()).child(orderId).child("total").setValue(Double.parseDouble(totalText.getText().toString().substring(1)));
                        dbRef.child("shoppingCarts").child(auth.getCurrentUser().getUid()).removeValue();
                    }
                    firstScreen.setVisibility(View.INVISIBLE);
                    secondScreen.setVisibility(View.VISIBLE);
                }
                else {
                    toastMessage("Enter all required information");
                }
            }
        });

        //sets click listener for back button. Takes user back to browse activity.
        backToBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckoutActivity.this, BrowseActivity.class);
                startActivity(intent);
            }
        });

        //sets click listener for history button. Takes user to there profile page.
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckoutActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    //method that updates the price information being displayed.
    private void updateLabels(double subtotal, double tax, double shipping, double total) {
        DecimalFormat df = new DecimalFormat("0.00");
        subtotalText.setText("$" + df.format(subtotal));
        taxText.setText("$" + df.format(tax));
        shippingText.setText("$" + df.format(shipping));
        totalText.setText("$" + df.format(total));
    }

    //method that creates the billing popup.
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
                    if(getSalesTax(billingState) < 0) {
                        toastMessage("Enter a valid state");
                    }
                    else {
                        if (!isGuest) {
                            DatabaseReference billingInfo = dbRef.child("userInfo").child(auth.getCurrentUser().getUid()).child("billingAddress");
                            billingInfo.child("Street").setValue(billingAddr);
                            billingInfo.child("City").setValue(billingCity);
                            billingInfo.child("State").setValue(billingState);
                            billingInfo.child("Zip").setValue(Integer.parseInt(billingZip));
                        }
                        billingCheck.setVisibility(View.VISIBLE);
                        dataSaved = true;
                        dialog.hide();
                    }
                }
            }
        });
    }

    //method that creates the shipping popup.
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
                    if(getSalesTax(shippingState) < 0) {
                        toastMessage("Enter a valid state");
                    }
                    else {
                        if (!isGuest) {
                            DatabaseReference billingInfo = dbRef.child("userInfo").child(auth.getCurrentUser().getUid()).child("shippingAddress");
                            billingInfo.child("Street").setValue(shippingAddr);
                            billingInfo.child("City").setValue(shippingCity);
                            billingInfo.child("State").setValue(shippingState);
                            billingInfo.child("Zip").setValue(Integer.parseInt(shippingZip));
                        }
                        tax = subtotal * getSalesTax(billingState);
                        updateLabels(subtotal, tax, shippingCost, subtotal + tax + shippingCost);
                        shippingCheck.setVisibility(View.VISIBLE);
                        dataSaved = true;
                        dialog.hide();
                    }
                }
            }
        });
    }

    //method that creates the payment popup.
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

        if(isGuest) {
            saveBox.setClickable(false);
        }

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
                    if(!isGuest && saveBox.isChecked()) {
                        DatabaseReference paymentInfo = dbRef.child("userInfo").child(auth.getCurrentUser().getUid()).child("creditCard");
                        paymentInfo.child("Expiration").setValue(expiration);
                        paymentInfo.child("NameOnCard").setValue(nameOnCard);
                        paymentInfo.child("CardNumber").setValue(cardNumber);
                    }
                    paymentCheck.setVisibility(View.VISIBLE);
                    dataSaved = true;
                    dialog.hide();
                }
            }
        });
    }

    //method that gets saved information for the signed in users billing address, shipping address,
    // and credit card.
    private void getSavedInfo() {
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

    //method that returns the sales tax rate for the state passed in.
    private double getSalesTax(String state) {
        double tax = -1.0;
        state = state.toUpperCase();
        switch (state) {
            case "AL" : tax = 4.0; break;
            case "AK" : tax = 0.0; break;
            case "AZ" : tax = 5.6; break;
            case "AR" : tax = 6.5; break;
            case "CA" : tax = 7.25; break;
            case "CO" : tax = 2.9; break;
            case "CT" : tax = 6.35; break;
            case "DE" : tax = 0.0; break;
            case "FL" : tax = 6.0; break;
            case "GA" : tax = 4.0; break;
            case "HI" : tax = 4.0; break;
            case "ID" : tax = 6.0; break;
            case "IL" : tax = 6.25; break;
            case "IN" : tax = 7.0; break;
            case "IA" : tax = 6.0; break;
            case "KS" : tax = 6.5; break;
            case "KY" : tax = 6.0; break;
            case "LA" : tax = 4.45; break;
            case "ME" : tax = 5.5; break;
            case "MD" : tax = 6.0; break;
            case "MA" : tax = 6.25; break;
            case "MI" : tax = 6.0; break;
            case "MN" : tax = 6.875; break;
            case "MS" : tax = 7.0; break;
            case "MO" : tax = 4.225; break;
            case "MT" : tax = 0.0; break;
            case "NE" : tax = 5.5; break;
            case "NV" : tax = 6.85; break;
            case "NH" : tax = 0.0; break;
            case "NJ" : tax = 6.625; break;
            case "NM" : tax = 5.125; break;
            case "NY" : tax = 4.0; break;
            case "NC" : tax = 4.75; break;
            case "ND" : tax = 5.0; break;
            case "OH" : tax = 5.75; break;
            case "OK" : tax = 4.5; break;
            case "OR" : tax = 0.0; break;
            case "PA" : tax = 6.0; break;
            case "RI" : tax = 7.0; break;
            case "SC" : tax = 6.0; break;
            case "SD" : tax = 4.5; break;
            case "TN" : tax = 7.0; break;
            case "TX" : tax = 6.25; break;
            case "UT" : tax = 5.95; break;
            case "VT" : tax = 6.0; break;
            case "VA" : tax = 5.3; break;
            case "WA" : tax = 6.5; break;
            case "WV" : tax = 6.0; break;
            case "WI" : tax = 5.0; break;
            case "WY" : tax = 4.0; break;
        }
        return tax / 100.0;
    }

    //generic method to make a toast message.
    private void toastMessage(String msg) {
        Toast.makeText(CheckoutActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    //method that takes the user back to the shopping cart activity without placing their order.
    public void backClicked(View view) {
        Intent intent = new Intent(CheckoutActivity.this, ShoppingCart.class);
        startActivity(intent);
    }
}
