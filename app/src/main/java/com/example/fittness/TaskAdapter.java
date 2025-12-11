package com.example.fittness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskChecked(Task task, boolean isChecked);
        void onTaskDeleted(Task task);
        void onStartPomodoro(Task task);
        void onTaskClicked(Task task);
    }

    private List<Task> tasks;
    private final OnTaskClickListener listener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void setTasks(List<Task> updatedTasks) {
        this.tasks = updatedTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCheckBox;
        TextView taskTitle, dueDateText, dueTimeText, estimatedMinutesText;
        ImageButton deleteButton, editButton;
        Button startPomodoroButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            dueTimeText = itemView.findViewById(R.id.dueTimeText);
            estimatedMinutesText = itemView.findViewById(R.id.estimatedMinutesText);
            deleteButton = itemView.findViewById(R.id.deleteTaskButton);
            editButton = itemView.findViewById(R.id.editTaskButton);
            startPomodoroButton = itemView.findViewById(R.id.startPomodoroButton);
        }

        public void bind(Task task) {
            if (task == null) return;

            // Reset listener
            taskCheckBox.setOnCheckedChangeListener(null);

            // Bind data
            taskCheckBox.setChecked(task.isCompleted());
            taskTitle.setText(task.getTitle());

            // Set date - handle null or empty
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                dueDateText.setText(task.getDueDate());
            } else {
                dueDateText.setText("DD/MM/YYYY");
            }

            // Set time - handle null or empty
            if (task.getDueTime() != null && !task.getDueTime().isEmpty()) {
                dueTimeText.setText(task.getDueTime());
            } else {
                dueTimeText.setText("HH:MM");
            }

            // Set estimated minutes
            if (task.getEstimatedMinutes() > 0) {
                estimatedMinutesText.setText(task.getEstimatedMinutes() + " min");
            } else {
                estimatedMinutesText.setText("0 Min");
            }

            // Apply strike-through for completed tasks
            if (task.isCompleted()) {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                dueDateText.setPaintFlags(dueDateText.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                dueTimeText.setPaintFlags(dueTimeText.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                estimatedMinutesText.setPaintFlags(estimatedMinutesText.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                dueDateText.setPaintFlags(dueDateText.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                dueTimeText.setPaintFlags(dueTimeText.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                estimatedMinutesText.setPaintFlags(estimatedMinutesText.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }

            // Listeners
            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskChecked(task, isChecked);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskDeleted(task);
                }
            });

            if (editButton != null) {
                editButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTaskClicked(task);
                    }
                });
            }

            startPomodoroButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStartPomodoro(task);
                }
            });

            // Clic sur tout l'item pour éditer
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClicked(task);
                }
            });
        }
    }
}