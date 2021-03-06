package com.example.myapplication;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

//activity for displaying the users shopping cart.
public class ShoppingCart extends AppCompatActivity implements ShoppingCartAdapter.ItemClickListener {
    ShoppingCartAdapter scAdapter;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private Button checkOut, promoCodeBtn, back;
    private ArrayList<ShoppingCartItem> items;
    private TextView total, promoCode;
    private DataSnapshot snapshot;
    private HashMap<String, Double> itemDiscounts; //stores item ids and their associated discount
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private boolean isGuest = false;
    private boolean decorationsSet = false;
    private HashMap<String, Integer> guestCart;
    private double subTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        auth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference();
        total = findViewById(R.id.totalPrice);
        promoCodeBtn = findViewById(R.id.promoCodeBtn);
        promoCode = findViewById(R.id.promoCode);
        back = findViewById(R.id.backBtn);
        itemDiscounts = new HashMap<>();
        checkOut = findViewById(R.id.checkout);

        if(auth.getCurrentUser() == null) {
            isGuest = true;
            guestCart = (HashMap<String, Integer>) getIntent().getSerializableExtra("cart");
        }

        //event listener for data change in the database. Updates page if there is a change in data.
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                snapshot = dataSnapshot;
                FirebaseUser user = auth.getCurrentUser();
                if(!isGuest) {
                    getData(dataSnapshot.child("items"), dataSnapshot.child("shoppingCarts").child(user.getUid()));
                }
                else {
                    getGuestData(dataSnapshot.child("items"));
                }
                doRecyclerView();
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

