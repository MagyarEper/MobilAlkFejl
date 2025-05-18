package com.example.stronger.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stronger.R;
import com.example.stronger.Workout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;
    private final Context context;
    private final OnWorkoutClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mma", Locale.getDefault());

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        // void onWorkoutLongClick(Workout workout); // Optional for other actions like delete
    }

    public WorkoutAdapter(Context context, OnWorkoutClickListener listener) {
        this.context = context;
        this.workoutList = new ArrayList<>();
        this.listener = listener;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workoutList = (workouts == null) ? new ArrayList<>() : workouts;
        notifyDataSetChanged(); // For simplicity. Use DiffUtil for better performance.
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.bind(workout, listener);
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvDuration, tvExerciseCount, tvTags;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.textViewWorkoutNameItem);
            tvDate = itemView.findViewById(R.id.textViewWorkoutDateItem);
            tvDuration = itemView.findViewById(R.id.textViewWorkoutDurationItem);
            tvExerciseCount = itemView.findViewById(R.id.textViewExerciseCountItem);
            tvTags = itemView.findViewById(R.id.textViewWorkoutTagsItem);
        }

        void bind(final Workout workout, final OnWorkoutClickListener listener) {
            tvName.setText(workout.getName());

            if (workout.getTimestamp() != null) {
                tvDate.setText(dateFormat.format(workout.getTimestamp()));
            } else {
                tvDate.setText("Date N/A");
            }

            tvDuration.setText(String.format(Locale.getDefault(), "%d min", workout.getDurationMinutes()));

            int exerciseCount = (workout.getExercises() != null) ? workout.getExercises().size() : 0;
            tvExerciseCount.setText(String.format(Locale.getDefault(), "%d Exercise%s",
                    exerciseCount, exerciseCount == 1 ? "" : "s"));


            if (workout.getTags() != null && !workout.getTags().isEmpty()) {
                String tagsString = workout.getTags().stream()
                        .filter(tag -> !TextUtils.isEmpty(tag))
                        .collect(Collectors.joining(", "));
                if (!tagsString.isEmpty()) {
                    tvTags.setText("Tags: " + tagsString);
                    tvTags.setVisibility(View.VISIBLE);
                } else {
                    tvTags.setVisibility(View.GONE);
                }
            } else {
                tvTags.setVisibility(View.GONE);
            }


            itemView.setOnClickListener(v -> listener.onWorkoutClick(workout));
            // itemView.setOnLongClickListener(v -> {
            //     listener.onWorkoutLongClick(workout);
            //     return true; // Consumes the long click
            // });
        }
    }
}