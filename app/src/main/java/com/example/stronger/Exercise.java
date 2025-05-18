package com.example.stronger;

// In your models package
public class Exercise {
    private String name; // e.g., "Bench Press", "Squats"
    private int sets;
    private int reps;
    private double weightKg; // Or string if you allow lbs/kg toggle

    // Constructors
    public Exercise() {}

    public Exercise(String name, int sets, int reps, double weightKg) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
    }

    // Getters and Setters...
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
}