        //sets click listener for promo code button. Validates and applies promo code to shopping
        //cart if valid.
        promoCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //code to get and validate promo code
                boolean isValid = true;
                double discount = 0.0;
                String inputPromoCode = promoCode.getText().toString();
                if (inputPromoCode.isEmpty()) {
                    toastMessage("Promo Code Not Valid");
                    return;
                }
                if (snapshot.child("itemDiscount").hasChild(inputPromoCode)) {
                    Date today = new Date(System.currentTimeMillis());
                    today.setHours(0);
                    today.setMinutes(0);
                    today.setSeconds(0);
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    try {
                        Date promoStart = df.parse(snapshot.child("itemDiscount").child(inputPromoCode).child("startDate").getValue().toString());
                        promoStart.setHours(0);
                        promoStart.setMinutes(0);
                        promoStart.setSeconds(0);
                        Date promoEnd = df.parse(snapshot.child("itemDiscount").child(inputPromoCode).child("endDate").getValue().toString());
                        promoEnd.setSeconds(1);
                        if(today.before(promoStart) || today.after(promoEnd)) {
                            isValid = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    discount = Double.parseDouble(snapshot.child("itemDiscount").child(inputPromoCode).child("percent").getValue().toString()) / 100.0;
                    ArrayList<String> itemIds = getItemIds();
                    DataSnapshot itemData = snapshot.child("items");
                    boolean foundCode = false;
                    for(String s : itemIds) {
                        if(itemData.child(s).hasChild("discountCode")) {
                            if(itemData.child(s).child("discountCode").getValue().toString().equals(inputPromoCode)) {
                                itemDiscounts.put(s, discount);
                                foundCode = true;
                            }
                        }
                    }
                    if(!foundCode)
                        isValid = false;
                }
                else {
                    isValid = false;
                }
                if(!isValid) {
                    toastMessage("Promo Code Not Valid");
                }
                else {
                    toastMessage("Promo Code Applied");
                    updatePrice();
                }
            }
        });

        //sets click listener for back button. Takes the user back to the browse screen.
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShoppingCart.this, BrowseActivity.class);
                if(isGuest) {
                    intent.putExtra("cart", guestCart);
                }
                startActivity(intent);
            }
        });

        //sets click listener for checkout button. Starts activity to checkout.
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCartEmpty()) {
                    Intent intent = new Intent(ShoppingCart.this, CheckoutActivity.class);
                    //pass cart total to checkout activity
                    intent.putExtra("total", subTotal);
                    startActivity(intent);
                }
                else {
                    toastMessage("Cannot proceed with an empty cart");
                }
            }
        });
    }

    /*
     * check if a user's shopping cart is empty so they cannot procede to checkout with an empty cart
     */
    private boolean isCartEmpty() {
        if(isGuest) {
            if(guestCart.isEmpty()) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if(items.isEmpty()) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    //gets the id of every item in the users shopping cart.
    private ArrayList<String> getItemIds() {
        ArrayList<String> itemIds = new ArrayList<>();
        for(ShoppingCartItem item : items) {
            itemIds.add(item.getItem().getId());
        }
        return itemIds;
    }

    private void doRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        scAdapter = new ShoppingCartAdapter(this, items);
        scAdapter.setClickListener(this);
        recyclerView.setAdapter(scAdapter);
        if(!decorationsSet) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            decorationsSet = true;
        }
        updatePrice();
    }

    //method that updates the price displayed in the shopping cart activity.
    private void updatePrice() {
        DecimalFormat df = new DecimalFormat("0.00");
        subTotal = 0;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i).getItem();
            double itemPrice = Double.parseDouble(item.getPrice());
            if(itemDiscounts.keySet().contains(item.getId()))
                itemPrice -= itemPrice * itemDiscounts.get(item.getId());
            subTotal = subTotal + itemPrice * items.get(i).getQuantity();
        }
        total.setText("Subtotal: $" + df.format(subTotal));
    }

    //fills the items array if item instances for each item in the users shopping cart.
    private void getData(DataSnapshot itemData, DataSnapshot cartData) {
        items = new ArrayList<>();
        for(DataSnapshot ds : cartData.getChildren()) {
            Item item = new Item();
            String itemId = ds.getKey();
            item.setId(itemId);
            item.setName(itemData.child(itemId).child("name").getValue().toString());
            item.setPrice(itemData.child(itemId).child("price").getValue().toString());
            item.setDescription(itemData.child(itemId).child("description").getValue().toString());
            int quantity = Integer.parseInt(ds.child("quantity").getValue().toString());
            items.add(new ShoppingCartItem(item, quantity));
        }
    }

    //fills items array with item instances for each item in the guest users shopping cart.
    private void getGuestData(DataSnapshot itemData) {
        items = new ArrayList<>();
        for(String s : guestCart.keySet()) {
            Item item = new Item();
            item.setId(s);
            item.setName(itemData.child(s).child("name").getValue().toString());
            item.setPrice(itemData.child(s).child("price").getValue().toString());
            item.setDescription(itemData.child(s).child("description").getValue().toString());
            items.add(new ShoppingCartItem(item, guestCart.get(s)));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    //method is called when an item in the shopping cart recycler view is clicked. Launches popup to
    //edit clicked the clicked items quantity.
    @Override
    public void onItemClick(View view, int position) {
        ShoppingCartItem item = items.get(position);
        createQuantityPopup(item.getItem().getId());
    }

    //creates the edit quantity popup.
    private void createQuantityPopup(final String itemId) {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.quantity_popup, null);
        Button update = view.findViewById(R.id.changeQuantityBtn);
        final EditText quantityText = view.findViewById(R.id.quanityInput);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(quantityText.getText().toString().matches("")) {
                    toastMessage("Enter a valid quantity");
                }
                else {
                    if(!isGuest) {
                        int newQuant = Integer.parseInt(quantityText.getText().toString());
                        if(newQuant == 0)
                            dbRef.child("shoppingCarts").child(auth.getCurrentUser().getUid()).child(itemId).removeValue();
                        else
                            dbRef.child("shoppingCarts").child(auth.getCurrentUser().getUid()).child(itemId).child("quantity").setValue(newQuant);
                        toastMessage("Updated Successfully");
                        dialog.hide();
                    }
                    else {
                        int newQuant = Integer.parseInt(quantityText.getText().toString());
                        if(newQuant == 0) {
                            guestCart.remove(itemId);
                            for(int i = 0; i < items.size(); i++) {
                                if(items.get(i).getItem().getId().equals(itemId)) {
                                    items.remove(i);
                                    break;
                                }
                            }
                        }
                        else {
                            guestCart.put(itemId, newQuant);
                            for(int i = 0; i < items.size(); i++) {
                                if(items.get(i).getItem().getId().equals(itemId)) {
                                    items.get(i).setQuantity(newQuant);
                                    break;
                                }
                            }
                        }
                        doRecyclerView();
                        toastMessage("Updated Successfully");
                        dialog.hide();
                    }
                }
            }
        });
    }

    //generic method that creates a toast message.
    private void toastMessage(String msg) {
        Toast.makeText(ShoppingCart.this, msg, Toast.LENGTH_SHORT).show();
    }
}