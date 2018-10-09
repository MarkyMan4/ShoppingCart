package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseDatabase db;
    private DatabaseReference dbRef, mDatabase;
    private FirebaseUser currUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText pwd;
    private Button signIn;
    private Button signUp;
    private Button guest;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog.Builder alert;
    private AlertDialog dialog;
    private EditText popupEmail;
    private EditText popupPwd;
    private EditText popupFName;
    private EditText popupLName;
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailField);
        pwd = findViewById(R.id.password);
        signIn = findViewById(R.id.loginBtn);
        signUp = findViewById(R.id.signUpBtn);
        guest = findViewById(R.id.guestBtn);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null){
                    //User signed in
                    toastMessage("Successfully signed in with " + user.getEmail());
                }else{
                    //user is signed out
                    Log.d("Here: ", "user not signed in");

                }
            }
        };

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailText = email.getText().toString();
                String pass = pwd.getText().toString();
                if(!emailText.equals("") && !pass.equals("")){
                    mAuth.signInWithEmailAndPassword(emailText, pass)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent intent = new Intent(MainActivity.this, BrowseActivity.class);
                                        startActivity(intent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        toastMessage("Authentication Failed");
                                    }
                                }
                            });
                }
                else {
                    toastMessage("You didn't fill in all the fields");
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailText = email.getText().toString();
                String passwordText = pwd.getText().toString();
                if(!emailText.equals("") && !passwordText.equals("")){

                }

                createPopupDialog();
            }
        });

        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BrowseActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createPopupDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.signup_popup, null);
        popupEmail = view.findViewById(R.id.email);
        popupPwd = view.findViewById(R.id.password);
        popupFName = view.findViewById(R.id.fname);
        popupLName = view.findViewById(R.id.lname);
        addBtn = view.findViewById(R.id.add);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
        addBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                addUser(popupEmail.getText().toString(), popupPwd.getText().toString(),
                        popupFName.getText().toString(), popupLName.getText().toString());
                dialog.hide();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void toastMessage(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void addUser(final String email, String password, final String first, final String last) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Show Dialog
                    alert = new AlertDialog.Builder(MainActivity.this);


                    currUser = FirebaseAuth.getInstance().getCurrentUser();
                    User user = new User(email, first, last);
                    mDatabase.child("users").child(currUser.getUid()).setValue(user);
                    //mDatabase.child("users").child("1").setValue("2");
                    //database.child("users").child(emailText).setValue("yes");
                    toastMessage("Account Created!");
                } else {
                    toastMessage(task.getException().getLocalizedMessage().toString());
                }
            }
        });
    }
}
