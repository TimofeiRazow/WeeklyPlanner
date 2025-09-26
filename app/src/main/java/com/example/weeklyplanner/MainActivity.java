// MainActivity.java
package com.example.weeklyplanner;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button dateButton;
    private ImageButton prevWeekButton;
    private ImageButton nextWeekButton;
    private Calendar currentWeekCalendar;
    private WeekViewFragment weekViewFragment;
    private WeekTasksFragment weekTasksFragment;
    private TaskDatabase database;
    private ViewPagerAdapter adapter;
    private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate started");

        // Инициализируем календарь сразу
        currentWeekCalendar = Calendar.getInstance();

        // Инициализируем базу данных
        try {
            database = TaskDatabase.getInstance(this);
            Log.d("MainActivity", "Database created: " + (database != null));

            if (database != null) {
                Log.d("MainActivity", "TaskDao: " + (database.taskDao() != null));
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Database initialization failed", e);
        }

        initViews();
        setupViewPager();
        updateDateButton();

        // Проверяем, первый ли это запуск
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("first_launch_done", false)) {
            showDatePicker();
            prefs.edit().putBoolean("first_launch_done", true).apply();
        }

        Log.d("MainActivity", "onCreate finished");
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        dateButton = findViewById(R.id.dateButton);
        prevWeekButton = findViewById(R.id.prevWeekButton);
        nextWeekButton = findViewById(R.id.nextWeekButton);

        dateButton.setOnClickListener(v -> showDatePicker());
        prevWeekButton.setOnClickListener(v -> navigateWeek(-1));
        nextWeekButton.setOnClickListener(v -> navigateWeek(1));


        fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }
    private void showAddTaskDialog() {
        AddTaskBottomSheet dialog = new AddTaskBottomSheet();
        dialog.setCurrentWeek(getCurrentWeek());
        dialog.show(this.getSupportFragmentManager(), "AddTaskBottomSheet");
    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    currentWeekCalendar.set(year, month, dayOfMonth);
                    updateCurrentWeek();
                    updateDateButton();
                    refreshFragments();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void navigateWeek(int weeks) {
        currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, weeks);
        updateDateButton();
        refreshFragments();
    }

    private void updateCurrentWeek() {
        // Устанавливаем на понедельник текущей недели
        int dayOfWeek = currentWeekCalendar.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        currentWeekCalendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract);
    }

    private void updateDateButton() {
        updateCurrentWeek();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Calendar endWeek = (Calendar) currentWeekCalendar.clone();
        endWeek.add(Calendar.DAY_OF_YEAR, 6);

        String weekRange = sdf.format(currentWeekCalendar.getTime()) + " - " + sdf.format(endWeek.getTime());
        dateButton.setText(weekRange);
    }

    private void refreshFragments() {
        if (weekTasksFragment != null) {
            weekTasksFragment.updateWeek(currentWeekCalendar);
        }
        if (weekViewFragment != null) {
            weekViewFragment.updateWeek(currentWeekCalendar);
        }
    }

    public Calendar getCurrentWeek() {
        return (Calendar) currentWeekCalendar.clone();
    }

    public TaskDatabase getDatabase() {
        if (database == null) {
            Log.w("MainActivity", "Database is null, recreating...");
            database = TaskDatabase.getInstance(this);
        }
        return database;
    }

    public void setWeekViewFragment(WeekViewFragment fragment) {
        this.weekViewFragment = fragment;
    }

    public void setWeekTasksFragment(WeekTasksFragment fragment) {
        this.weekTasksFragment = fragment;
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.d("MainActivity", "Creating fragment at position: " + position);
            if (position == 0) {
                weekViewFragment = new WeekViewFragment();
                return weekViewFragment;
            } else {
                weekTasksFragment = new WeekTasksFragment();
                return weekTasksFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}