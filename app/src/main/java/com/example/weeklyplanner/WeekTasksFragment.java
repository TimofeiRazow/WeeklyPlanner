// WeekTasksFragment.java
package com.example.weeklyplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeekTasksFragment extends Fragment {
    private RecyclerView weeklyTasksRecycler;
    private RecyclerView dailyTasksRecycler;
    private TaskAdapter weeklyTasksAdapter;
    private TaskAdapter dailyTasksAdapter;
    private EditText noteTitle1, noteDescription1, noteTitle2, noteDescription2;
    private TextView weekRangeText;
    private TextView weeklyTasksLabel;
    private TextView dailyTasksLabel;
    private Calendar currentWeek;
    private TaskDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_tasks, container, false);

        weeklyTasksRecycler = view.findViewById(R.id.weeklyTasksRecycler);
        dailyTasksRecycler = view.findViewById(R.id.dailyTasksRecycler);
        noteTitle1 = view.findViewById(R.id.noteTitle1);
        noteDescription1 = view.findViewById(R.id.noteDescription1);
        noteTitle2 = view.findViewById(R.id.noteTitle2);
        noteDescription2 = view.findViewById(R.id.noteDescription2);
        weekRangeText = view.findViewById(R.id.weekRangeText);
        weeklyTasksLabel = view.findViewById(R.id.weeklyTasksLabel);
        dailyTasksLabel = view.findViewById(R.id.dailyTasksLabel);

        database = TaskDatabase.getInstance(getContext());
        currentWeek = ((MainActivity) getActivity()).getCurrentWeek();

        setupRecyclerViews();
        updateWeekRange();
        loadWeekTasks();

        return view;
    }

    private void setupRecyclerViews() {
        // Адаптер для недельных задач
        weeklyTasksAdapter = new TaskAdapter(new ArrayList<>(), this::onTaskClick, this::onTaskLongClick);
        weeklyTasksRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        weeklyTasksRecycler.setAdapter(weeklyTasksAdapter);

        // Адаптер для дневных задач
        dailyTasksAdapter = new TaskAdapter(new ArrayList<>(), this::onTaskClick, this::onTaskLongClick);
        dailyTasksRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyTasksRecycler.setAdapter(dailyTasksAdapter);
    }

    private void updateWeekRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        Calendar endWeek = (Calendar) currentWeek.clone();
        endWeek.add(Calendar.DAY_OF_YEAR, 6);

        String weekRange = sdf.format(currentWeek.getTime()) + " - " + sdf.format(endWeek.getTime());
        weekRangeText.setText("Неделя: " + weekRange);
    }

    private void loadWeekTasks() {
        Date weekStart = WeekUtils.getWeekStart(currentWeek);
        Date weekEnd = WeekUtils.getWeekEnd(currentWeek);

        // Загружаем недельные задачи
        database.taskDao().getWeeklyTasks(weekStart)
                .observe(getViewLifecycleOwner(), weeklyTasks -> {
                    if (weeklyTasks != null) {
                        weeklyTasksAdapter.updateTasks(weeklyTasks);
                        weeklyTasksLabel.setText("Задачи на неделю (" + weeklyTasks.size() + ")");
                        Log.d("WeekTasksFragment", "Weekly tasks loaded: " + weeklyTasks.size());
                    } else {
                        weeklyTasksLabel.setText("Задачи на неделю (0)");
                        Log.d("WeekTasksFragment", "No weekly tasks found");
                    }
                });

        // Загружаем все остальные задачи недели (дневные и временные)
        database.taskDao().getTasksForWeek(weekStart, weekEnd)
                .observe(getViewLifecycleOwner(), dailyTasks -> {
                    if (dailyTasks != null) {
                        // Фильтруем только не-недельные задачи
                        List<Task> nonWeeklyTasks = new ArrayList<>();
                        for (Task task : dailyTasks) {
                            if (!task.isWeeklyTask()) {
                                nonWeeklyTasks.add(task);
                            }
                        }
                        dailyTasksAdapter.updateTasks(nonWeeklyTasks);
                        dailyTasksLabel.setText("Задачи по дням (" + nonWeeklyTasks.size() + ")");
                    } else {
                        dailyTasksLabel.setText("Задачи по дням (0)");
                    }
                });
    }

    private void onTaskClick(Task task) {
        // Переключаем состояние выполнения задачи
        task.isCompleted = !task.isCompleted;
        new Thread(() -> database.taskDao().update(task)).start();
    }

    private void onTaskLongClick(Task task) {
        // Показываем диалог редактирования
        EditTaskBottomSheet dialog = new EditTaskBottomSheet();
        dialog.setTask(task);
        dialog.show(getParentFragmentManager(), "EditTaskBottomSheet");
    }

    public void updateWeek(Calendar newWeek) {
        this.currentWeek = newWeek;
        if (isAdded()) {
            updateWeekRange();
            loadWeekTasks();
        }
    }
}