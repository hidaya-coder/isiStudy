package com.example.isiStudy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MistralClient {

    private static final String API_URL =
            "https://api.mistral.ai/v1/chat/completions";

    public static String MISTRAL_API_KEY = "CHF3yp9NMhMXwTuLrLe5nWBoNqUzSqsU";

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();

    public static String chat(String prompt) throws IOException, JSONException {

        JSONObject body = new JSONObject();
        JSONArray messages = new JSONArray();

        // System message (coach behavior)
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "You are a friendly fitness coach. " +
                                "Give concise, motivating workout advice. " +
                                "Max 50 words.")
        );

        // User message
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt)
        );

        body.put("model", "mistral-small-latest");
        body.put("messages", messages);
        body.put("temperature", 0.7);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization",
                        "Bearer " + MISTRAL_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }

            JSONObject json = new JSONObject(response.body().string());
            return json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}