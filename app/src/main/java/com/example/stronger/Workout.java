package com.example.stronger;

public class Workout {
    public String workoutId;
    public long workoutDate;
    public String workoutDescription;
    public String mood;
    public String imagePath;

    public Workout() {} // No-argument constructor

    public Workout(String workoutId, long workoutDate, String workoutDescription, String mood, String imagePath) {
        this.workoutId = workoutId;
        this.workoutDate = workoutDate;
        this.workoutDescription = workoutDescription;
        this.mood = mood;
        this.imagePath = imagePath;
    }

    // Getters and setters
    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }
    public long getWorkoutDate() { return workoutDate; }
    public void setWorkoutDate(long workoutDate) { this.workoutDate = workoutDate; }
    public String getWorkoutDescription() { return workoutDescription; }
    public void setWorkoutDescription(String workoutDescription) { this.workoutDescription = workoutDescription; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}