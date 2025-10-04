package com.example.fittness;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Home extends AppCompatActivity {

    private TextView timerText;
    private ProgressBar progressBar;
    private Button startButton;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private final int totalTimeInMillis = 5 * 60 * 1000;
    private int secondsPassed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro_timer);

        timerText = findViewById(R.id.timerText);
        progressBar = findViewById(R.id.circleProgress);
        startButton = findViewById(R.id.stopButton);

        progressBar.setMax(totalTimeInMillis / 1000);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }

                secondsPassed = 0;
                progressBar.setProgress(0);
                timerText.setText("05:00");

                int remainingMillis = totalTimeInMillis;
                int finalRemainingMillis = remainingMillis;

                countDownTimer = new CountDownTimer(finalRemainingMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int minutes = (int) (millisUntilFinished / 1000) / 60;
                        int seconds = (int) (millisUntilFinished / 1000) % 60;

                        String time = String.format("%02d:%02d", minutes, seconds);
                        timerText.setText(time);

                        secondsPassed++;
                        progressBar.setProgress(secondsPassed);
                    }

                    @Override
                    public void onFinish() {
                        timerText.setText("00:00");
                        isRunning = false;
                    }
                }.start();

                isRunning = true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (secondsPassed > 0 && secondsPassed < totalTimeInMillis / 1000) {
            int remainingMillis = totalTimeInMillis - (secondsPassed * 1000);
            int finalRemainingMillis = remainingMillis;

            countDownTimer = new CountDownTimer(finalRemainingMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int minutes = (int) (millisUntilFinished / 1000) / 60;
                    int seconds = (int) (millisUntilFinished / 1000) % 60;

                    String time = String.format("%02d:%02d", minutes, seconds);
                    timerText.setText(time);

                    secondsPassed++;
                    progressBar.setProgress(secondsPassed);
                }

                @Override
                public void onFinish() {
                    timerText.setText("00:00");
                    isRunning = false;
                }
            }.start();

            isRunning = true;
        }
    }
}
