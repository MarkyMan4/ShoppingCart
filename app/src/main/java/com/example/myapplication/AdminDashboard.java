package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdminDashboard extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

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
    private Button newItemButton;
    private Button editButton;
    private Spinner itemSpinner;
    private EditText percentInput;
    private EditText startDate;
    private EditText endDate;
    private EditText codeName;
    private AlertDialog.Builder dialogBuilder;
    private ArrayList<Item> items;
    private AlertDialog dialog;
    private MyRecyclerViewAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        signOut = findViewById(R.id.signout);
        addCode = findViewById(R.id.addcode);
        newItemButton = findViewById(R.id.add_item_btn);
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
                managePromotionsDialog();
            }
        });

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data = dataSnapshot;
                items = getData(dataSnapshot.child("items"));
                updateRecyclerView(items);
                doRecyclerView(dataSnapshot.child("items"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        newItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminDashboard.this, NewItemActivity.class);
                startActivity(intent);
            }
        });
    }

    private ArrayList<Item> getData(DataSnapshot dataSnapshot) {
        items = new ArrayList<>();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            Item item = new Item();
            item.setName((String)ds.child("name").getValue());
            item.setDescription((String)ds.child("description").getValue());
            item.setPrice(ds.child("price").getValue().toString());
            item.setId(ds.getKey());
            items.add(item);
        }
        return items;
    }

    private void updateRecyclerView(ArrayList<Item> newItems) {
        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter = new MyRecyclerViewAdapter(this, newItems);
        rvAdapter.setClickListener(this);
        recyclerView.setAdapter(rvAdapter);
    }

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
    private void managePromotionsDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_code_popup, null);
        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

//        DataSnapshot promotions = data.child("promotion");
//        ArrayList<String> items = new ArrayList<>();
//        for(DataSnapshot ds : promotions.getChildren()) {
//            items.add((String)ds.child("name").getValue());
//        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, items);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        promoSpinner.setAdapter(adapter);

        next = view.findViewById(R.id.nextbtn);
        editButton = view.findViewById(R.id.edit_btn);
        close = view.findViewById(R.id.closebtn);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                createItemPercentPopup();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                createEditPromoPopup();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
    }

    private void createEditPromoPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_edit_promotion, null);

        final Spinner promoSpinner = view.findViewById(R.id.promo_spinner);
        final EditText editPercentInput = view.findViewById(R.id.edit_percent_input);
        final EditText editStartDateInput = view.findViewById(R.id.edit_start_date);
        final EditText editEndDateInput = view.findViewById(R.id.edit_end_date);
        final View div1 = view.findViewById(R.id.divider6);
        final View div2 = view.findViewById(R.id.divider7);
        final TextView percentLabel = view.findViewById(R.id.percent_label);
        final TextView startLabel = view.findViewById(R.id.start_label);
        final TextView endLabel = view.findViewById(R.id.end_label);
        final Button editDoneBtn = view.findViewById(R.id.edit_promo_done);
        final Button editCancelBtn = view.findViewById(R.id.edit_promo_cancel);

        DataSnapshot promotions = data.child("itemDiscount");
        ArrayList<String> promos = new ArrayList<>();
        promos.add("-select-");
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

        promoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = promoSpinner.getSelectedItem().toString();
                if(text.equals("-select-")) {
                    editPercentInput.setVisibility(View.INVISIBLE);
                    editStartDateInput.setVisibility(View.INVISIBLE);
                    editEndDateInput.setVisibility(View.INVISIBLE);
                    div1.setVisibility(View.INVISIBLE);
                    div2.setVisibility(View.INVISIBLE);
                    percentLabel.setVisibility(View.INVISIBLE);
                    startLabel.setVisibility(View.INVISIBLE);
                    endLabel.setVisibility(View.INVISIBLE);
                    editDoneBtn.setVisibility(View.INVISIBLE);
                    editCancelBtn.setVisibility(View.INVISIBLE);
                }
                else {
                    editPercentInput.setVisibility(View.VISIBLE);
                    editStartDateInput.setVisibility(View.VISIBLE);
                    editEndDateInput.setVisibility(View.VISIBLE);
                    div1.setVisibility(View.VISIBLE);
                    div2.setVisibility(View.VISIBLE);
                    percentLabel.setVisibility(View.VISIBLE);
                    startLabel.setVisibility(View.VISIBLE);
                    endLabel.setVisibility(View.VISIBLE);
                    editDoneBtn.setVisibility(View.VISIBLE);
                    editCancelBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        editDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String promotion = promoSpinner.getSelectedItem().toString();
                String percent = editPercentInput.getText().toString();
                String startDate= editStartDateInput.getText().toString();
                String endDate = editEndDateInput.getText().toString();
                String regex = "([0-9]{2})/([0-9]{2})/([0-9]{4})";
                if(percent.matches("") || startDate.matches("") || endDate.matches("")) {
                    toastMessage("Enter all fields");
                }
                else {
                    if(!startDate.matches(regex) || !endDate.matches(regex)) {
                        toastMessage("Enter dates in the form mm/dd/yyyy");
                    }
                    else {
                        dbRef.child("itemDiscount").child(promotion).child("percent").setValue(percent);
                        dbRef.child("itemDiscount").child(promotion).child("startDate").setValue(startDate);
                        dbRef.child("itemDiscount").child(promotion).child("endDate").setValue(endDate);
                        toastMessage(promotion + " has been updated");
                        dialog.hide();
                    }
                }
            }
        });

        editCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private boolean datesEqual(Date d1, Date d2) {
        return d1.getDate() == d2.getDate() && d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear();
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
                    dialog.hide();
                    toastMessage("Promotion Successfully Created!");
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

    @Override
    public void onItemClick(View view, int position) {
        Item item = items.get(position);
        Intent intent = new Intent(AdminDashboard.this, EditItemActivity.class);
        intent.putExtra("ID", item.getId());
        startActivity(intent);
    }

    private void toastMessage(String msg) {
        Toast.makeText(AdminDashboard.this, msg, Toast.LENGTH_SHORT).show();
    }
}
