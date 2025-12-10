package com.example.fittness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TodoList extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList; // Changé de ArrayList<TaskInfo> à List<Task>
    private Button addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        recyclerView = findViewById(R.id.tasksRecyclerView);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Initialiser avec List<Task> au lieu de ArrayList<TaskInfo>
        taskList = new ArrayList<>();


        taskList.get(1).setDueDate("16/12/2023");
        taskList.get(1).setDueTime("14:00");
        taskList.get(1).setEstimatedMinutes(120);

        adapter = new TaskAdapter(taskList, this); // ✅ Maintenant compatible
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addTaskButton.setOnClickListener(v -> openTaskActivity(null));
    }

    private void openTaskActivity(Task task) {
        // Remplacez Task.class par le nom réel de votre activité d'édition
        Intent intent = new Intent(this, TaskActivity.class);
        if (task != null) {
            int index = taskList.indexOf(task);
            intent.putExtra("TASK_INDEX", index);
            intent.putExtra("TASK_ID", task.getId());
            intent.putExtra("TASK_TITLE", task.getTitle());
            intent.putExtra("TASK_DATE", task.getDueDate());
            intent.putExtra("TASK_TIME", task.getDueTime());
            intent.putExtra("TASK_EST_MIN", task.getEstimatedMinutes());
        } else {
            intent.putExtra("TASK_INDEX", -1);
        }
        startActivityForResult(intent, 100);
    }

    // ✅ Implémentation CORRECTE de l'interface (avec Task, pas TaskInfo)
    @Override
    public void onTaskChecked(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        adapter.notifyItemChanged(taskList.indexOf(task));
        Toast.makeText(this, task.getTitle() + (isChecked ? " complétée" : " à faire"),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskDeleted(Task task) {
        int index = taskList.indexOf(task);
        taskList.remove(index);
        adapter.notifyItemRemoved(index);
        Toast.makeText(this, "Tâche supprimée", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartPomodoro(Task task) {
        Toast.makeText(this, "Pomodoro démarré pour: " + task.getTitle(),
                Toast.LENGTH_SHORT).show();
        // Démarrer l'activité Pomodoro
        Intent intent = new Intent(this, PomodoroTimerActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());
        startActivity(intent);
    }

    @Override
    public void onTaskClicked(Task task) {
        openTaskActivity(task);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int index = data.getIntExtra("TASK_INDEX", -1);
            String title = data.getStringExtra("TASK_TITLE");
            String date = data.getStringExtra("TASK_DATE");
            String time = data.getStringExtra("TASK_TIME");
            int estMinutes = data.getIntExtra("TASK_EST_MIN", 0);

            if (index == -1) {
                // Nouvelle tâche
                Task newTask = new Task(title);
                newTask.setDueDate(date);
                newTask.setDueTime(time);
                newTask.setEstimatedMinutes(estMinutes);
                taskList.add(newTask);
                adapter.notifyItemInserted(taskList.size() - 1);
            } else {
                // Modification d'une tâche existante
                Task task = taskList.get(index);
                task.setTitle(title);
                task.setDueDate(date);
                task.setDueTime(time);
                task.setEstimatedMinutes(estMinutes);
                adapter.notifyItemChanged(index);
            }
        }
    }

    // Supprimez la classe interne TaskInfo car vous utilisez Task maintenant
    // public static class TaskInfo { ... } // À SUPPRIMER
}