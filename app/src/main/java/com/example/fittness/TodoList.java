package com.example.fittness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TodoList extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskManager taskManager;
    private Button addTaskButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        taskManager = new TaskManager(this);

        recyclerView = findViewById(R.id.tasksRecyclerView);
        addTaskButton = findViewById(R.id.addTaskButton);
        backButton = findViewById(R.id.backButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initial load
        refreshTasks();

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void refreshTasks() {
        List<Task> tasks = taskManager.getAllTasks();
        if (tasks == null) tasks = new ArrayList<>();
        
        if (adapter == null) {
            adapter = new TaskAdapter(tasks, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setTasks(tasks);
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TodoList.this, TaskActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onTaskChecked(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        taskManager.updateTask(task);
        refreshTasks();
    }

    @Override
    public void onTaskDeleted(Task task) {
        taskManager.deleteTask(task);
        refreshTasks();
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartPomodoro(Task task) {
        Intent intent = new Intent(TodoList.this, PomodoroTimerActivity.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("taskTitle", task.getTitle());
        startActivity(intent);
    }

    @Override
    public void onTaskClicked(Task task) {
        Intent intent = new Intent(TodoList.this, TaskActivity.class);
        intent.putExtra("isEditMode", true);
        intent.putExtra("taskId", task.getId());
        startActivity(intent);
    }
}