package com.example.stronger; // Or com.example.stronger.models

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Workout {
    @DocumentId
    private String id;
    private String userId; // <<< THIS FIELD IS KEY
    private String name;
    @ServerTimestamp
    private Date timestamp;
    private int durationMinutes;
    private List<Exercise> exercises;
    private String notes;
    private List<String> tags;

    // Constructor for Firestore
    public Workout() {
        this.exercises = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // Your constructor used in AddEditWorkoutActivity
    public Workout(String userId, String name, int durationMinutes, String notes) {
        this.userId = userId; // <<< ENSURE THIS ASSIGNMENT HAPPENS
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
        this.exercises = new ArrayList<>(); // Initialize lists
        this.tags = new ArrayList<>();      // Initialize lists
    }

    // Getters and Setters for ALL fields, including userId
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; } // <<< Getter for userId
    public void setUserId(String userId) { this.userId = userId; } // <<< Setter for userId

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; } // Usually set by server

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}