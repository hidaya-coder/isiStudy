package com.example.fittness;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoteManager {
    private static final String PREFS_NAME = "NotesPrefs";
    private static final String KEY_NOTES = "notes";
    private SharedPreferences prefs;
    private long nextId = 1;

    public NoteManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadNotes(); // Initialize nextId
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String notesJson = prefs.getString(KEY_NOTES, "[]");
        try {
            JSONArray jsonArray = new JSONArray(notesJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                long id = obj.getLong("id");
                String title = obj.getString("title");
                String content = obj.getString("content");
                long timestamp = obj.getLong("timestamp");
                notes.add(new Note(id, title, content, timestamp));
                if (id >= nextId) {
                    nextId = id + 1;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public void addNote(String title, String content) {
        List<Note> notes = getAllNotes();
        Note note = new Note(nextId++, title, content, System.currentTimeMillis());
        notes.add(0, note); // Add at beginning
        saveNotes(notes);
    }

    public void updateNote(Note note) {
        List<Note> notes = getAllNotes();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId() == note.getId()) {
                note.setTimestamp(System.currentTimeMillis()); // Update timestamp
                notes.set(i, note);
                break;
            }
        }
        saveNotes(notes);
    }

    public void deleteNote(Note note) {
        List<Note> notes = getAllNotes();
        notes.removeIf(n -> n.getId() == note.getId());
        saveNotes(notes);
    }

    private void saveNotes(List<Note> notes) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Note note : notes) {
                JSONObject obj = new JSONObject();
                obj.put("id", note.getId());
                obj.put("title", note.getTitle());
                obj.put("content", note.getContent());
                obj.put("timestamp", note.getTimestamp());
                jsonArray.put(obj);
            }
            prefs.edit().putString(KEY_NOTES, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadNotes() {
        List<Note> notes = getAllNotes();
        // nextId is already set in getAllNotes
    }
}

