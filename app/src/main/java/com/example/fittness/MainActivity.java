package com.example.fittness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView subtitle = findViewById(R.id.subtitle);
        String subtitleText = "Let’s Go To <br>Pomodoro <br><font color='#236DF6'>Study</font> Time.";
        subtitle.setText(Html.fromHtml(subtitleText));

        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this,Login.class);
                        startActivity(intent);
                    }
                }
        );
    }
}