// WeekViewFragment.java - ДИАГНОСТИЧЕСКАЯ ВЕРСИЯ
package com.example.weeklyplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeekViewFragment extends Fragment {
    private static final String TAG = "WeekViewFragment";

    private TableLayout weekTable;
    private Calendar currentWeek;
    private TaskDatabase database;


    private String[] dayNames = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "=== onCreateView STARTED ===");

        View view = inflater.inflate(R.layout.fragment_week_view, container, false);
        Log.d(TAG, "Layout inflated successfully");

        // Проверяем что элементы найдены
        weekTable = view.findViewById(R.id.weekTable);

        Log.d(TAG, "weekTable found: " + (weekTable != null));
        database = TaskDatabase.getInstance(getContext());
        Log.d(TAG, "База данных найдена? : " + (database != null));

        currentWeek = ((MainActivity) getActivity()).getCurrentWeek();
        ((MainActivity) getActivity()).setWeekViewFragment(this);

        if (currentWeek == null) {
            Log.w(TAG, "CurrentWeek is null, using default");
            currentWeek = Calendar.getInstance();
        }
        setupWeekView();

        Log.d(TAG, "=== onCreateView FINISHED ===");
        return view;
    }


    private void setupWeekView() {
        Log.d(TAG, "=== setupWeekView STARTED ===");

        if (weekTable == null || currentWeek == null) {
            Log.e(TAG, "weekTable or currentWeek is null");
            return;
        }

        weekTable.removeAllViews();

        Calendar cal = (Calendar) currentWeek.clone();
        Log.d(TAG, "Starting from date: " + cal.getTime());

        for (int i = 0; i < 7; i++) {
            Log.d(TAG, "Creating row for day " + i + ": " + cal.getTime());
            TableRow dayRow = createDayRow(cal.getTime(), i);
            if (dayRow != null) {
                weekTable.addView(dayRow);
                Log.d(TAG, "Row " + i + " added successfully");
            } else {
                Log.e(TAG, "Row " + i + " is null!");
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Log.d(TAG, "Final table child count: " + weekTable.getChildCount());
        Log.d(TAG, "=== setupWeekView FINISHED ===");
    }

    @SuppressLint("ResourceType")
    private TableRow createDayRow(Date date, int dayIndex) {
        Log.d(TAG, "Creating day row for: " + date + ", index: " + dayIndex);

        if (getContext() == null) {
            Log.e(TAG, "Context is null in createDayRow");
            return null;
        }

        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        row.setMinimumHeight(dpToPx(80));

        try {
            android.util.TypedValue outValue = new android.util.TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            row.setBackgroundResource(outValue.resourceId);
        } catch (Exception e) {
            Log.w(TAG, "Could not set selectable background", e);
        }

        // Day column
        LinearLayout dayColumn = createDayColumn(date, dayIndex);
        if (dayColumn == null) {
            Log.e(TAG, "dayColumn is null");
            return null;
        }

        TableRow.LayoutParams dayParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f);
        dayColumn.setLayoutParams(dayParams);

        // Timed tasks column
        LinearLayout timedColumn = createTaskColumn();
        if (timedColumn == null) {
            Log.e(TAG, "timedColumn is null");
            return null;
        }

        TableRow.LayoutParams timedParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 2f);
        timedColumn.setLayoutParams(timedParams);

        // Daily tasks column
        LinearLayout dailyColumn = createTaskColumn();
        if (dailyColumn == null) {
            Log.e(TAG, "dailyColumn is null");
            return null;
        }

        TableRow.LayoutParams dailyParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 2f);
        dailyColumn.setLayoutParams(dailyParams);

        // Add borders - используем цвета из предоставленных файлов
        dayColumn.setBackgroundResource(R.drawable.task_container_background);
        timedColumn.setBackgroundResource(R.drawable.task_container_background);
        dailyColumn.setBackgroundResource(R.drawable.task_container_background);

        // Load tasks for this day
        if (database != null) {
            loadTasksForDay(date, timedColumn, dailyColumn);
        } else {
            Log.w(TAG, "Database is null, cannot load tasks");
        }

        // Set click listener for the entire row
        final Date finalDate = date;
        row.setOnClickListener(v -> openDayView(finalDate));

        row.addView(dayColumn);
        row.addView(timedColumn);
        row.addView(dailyColumn);

        Log.d(TAG, "Day row created successfully");
        return row;
    }

    private LinearLayout createDayColumn(Date date, int dayIndex) {
        if (getContext() == null) {
            Log.e(TAG, "Context is null in createDayColumn");
            return null;
        }

        LinearLayout dayColumn = new LinearLayout(getContext());
        dayColumn.setOrientation(LinearLayout.VERTICAL);
        dayColumn.setGravity(android.view.Gravity.CENTER);
        dayColumn.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        TextView dayLabel = new TextView(getContext());
        dayLabel.setText(dayNames[dayIndex]);
        dayLabel.setTextSize(14);
        dayLabel.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView dateLabel = new TextView(getContext());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        dateLabel.setText(sdf.format(date));
        dateLabel.setTextSize(12);

        dayColumn.addView(dayLabel);
        dayColumn.addView(dateLabel);

        return dayColumn;
    }

    private LinearLayout createTaskColumn() {
        if (getContext() == null) {
            Log.e(TAG, "Context is null in createTaskColumn");
            return null;
        }

        LinearLayout taskColumn = new LinearLayout(getContext());
        taskColumn.setOrientation(LinearLayout.VERTICAL);
        taskColumn.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        return taskColumn;
    }

    private void loadTasksForDay(Date date, LinearLayout timedContainer, LinearLayout dailyContainer) {
        Log.d(TAG, "Loading tasks for date: " + date);

        if (database == null || !isAdded()) {
            Log.w(TAG, "Cannot load tasks - database is null or fragment not added");
            return;
        }

        try {
            Date[] bounds = getDayBounds(date);

            database.taskDao().getTimedTasksForDay(bounds[0], bounds[1]).observe(getViewLifecycleOwner(), timedTasks -> {
                Log.d(TAG, "Временные задания найдены? " + (timedTasks != null ? (" Да " + timedTasks.size()) : timedTasks.size()));
                if (!isAdded() || timedContainer == null) return;

                Log.d(TAG, "Received timed tasks: " + (timedTasks != null ? timedTasks.size() : 0));

                timedContainer.removeAllViews();
                if (timedTasks != null && !timedTasks.isEmpty()) {
                    for (Task task : timedTasks) {
                        View taskView = createTaskView(task, true);
                        if (taskView != null) {
                            timedContainer.addView(taskView);
                        }
                    }
                } else {
                    // Добавляем тестовый элемент если задач нет
                    TextView testView = new TextView(getContext());
                    testView.setText("Нет задач");
                    testView.setTextSize(10);
                    timedContainer.addView(testView);
                }
            });

            database.taskDao().getTasksForDay(bounds[0], bounds[1]).observe(getViewLifecycleOwner(), dailyTasks -> {
                if (!isAdded() || dailyContainer == null) return;

                Log.d(TAG, "Received daily tasks: " + (dailyTasks != null ? dailyTasks.size() : 0));

                dailyContainer.removeAllViews();
                if (dailyTasks != null && dailyTasks.size() > 0) {
                    for (Task task : dailyTasks) {
                        View taskView = createTaskView(task, false);
                        if (taskView != null) {
                            dailyContainer.addView(taskView);
                        }
                    }
                } else {
                    // Добавляем тестовый элемент если задач нет
                    TextView testView = new TextView(getContext());
                    testView.setText("Нет задач");
                    testView.setTextSize(10);
                    dailyContainer.addView(testView);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading tasks", e);
        }
    }

    private View createTaskView(Task task, boolean showTime) {
        if (getContext() == null) {
            Log.e(TAG, "Context is null in createTaskView");
            return null;
        }

        View taskView = LayoutInflater.from(getContext()).inflate(R.layout.task_item_mini, null);

        TextView taskTitle = taskView.findViewById(R.id.taskTitle);
        TextView taskTime = taskView.findViewById(R.id.taskTime);

        if (taskTitle != null) {
            taskTitle.setText(task.title);
        }

        if (taskTime != null) {
            if (showTime && task.time != null) {
                taskTime.setText(task.time);
                taskTime.setVisibility(View.VISIBLE);
            } else {
                taskTime.setVisibility(View.GONE);
            }
        }

        if (task.isCompleted) {
            if (taskTitle != null) taskTitle.setAlpha(0.6f);
            if (taskTime != null) taskTime.setAlpha(0.6f);
        }

        // Add margin for better spacing
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(2), 0, dpToPx(2));
        taskView.setLayoutParams(params);

        return taskView;
    }

    private void openDayView(Date date) {
        Log.d(TAG, "Opening day view for: " + date);

        if (getContext() == null) return;

        Intent intent = new Intent(getContext(), DayViewActivity.class);
        intent.putExtra("selected_date", date.getTime());
        startActivity(intent);
    }

    private void showAddTaskDialog() {
        Log.d(TAG, "Showing add task dialog");

        if (getParentFragmentManager() == null || currentWeek == null) {
            Log.w(TAG, "Cannot show dialog - fragmentManager or currentWeek is null");
            return;
        }

        AddTaskBottomSheet dialog = new AddTaskBottomSheet();
        dialog.setCurrentWeek(currentWeek);
        dialog.show(getParentFragmentManager(), "AddTaskBottomSheet");
    }

    public void updateWeek(Calendar newWeek) {
        Log.d(TAG, "updateWeek called with: " + (newWeek != null ? newWeek.getTime() : "null"));

        this.currentWeek = newWeek;
        if (isAdded() && weekTable != null) {
            setupWeekView();
        }
    }

    private int dpToPx(int dp) {
        if (getContext() == null) {
            return dp; // fallback
        }
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    public static Date[] getDayBounds(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // начало дня
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        // конец дня
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfDay = cal.getTime();

        return new Date[]{startOfDay, endOfDay};
    }

}