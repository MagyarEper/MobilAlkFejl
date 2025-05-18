package com.example.stronger; // Replace with your actual package name

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

// Replace with your actual model package
import com.example.stronger.Exercise;
import com.example.stronger.Workout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class AddEditWorkoutActivity extends AppCompatActivity {

    private static final String TAG = "AddEditWorkoutActivity";
    public static final String EXTRA_WORKOUT_ID = "WORKOUT_ID";

    private TextInputEditText editTextWorkoutName, editTextDuration, editTextNotes, editTextTags;
    private TextInputLayout tilWorkoutName, tilDuration;
    private LinearLayout layoutExercisesContainer;
    private Button buttonAddExercise, buttonSaveWorkout, buttonDeleteWorkout, buttonSetReminder;
    private ProgressBar progressBarSave;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String workoutIdToEdit = null;
    private Workout existingWorkout = null; // To hold loaded workout for editing

    private Calendar reminderCalendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_workout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            // Optionally, redirect to login:
            // startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        editTextWorkoutName = findViewById(R.id.editTextWorkoutName);
        editTextDuration = findViewById(R.id.editTextDuration);
        editTextNotes = findViewById(R.id.editTextNotes);
        editTextTags = findViewById(R.id.editTextTags);
        tilWorkoutName = findViewById(R.id.tilWorkoutName);
        tilDuration = findViewById(R.id.tilDuration);
        layoutExercisesContainer = findViewById(R.id.layoutExercisesContainer);
        buttonAddExercise = findViewById(R.id.buttonAddExercise);
        buttonSaveWorkout = findViewById(R.id.buttonSaveWorkout);
        buttonDeleteWorkout = findViewById(R.id.buttonDeleteWorkout);
        buttonSetReminder = findViewById(R.id.buttonSetReminder);
        progressBarSave = findViewById(R.id.progressBarSave);

        reminderCalendar = Calendar.getInstance();


        buttonAddExercise.setOnClickListener(v -> addExerciseInputView(null));
        buttonSaveWorkout.setOnClickListener(v -> saveWorkout());
        buttonDeleteWorkout.setOnClickListener(v -> confirmDeleteWorkout());
        buttonSetReminder.setOnClickListener(v -> showDateTimePickerForReminder());


        if (getIntent().hasExtra(EXTRA_WORKOUT_ID)) {
            workoutIdToEdit = getIntent().getStringExtra(EXTRA_WORKOUT_ID);
            if (workoutIdToEdit != null) {
                setTitle("Edit Workout");
                buttonSaveWorkout.setText("Update Workout");
                buttonDeleteWorkout.setVisibility(View.VISIBLE);
                loadWorkoutData(workoutIdToEdit);
            }
        } else {
            setTitle("Add New Workout");
            // Add one empty exercise row by default for new workouts
            addExerciseInputView(null);
        }
    }

    private void showDateTimePickerForReminder() {
        final Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            reminderCalendar.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                reminderCalendar.set(Calendar.MINUTE, minute);
                reminderCalendar.set(Calendar.SECOND, 0);
                reminderCalendar.set(Calendar.MILLISECOND, 0);

                // Check if selected time is in the past
                if (reminderCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Cannot set reminder for a past time.", Toast.LENGTH_LONG).show();
                    return;
                }

                String workoutName = editTextWorkoutName.getText().toString().trim();
                if (workoutName.isEmpty()) workoutName = "Your Workout";

                setWorkoutReminder(reminderCalendar.getTimeInMillis(), workoutName);

            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }


    private void setWorkoutReminder(long triggerAtMillis, String workoutName) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class); // Assuming AlarmReceiver.java exists
        intent.putExtra("WORKOUT_NAME", workoutName);
        intent.putExtra("NOTIFICATION_ID", (int) System.currentTimeMillis()); // Unique ID for pending intent

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(), // Unique request code
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                Toast.makeText(this, "Reminder set for " + workoutName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please grant 'Alarms & reminders' permission to set exact alarms.", Toast.LENGTH_LONG).show();
                // Optionally guide user to settings
                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(permissionIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Toast.makeText(this, "Reminder set for " + workoutName, Toast.LENGTH_SHORT).show();
        }
    }


    private void loadWorkoutData(String workoutId) {
        progressBarSave.setVisibility(View.VISIBLE);
        db.collection("workouts").document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBarSave.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        existingWorkout = documentSnapshot.toObject(Workout.class);
                        if (existingWorkout != null) {
                            existingWorkout.setId(documentSnapshot.getId()); // Important!
                            populateFields(existingWorkout);
                        } else {
                            Toast.makeText(this, "Failed to parse workout data.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Workout not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBarSave.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading workout: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading workout", e);
                    finish();
                });
    }

    private void populateFields(Workout workout) {
        editTextWorkoutName.setText(workout.getName());
        editTextDuration.setText(String.valueOf(workout.getDurationMinutes()));
        editTextNotes.setText(workout.getNotes());
        if (workout.getTags() != null && !workout.getTags().isEmpty()) {
            editTextTags.setText(String.join(",", workout.getTags()));
        }


        layoutExercisesContainer.removeAllViews(); // Clear any default or previous views
        if (workout.getExercises() != null) {
            for (Exercise exercise : workout.getExercises()) {
                addExerciseInputView(exercise);
            }
        }
    }

    private void addExerciseInputView(Exercise exercise) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View exerciseView = inflater.inflate(R.layout.item_exercise_input, layoutExercisesContainer, false);

        TextInputEditText etName = exerciseView.findViewById(R.id.editTextExerciseName);
        TextInputEditText etSets = exerciseView.findViewById(R.id.editTextSets);
        TextInputEditText etReps = exerciseView.findViewById(R.id.editTextReps);
        TextInputEditText etWeight = exerciseView.findViewById(R.id.editTextWeight);
        ImageButton btnRemove = exerciseView.findViewById(R.id.buttonRemoveExercise);

        if (exercise != null) {
            etName.setText(exercise.getName());
            etSets.setText(String.valueOf(exercise.getSets()));
            etReps.setText(String.valueOf(exercise.getReps()));
            etWeight.setText(String.format(Locale.US, "%.1f", exercise.getWeightKg()));
        }

        btnRemove.setOnClickListener(v -> {
            // Simple animation (optional, could be more complex)
            v.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                layoutExercisesContainer.removeView(exerciseView);
            }).start();
        });

        layoutExercisesContainer.addView(exerciseView);
    }

    private boolean validateInputs() {
        String workoutName = editTextWorkoutName.getText().toString().trim();
        String durationStr = editTextDuration.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(workoutName)) {
            tilWorkoutName.setError("Workout name cannot be empty");
            isValid = false;
        } else {
            tilWorkoutName.setError(null);
        }

        if (TextUtils.isEmpty(durationStr)) {
            tilDuration.setError("Duration cannot be empty");
            isValid = false;
        } else {
            try {
                Integer.parseInt(durationStr);
                tilDuration.setError(null);
            } catch (NumberFormatException e) {
                tilDuration.setError("Invalid duration number");
                isValid = false;
            }
        }

        // Validate exercises
        for (int i = 0; i < layoutExercisesContainer.getChildCount(); i++) {
            View exerciseView = layoutExercisesContainer.getChildAt(i);
            TextInputEditText etExName = exerciseView.findViewById(R.id.editTextExerciseName);
            TextInputEditText etExSets = exerciseView.findViewById(R.id.editTextSets);
            TextInputEditText etExReps = exerciseView.findViewById(R.id.editTextReps);
            // Weight can be optional or 0

            if (TextUtils.isEmpty(etExName.getText().toString().trim())) {
                ((TextInputLayout)etExName.getParent().getParent()).setError("Name?");
                isValid = false;
            } else {
                ((TextInputLayout)etExName.getParent().getParent()).setError(null);
            }
            if (TextUtils.isEmpty(etExSets.getText().toString().trim())) {
                ((TextInputLayout)etExSets.getParent().getParent()).setError("Sets?");
                isValid = false;
            } else {
                ((TextInputLayout)etExSets.getParent().getParent()).setError(null);
            }
            if (TextUtils.isEmpty(etExReps.getText().toString().trim())) {
                ((TextInputLayout)etExReps.getParent().getParent()).setError("Reps?");
                isValid = false;
            } else {
                ((TextInputLayout)etExReps.getParent().getParent()).setError(null);
            }
        }
        if (!isValid) {
            Toast.makeText(this, "Please correct the errors.", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void saveWorkout() {
        if (!validateInputs()) {
            return;
        }

        progressBarSave.setVisibility(View.VISIBLE);
        buttonSaveWorkout.setEnabled(false);

        String workoutName = editTextWorkoutName.getText().toString().trim();
        int duration = Integer.parseInt(editTextDuration.getText().toString().trim());
        String notes = editTextNotes.getText().toString().trim();
        String tagsString = editTextTags.getText().toString().trim();
        List<String> tagsList = new ArrayList<>();
        if (!tagsString.isEmpty()) {
            tagsList = Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
        }


        List<Exercise> exercises = new ArrayList<>();
        for (int i = 0; i < layoutExercisesContainer.getChildCount(); i++) {
            View exerciseView = layoutExercisesContainer.getChildAt(i);
            TextInputEditText etExName = exerciseView.findViewById(R.id.editTextExerciseName);
            TextInputEditText etExSets = exerciseView.findViewById(R.id.editTextSets);
            TextInputEditText etExReps = exerciseView.findViewById(R.id.editTextReps);
            TextInputEditText etExWeight = exerciseView.findViewById(R.id.editTextWeight);

            String exName = etExName.getText().toString().trim();
            int exSets = 0;
            try { exSets = Integer.parseInt(etExSets.getText().toString().trim()); } catch (NumberFormatException ignored) {}
            int exReps = 0;
            try { exReps = Integer.parseInt(etExReps.getText().toString().trim()); } catch (NumberFormatException ignored) {}
            double exWeight = 0.0;
            try { exWeight = Double.parseDouble(etExWeight.getText().toString().trim()); } catch (NumberFormatException ignored) {}

            if (!TextUtils.isEmpty(exName)) { // Only add if name is present
                exercises.add(new Exercise(exName, exSets, exReps, exWeight));
            }
        }

        Workout workout = new Workout(currentUser.getUid(), workoutName, duration, notes);
        workout.setExercises(exercises);
        workout.setTags(tagsList);
        // `timestamp` will be set by @ServerTimestamp on the model if saving for the first time,
        // or preserved if editing (unless you explicitly re-set it).
        // If editing, we want to preserve the original timestamp or allow user to change it.
        // For simplicity, if existingWorkout is present, we can copy its timestamp.
        if (existingWorkout != null && existingWorkout.getTimestamp() != null) {
            workout.setTimestamp(existingWorkout.getTimestamp()); // Preserve original creation/logged time
        }


        if (workoutIdToEdit != null) { // Update existing
            workout.setId(workoutIdToEdit); // Ensure ID is set for update logic if model needs it
            db.collection("workouts").document(workoutIdToEdit)
                    .set(workout, SetOptions.merge()) // Merge to avoid overwriting fields not handled here
                    .addOnSuccessListener(aVoid -> handleSaveSuccess(workoutName))
                    .addOnFailureListener(this::handleSaveFailure);
        } else { // Create new
            db.collection("workouts")
                    .add(workout)
                    .addOnSuccessListener(documentReference -> {
                        // workout.setId(documentReference.getId()); // Set ID on local object if needed elsewhere
                        handleSaveSuccess(workoutName);
                    })
                    .addOnFailureListener(this::handleSaveFailure);
        }
    }

    private void handleSaveSuccess(String workoutName) {
        progressBarSave.setVisibility(View.GONE);
        buttonSaveWorkout.setEnabled(true);
        Toast.makeText(AddEditWorkoutActivity.this, "Workout saved!", Toast.LENGTH_SHORT).show();

        // Call NotificationHelper (assuming you have NotificationHelper.java from previous steps)
        // Make sure you have created NotificationHelper and the ic_fitness_notification drawable
        try {
            NotificationHelper.showWorkoutSavedNotification(AddEditWorkoutActivity.this, workoutName);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
            Toast.makeText(this, "Notification error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        setResult(RESULT_OK); // To notify WorkoutListActivity to refresh
        finish();
        // Apply slide out animation if you have it
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void handleSaveFailure(Exception e) {
        progressBarSave.setVisibility(View.GONE);
        buttonSaveWorkout.setEnabled(true);
        Toast.makeText(AddEditWorkoutActivity.this, "Error saving workout: " + e.getMessage(), Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error saving workout", e);
    }


    private void confirmDeleteWorkout() {
        if (workoutIdToEdit == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete this workout? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteWorkout())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert) // Or a custom warning icon
                .show();
    }


    private void deleteWorkout() {
        if (workoutIdToEdit == null) return;

        progressBarSave.setVisibility(View.VISIBLE); // Re-use progress bar
        db.collection("workouts").document(workoutIdToEdit)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBarSave.setVisibility(View.GONE);
                    Toast.makeText(this, "Workout deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Notify list to refresh
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                })
                .addOnFailureListener(e -> {
                    progressBarSave.setVisibility(View.GONE);
                    Toast.makeText(this, "Error deleting workout: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error deleting workout", e);
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}