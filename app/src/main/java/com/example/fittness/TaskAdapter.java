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
        void onTaskChecked(int index, boolean isChecked);
        void onTaskDeleted(int index);
        void onTaskClicked(int index);
    }

    private final List<TodoList.TaskInfo> tasks;
    private final OnTaskClickListener listener;

    public TaskAdapter(List<TodoList.TaskInfo> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
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
        holder.bind(tasks.get(position), position);
    }

    @Override
    public int getItemCount() { return tasks.size(); }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCheckBox;
        TextView taskTitle, dueDateText, dueTimeText, estimatedMinutesText;
        ImageButton deleteButton;
        Button openTaskButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            dueDateText = itemView.findViewById(R.id.taskDateEditText);
            dueTimeText = itemView.findViewById(R.id.dueTimeText);
            estimatedMinutesText = itemView.findViewById(R.id.estimatedMinutesText);
            deleteButton = itemView.findViewById(R.id.deleteTaskButton);
            openTaskButton = itemView.findViewById(R.id.startPomodoroButton);
        }

        public void bind(TodoList.TaskInfo task, int index) {
            taskCheckBox.setOnCheckedChangeListener(null);
            taskCheckBox.setChecked(task.isCompleted);
            taskTitle.setText(task.title);
            dueDateText.setText(task.dueDate);
            dueTimeText.setText(task.dueTime);
            estimatedMinutesText.setText(task.estimatedMinutes + " min");

            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onTaskChecked(index, isChecked));
            deleteButton.setOnClickListener(v -> listener.onTaskDeleted(index));
            openTaskButton.setOnClickListener(v -> listener.onTaskClicked(index));
        }
    }
}
