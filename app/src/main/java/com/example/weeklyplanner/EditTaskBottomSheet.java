// EditTaskBottomSheet.java
package com.example.weeklyplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private Button dateButton;
    private Button timeButton;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;
    private Switch timedTaskSwitch;
    private Switch weeklyTaskSwitch;
    private LinearLayout timeContainer;
    private LinearLayout dateContainer;

    private Calendar selectedDate;
    private String selectedTime;
    private Task currentTask;
    private TaskDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_edit_task, container, false);

        initViews(view);
        setupListeners(view);

        database = TaskDatabase.getInstance(requireContext());

        if (currentTask != null) {
            populateFields();
        }

        return view;
    }

    private void initViews(View view) {
        titleInput = view.findViewById(R.id.titleInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        dateButton = view.findViewById(R.id.dateButton);
        timeButton = view.findViewById(R.id.timeButton);
        saveButton = view.findViewById(R.id.saveButton);
        deleteButton = view.findViewById(R.id.deleteButton);
        timedTaskSwitch = view.findViewById(R.id.timedTaskSwitch);
        weeklyTaskSwitch = view.findViewById(R.id.weeklyTaskSwitch);
        timeContainer = view.findViewById(R.id.timeContainer);
        dateContainer = view.findViewById(R.id.dateContainer);
    }

    private void setupListeners(View view) {
        weeklyTaskSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                timedTaskSwitch.setChecked(false);
                timeContainer.setVisibility(View.GONE);
                dateContainer.setVisibility(View.GONE);
                selectedTime = null;
                timeButton.setText("Выберите время");
            } else {
                dateContainer.setVisibility(View.VISIBLE);
            }
        });

        timedTaskSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                weeklyTaskSwitch.setChecked(false);
                timeContainer.setVisibility(View.VISIBLE);
                dateContainer.setVisibility(View.VISIBLE);
            } else {
                timeContainer.setVisibility(View.GONE);
                selectedTime = null;
                timeButton.setText("Выберите время");
            }
        });

        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> saveTask());
        deleteButton.setOnClickListener(v -> deleteTask());

        view.findViewById(R.id.cancelButton).setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        titleInput.setText(currentTask.title);
        descriptionInput.setText(currentTask.description);

        if (currentTask.isWeeklyTask()) {
            // Недельная задача
            weeklyTaskSwitch.setChecked(true);
            dateContainer.setVisibility(View.GONE);
            timeContainer.setVisibility(View.GONE);
        } else {
            // Обычная задача
            weeklyTaskSwitch.setChecked(false);
            dateContainer.setVisibility(View.VISIBLE);

            selectedDate = Calendar.getInstance();
            selectedDate.setTime(currentTask.date);
            updateDateButton();

            selectedTime = currentTask.time;
            boolean isTimedTask = currentTask.isTimedTask();

            timedTaskSwitch.setChecked(isTimedTask);
            timeContainer.setVisibility(isTimedTask ? View.VISIBLE : View.GONE);

            if (selectedTime != null) {
                timeButton.setText(selectedTime);
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = selectedDate != null ? selectedDate : Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateButton();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    timeButton.setText(selectedTime);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void updateDateButton() {
        if (selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            dateButton.setText(sdf.format(selectedDate.getTime()));
        }
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleInput.setError("Введите название задачи");
            return;
        }

        if (weeklyTaskSwitch.isChecked()) {
            // Сохраняем как недельную задачу
            currentTask.title = title;
            currentTask.description = description;
            currentTask.type = 2;
            currentTask.time = null;
            // weekStartDate остается без изменений
        } else {
            // Сохраняем как обычную задачу
            if (selectedDate == null) {
                Toast.makeText(requireContext(), "Выберите дату", Toast.LENGTH_SHORT).show();
                return;
            }

            if (timedTaskSwitch.isChecked() && selectedTime == null) {
                Toast.makeText(requireContext(), "Выберите время", Toast.LENGTH_SHORT).show();
                return;
            }

            currentTask.title = title;
            currentTask.description = description;
            currentTask.date = selectedDate.getTime();
            currentTask.time = timedTaskSwitch.isChecked() ? selectedTime : null;
            currentTask.type = timedTaskSwitch.isChecked() ? 1 : 0;
            currentTask.weekStartDate = null; // Убираем недельную привязку
        }

        new Thread(() -> database.taskDao().update(currentTask)).start();

        dismiss();
    }

    private void deleteTask() {
        new Thread(() -> database.taskDao().delete(currentTask)).start();
        dismiss();
    }

    public void setTask(Task task) {
        this.currentTask = task;
    }
}