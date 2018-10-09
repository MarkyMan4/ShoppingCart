package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class BrowseActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {
    MyRecyclerViewAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        ArrayList<Item> itemsList = new ArrayList<>();
        itemsList.add(new Item("test1", "this is test item 1.", 1.00));
        itemsList.add(new Item("test2", "this is test item 2.", 2.00));
        itemsList.add(new Item("test3", "this is test item 3.", 3.00));
        itemsList.add(new Item("test4", "this is test item 4.", 4.00));
        itemsList.add(new Item("test5", "kjuheraikjgraiouprgouipraes .", 1.00));


        RecyclerView recyclerView = findViewById(R.id.rvItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter = new MyRecyclerViewAdapter(this, itemsList);
        rvAdapter.setClickListener(this);
        recyclerView.setAdapter(rvAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + rvAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}
