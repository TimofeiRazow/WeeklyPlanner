// DayViewActivity.java
package com.example.weeklyplanner;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DayViewActivity extends AppCompatActivity {
    private TextView dayTitle;
    private RecyclerView dailyTasksRecycler;
    private RecyclerView timedTasksRecycler;
    private TextView dailyTasksLabel;
    private TextView timedTasksLabel;
    private TaskAdapter dailyTasksAdapter;
    private TaskAdapter timedTasksAdapter;
    private FloatingActionButton fabAddTask;

    private Date selectedDate;
    private TaskDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        initViews();

        database = TaskDatabase.getInstance(this);

        // Получаем выбранную дату
        long dateMillis = getIntent().getLongExtra("selected_date", System.currentTimeMillis());
        selectedDate = new Date(dateMillis);

        setupTitle();
        setupRecyclerViews();
        loadTasks();

        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    private void initViews() {
        dayTitle = findViewById(R.id.dayTitle);
        dailyTasksRecycler = findViewById(R.id.dailyTasksRecycler);
        timedTasksRecycler = findViewById(R.id.timedTasksRecycler);
        dailyTasksLabel = findViewById(R.id.dailyTasksLabel);
        timedTasksLabel = findViewById(R.id.timedTasksLabel);
        fabAddTask = findViewById(R.id.fabAddTask);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        dayTitle.setText(sdf.format(selectedDate));
    }

    private void setupRecyclerViews() {
        dailyTasksAdapter = new TaskAdapter(new ArrayList<>(), this::onTaskClick, this::onTaskLongClick);
        timedTasksAdapter = new TaskAdapter(new ArrayList<>(), this::onTaskClick, this::onTaskLongClick);

        dailyTasksRecycler.setLayoutManager(new LinearLayoutManager(this));
        dailyTasksRecycler.setAdapter(dailyTasksAdapter);

        timedTasksRecycler.setLayoutManager(new LinearLayoutManager(this));
        timedTasksRecycler.setAdapter(timedTasksAdapter);
    }

    private void loadTasks() {
        Date[] bounds = WeekViewFragment.getDayBounds(selectedDate);
        // Загружаем задачи на день без времени
        database.taskDao().getDailyTasksForDay(bounds[0], bounds[1]).observe(this, tasks -> {
            if (tasks != null) {
                dailyTasksAdapter.updateTasks(tasks);
                dailyTasksLabel.setText("Задачи на день (" + tasks.size() + ")");
            }
        });

        // Загружаем задачи со временем
        database.taskDao().getTimedTasksForDay(bounds[0], bounds[1]).observe(this, tasks -> {
            if (tasks != null) {
                timedTasksAdapter.updateTasks(tasks);
                timedTasksLabel.setText("По времени (" + tasks.size() + ")");
            }
        });
    }

    private void onTaskClick(Task task) {
        task.isCompleted = !task.isCompleted;
        new Thread(() -> database.taskDao().update(task)).start();
    }

    private void onTaskLongClick(Task task) {
        EditTaskBottomSheet dialog = new EditTaskBottomSheet();
        dialog.setTask(task);
        dialog.show(getSupportFragmentManager(), "EditTaskBottomSheet");
    }

    private void showAddTaskDialog() {
        AddTaskBottomSheet dialog = new AddTaskBottomSheet();
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        dialog.setCurrentWeek(cal);
        dialog.show(getSupportFragmentManager(), "AddTaskBottomSheet");
    }
}