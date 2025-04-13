package com.example.stronger;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
public class FirestoreHelper {

    public static void addWorkoutToFirestore(FirebaseFirestore firestore, Workout workout, OnCompleteListener<Void> listener) {
        firestore.collection("workouts").document(workout.workoutId).set(workout).addOnCompleteListener(listener);
    }

    public static void getWorkoutsFromFirestore(FirebaseFirestore firestore, OnCompleteListener<QuerySnapshot> listener) {
        firestore.collection("workouts").get().addOnCompleteListener(listener);
    }

    public static void getWorkoutFromFirestore(FirebaseFirestore firestore, String workoutId, OnCompleteListener<DocumentSnapshot> listener) {
        firestore.collection("workouts").document(workoutId).get().addOnCompleteListener(listener);
    }
}
