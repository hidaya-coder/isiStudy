package com.example.fittness;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SettingsDialogFragment extends DialogFragment {
    private EditText focusMinutesEdit;
    private EditText shortRestMinutesEdit;
    private EditText longRestMinutesEdit;
    private EditText cyclesEdit;
    private Button saveButton;
    private Button cancelButton;

    private SettingsListener listener;

    public interface SettingsListener {
        void onSettingsChanged(long focusMs, long shortRestMs, long longRestMs, int cycles);
    }

    public void setSettingsListener(SettingsListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_settings, container, false);

        focusMinutesEdit = view.findViewById(R.id.focusMinutesEdit);
        shortRestMinutesEdit = view.findViewById(R.id.shortRestMinutesEdit);
        longRestMinutesEdit = view.findViewById(R.id.longRestMinutesEdit);
        cyclesEdit = view.findViewById(R.id.cyclesEdit);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Load current values from arguments
        Bundle args = getArguments();
        if (args != null) {
            long focusMs = args.getLong("focus", 25 * 60 * 1000);
            long shortRestMs = args.getLong("shortRest", 5 * 60 * 1000);
            long longRestMs = args.getLong("longRest", 20 * 60 * 1000);
            int cycles = args.getInt("cycles", 4);

            focusMinutesEdit.setText(String.valueOf(focusMs / 60000));
            shortRestMinutesEdit.setText(String.valueOf(shortRestMs / 60000));
            longRestMinutesEdit.setText(String.valueOf(longRestMs / 60000));
            cyclesEdit.setText(String.valueOf(cycles));
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int focusMinutes = Integer.parseInt(focusMinutesEdit.getText().toString());
                    int shortRestMinutes = Integer.parseInt(shortRestMinutesEdit.getText().toString());
                    int longRestMinutes = Integer.parseInt(longRestMinutesEdit.getText().toString());
                    int cycles = Integer.parseInt(cyclesEdit.getText().toString());

                    if (focusMinutes <= 0 || shortRestMinutes <= 0 || longRestMinutes <= 0 || cycles <= 0) {
                        Toast.makeText(getContext(), "All values must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long focusMs = focusMinutes * 60 * 1000L;
                    long shortRestMs = shortRestMinutes * 60 * 1000L;
                    long longRestMs = longRestMinutes * 60 * 1000L;

                    if (listener != null) {
                        listener.onSettingsChanged(focusMs, shortRestMs, longRestMs, cycles);
                    }

                    dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }
}

