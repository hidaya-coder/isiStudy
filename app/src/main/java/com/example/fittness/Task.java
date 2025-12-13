package com.example.fittness;

/**
 * Task model used by TaskManager, TaskAdapter and Home.
 */
public class Task {
    private long id;
    private String title;
    private boolean completed;
    private String dueDate = "";
    private String dueTime = "";
    private int completedMinutes = 0;
    private int estimatedMinutes = 0;
    private long linkedPomodoroSessionId = -1L;

    // Constructors
    public Task(long id, String title, boolean completed) {
        this.id = id;
        this.title = title != null ? title : "";
        this.completed = completed;
    }

    public Task(String title) {
        this(System.currentTimeMillis(), title, false);
    }

    // Getters / Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title != null ? title : ""; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate != null ? dueDate : ""; }

    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime != null ? dueTime : ""; }

    public int getCompletedMinutes() { return completedMinutes; }
    public void setCompletedMinutes(int completedMinutes) { this.completedMinutes = completedMinutes; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public long getLinkedPomodoroSessionId() { return linkedPomodoroSessionId; }
    public void setLinkedPomodoroSessionId(long linkedPomodoroSessionId) {
        this.linkedPomodoroSessionId = linkedPomodoroSessionId;
    }

    private int breakMinutes = 5;
    public int getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(int breakMinutes) { this.breakMinutes = breakMinutes; }
}
