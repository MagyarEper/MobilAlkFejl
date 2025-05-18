package com.example.stronger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    // Renamed buttons for clarity based on new roles
    private Button buttonPrimaryAction, buttonRegisterOrProfile, logoutButton;
    private static FirebaseAuth mAuth;
    public static FirebaseFirestore firestore; // Keep this if other activities still rely on it

    // Static getters - consider if these are still the best approach long-term
    // It's often preferred for each activity to get its own instance.
    // For now, we'll keep them if LoginActivity depends on them.
    public static FirebaseAuth getmAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }
    public static FirebaseFirestore getFirestore(){
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.textViewWelcome);
        buttonPrimaryAction = findViewById(R.id.buttonPrimaryAction);         // Renamed
        buttonRegisterOrProfile = findViewById(R.id.buttonRegisterOrProfile); // Renamed
        logoutButton = findViewById(R.id.buttonLogout);
        ConstraintLayout rootLayout = findViewById(R.id.mainLayout);

        // Initialize directly here is also fine, ensures they are not null
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Initialize here too

        FirebaseUser currentUser = mAuth.getCurrentUser();

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in); // Ensure fade_in.xml exists in res/anim
        rootLayout.startAnimation(fadeIn);

        updateUI(); // Call updateUI to set initial state

        buttonPrimaryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // USER IS LOGGED IN: Primary action is to go to Workout List
                    startActivity(new Intent(MainActivity.this, WorkoutListActivity.class));
                    // Apply transition animation
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    // USER IS NOT LOGGED IN: Primary action is to go to Login
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        buttonRegisterOrProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // USER IS LOGGED IN: Secondary action could be Profile (not implemented yet)
                    // For now, let's just make it do nothing or show a Toast
                    Toast.makeText(MainActivity.this, "Profile page coming soon!", Toast.LENGTH_SHORT).show();
                    // startActivity(new Intent(MainActivity.this, ProfileActivity.class)); // If you create ProfileActivity
                } else {
                    // USER IS NOT LOGGED IN: Secondary action is to go to Register
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                }
            }
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            updateUI(); // Update UI after logout
        });
    }

    private void updateUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail() != null ? currentUser.getEmail() : "User";
            welcomeText.setText("Welcome, " + email + "!");
            buttonPrimaryAction.setText("View Workout Log"); // Changed text
            buttonRegisterOrProfile.setText("My Profile");   // Changed text
            buttonRegisterOrProfile.setVisibility(View.VISIBLE); // Or View.GONE if no profile yet
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            welcomeText.setText("Welcome to Stronger!");
            buttonPrimaryAction.setText("Login");
            buttonRegisterOrProfile.setText("Register");
            buttonRegisterOrProfile.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI in case auth state changed while app was in background
        // (e.g. user logged out from another device or session expired - though less common with Firebase Auth)
        updateUI();
    }
}