package com.example.stronger; // Replace with your package name

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.stronger.adapters.WorkoutAdapter; // Replace with your adapter package
import com.example.stronger.Workout;      // Replace with your model package

import java.util.ArrayList;
import java.util.List;

public class WorkoutListActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutClickListener {

    private static final String TAG = "WorkoutListActivity";

    private RecyclerView recyclerViewWorkouts;
    private WorkoutAdapter workoutAdapter;
    private FloatingActionButton fabAddWorkout;
    private ProgressBar progressBarList;
    private TextView textViewEmptyList;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration firestoreListener;

    // For handling results from AddEditWorkoutActivity (e.g., to refresh or react)
    private final ActivityResultLauncher<Intent> addEditWorkoutLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // The Firestore listener should automatically update the list.
                    // You might log or perform other actions here if needed.
                    Log.d(TAG, "Returned from AddEditWorkoutActivity with RESULT_OK.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);

        toolbar = findViewById(R.id.toolbarWorkoutList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Workout Tracker");
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // If user is not logged in, redirect to LoginActivity (assuming you have one)
        if (currentUser == null) {
            startActivity(new Intent(WorkoutListActivity.this, LoginActivity.class)); // Replace LoginActivity.class
            finish();
            return;
        }

        recyclerViewWorkouts = findViewById(R.id.recyclerViewWorkouts);
        fabAddWorkout = findViewById(R.id.fabAddWorkout);
        progressBarList = findViewById(R.id.progressBarList);
        textViewEmptyList = findViewById(R.id.textViewEmptyList);

        setupRecyclerView();

        fabAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutListActivity.this, AddEditWorkoutActivity.class);
            addEditWorkoutLauncher.launch(intent);
            // Activity transition animation (as discussed previously)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Ensure these anims exist
        });

        // FAB Animation on Scroll (Requirement)
        recyclerViewWorkouts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fabAddWorkout.getVisibility() == View.VISIBLE) { // Scrolling down
                    fabAddWorkout.animate().scaleX(0).scaleY(0).setDuration(200).withEndAction(() -> fabAddWorkout.setVisibility(View.GONE)).start();
                } else if (dy < 0 && fabAddWorkout.getVisibility() != View.VISIBLE) { // Scrolling up
                    fabAddWorkout.setVisibility(View.VISIBLE);
                    fabAddWorkout.animate().scaleX(1).scaleY(1).setDuration(200).start();
                }
            }
        });
    }

    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter(this, this);
        recyclerViewWorkouts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWorkouts.setAdapter(workoutAdapter);
    }

    private void loadWorkoutsFromFirestore() {
        if (currentUser == null) return;

        progressBarList.setVisibility(View.VISIBLE);
        textViewEmptyList.setVisibility(View.GONE);
        recyclerViewWorkouts.setVisibility(View.GONE);

        // Query to get workouts for the current user, ordered by timestamp (most recent first)
        Query query = db.collection("workouts")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        // Remove any existing listener before attaching a new one
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        firestoreListener = query.addSnapshotListener((snapshots, e) -> {
            progressBarList.setVisibility(View.GONE);
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                Toast.makeText(WorkoutListActivity.this, "Error loading workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                textViewEmptyList.setText("Error loading data. Please try again.");
                textViewEmptyList.setVisibility(View.VISIBLE);
                recyclerViewWorkouts.setVisibility(View.GONE);
                return;
            }

            List<Workout> workouts = new ArrayList<>();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    if (doc.exists()) {
                        Workout workout = doc.toObject(Workout.class);
                        workout.setId(doc.getId()); // Set the document ID to the Workout object
                        workouts.add(workout);
                    }
                }
            }

            if (workouts.isEmpty()) {
                textViewEmptyList.setText("No workouts logged yet.\nTap '+' to add your first one!");
                textViewEmptyList.setVisibility(View.VISIBLE);
                recyclerViewWorkouts.setVisibility(View.GONE);
            } else {
                textViewEmptyList.setVisibility(View.GONE);
                recyclerViewWorkouts.setVisibility(View.VISIBLE);
            }
            workoutAdapter.setWorkouts(workouts);
        });
    }

    @Override
    public void onWorkoutClick(Workout workout) {
        Intent intent = new Intent(WorkoutListActivity.this, AddEditWorkoutActivity.class);
        intent.putExtra(AddEditWorkoutActivity.EXTRA_WORKOUT_ID, workout.getId()); // Pass workout ID for editing
        addEditWorkoutLauncher.launch(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Ensure these anims exist
    }

    // --- Lifecycle Hooks ---
    @Override
    protected void onStart() {
        super.onStart();
        // Re-check auth status and start listening for workout changes
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(WorkoutListActivity.this, LoginActivity.class)); // Replace LoginActivity.class
            finish();
            return;
        }
        loadWorkoutsFromFirestore(); // Start or restart listening
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreListener != null) {
            firestoreListener.remove(); // Stop listening to prevent memory leaks and unnecessary operations
        }
    }

    // --- Menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workout_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(WorkoutListActivity.this, LoginActivity.class)); // Replace LoginActivity.class
            finishAffinity(); // Clears the activity stack
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}