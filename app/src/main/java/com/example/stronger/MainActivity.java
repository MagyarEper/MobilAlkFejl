package com.example.stronger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button loginButton, registerButton, logoutButton;
    private static FirebaseAuth mAuth;

    public static FirebaseFirestore firestore;
    public static FirebaseAuth getmAuth() {
        return mAuth;
    }
    public static FirebaseFirestore getFirestore(){
        return firestore;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.textViewWelcome);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);
        //addWorkoutBtn = findViewById(R.id.buttonAddWorkout);
        //myWorkoutBtn = findViewById(R.id.buttonMyWorkout);
        logoutButton = findViewById(R.id.buttonLogout);
        ConstraintLayout rootLayout = findViewById(R.id.mainLayout);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        rootLayout.startAnimation(fadeIn);
        if (currentUser != null) {
            String email = currentUser.getEmail();
            welcomeText.setText("Welcome to stronger " + email + "!");
        }

        updateUI();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is logged in, so go to profile.
                    String email = currentUser.getEmail();
                    welcomeText.setText("Welcome to stronger " + email + "!"); //  Create a ProfileActivity
                } else {
                    // User is not logged in, go to Login Activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            updateUI(); // Update UI after logout
        });
        //későbbi implementálásra ->
        /*addWorkoutBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));*/

        //későbbi implementálásra ->
        /*myWorkoutBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));*/


    }

    private void updateUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            welcomeText.setText("Welcome, " + email + "!");
            loginButton.setText("Profile");
            registerButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            welcomeText.setText("Welcome!");
            loginButton.setText("Login");
            registerButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
        }
    }
}
