package com.example.fittness;

public class Note {
    private long id;
    private String title;
    private String content;
    private long timestamp;
    private String imagePath;

    public Note(long id, String title, String content, long timestamp, String imagePath) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.imagePath = imagePath;
    }
    
    // Constructor for backward compatibility if needed, or just update callers
    public Note(long id, String title, String content, long timestamp) {
        this(id, title, content, timestamp, null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}

