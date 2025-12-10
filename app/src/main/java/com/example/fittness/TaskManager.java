package com.example.fittness;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String PREFS_NAME = "TasksPrefs";
    private static final String KEY_TASKS = "tasks";
    private SharedPreferences prefs;
    private long nextId = 1;

    public TaskManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadNextId();
    }

    private void loadNextId() {
        List<Task> tasks = getAllTasks();
        nextId = 1;
        for (Task task : tasks) {
            if (task.getId() >= nextId) {
                nextId = task.getId() + 1;
            }
        }
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String tasksJson = prefs.getString(KEY_TASKS, "[]");
        try {
            JSONArray jsonArray = new JSONArray(tasksJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                long id = obj.getLong("id");
                String title = obj.getString("title");
                boolean completed = obj.getBoolean("completed");
                Task task = new Task(id, title, completed);

                // Load optional fields
                if (obj.has("dueDate")) {
                    task.setDueDate(obj.getString("dueDate"));
                }
                if (obj.has("dueTime")) {
                    task.setDueTime(obj.getString("dueTime"));
                }
                if (obj.has("completedMinutes")) {
                    task.setCompletedMinutes(obj.getInt("completedMinutes"));
                }
                if (obj.has("estimatedMinutes")) {
                    task.setEstimatedMinutes(obj.getInt("estimatedMinutes"));
                }
                if (obj.has("linkedPomodoroSessionId")) {
                    task.setLinkedPomodoroSessionId(obj.getLong("linkedPomodoroSessionId"));
                }

                tasks.add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public void addTask(String title) {
        List<Task> tasks = getAllTasks();
        Task task = new Task(nextId++, title, false);
        tasks.add(task);
        saveTasks(tasks);
    }

    public void addTaskWithDetails(String title, String dueDate, String dueTime,
                                   int estimatedMinutes) {
        List<Task> tasks = getAllTasks();
        Task task = new Task(nextId++, title, false);
        task.setDueDate(dueDate);
        task.setDueTime(dueTime);
        task.setEstimatedMinutes(estimatedMinutes);
        tasks.add(task);
        saveTasks(tasks);
    }

    public void updateTask(Task task) {
        List<Task> tasks = getAllTasks();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == task.getId()) {
                tasks.set(i, task);
                break;
            }
        }
        saveTasks(tasks);
    }

    public void deleteTask(Task task) {
        List<Task> tasks = getAllTasks();
        tasks.removeIf(t -> t.getId() == task.getId());
        saveTasks(tasks);
    }

    public Task getTaskById(long taskId) {
        List<Task> tasks = getAllTasks();
        for (Task task : tasks) {
            if (task.getId() == taskId) {
                return task;
            }
        }
        return null;
    }

    public void deleteCompletedTasks() {
        List<Task> tasks = getAllTasks();
        tasks.removeIf(Task::isCompleted);
        saveTasks(tasks);
    }

    private void saveTasks(List<Task> tasks) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Task task : tasks) {
                JSONObject obj = new JSONObject();
                obj.put("id", task.getId());
                obj.put("title", task.getTitle());
                obj.put("completed", task.isCompleted());
                obj.put("dueDate", task.getDueDate());
                obj.put("dueTime", task.getDueTime());
                obj.put("completedMinutes", task.getCompletedMinutes());
                obj.put("estimatedMinutes", task.getEstimatedMinutes());
                obj.put("linkedPomodoroSessionId", task.getLinkedPomodoroSessionId());
                jsonArray.put(obj);
            }
            prefs.edit().putString(KEY_TASKS, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}