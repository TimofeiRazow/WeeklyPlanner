// TaskAdapter.java
package com.example.weeklyplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnTaskClickListener clickListener;
    private OnTaskLongClickListener longClickListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskLongClickListener {
        void onTaskLongClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener clickListener, OnTaskLongClickListener longClickListener) {
        this.tasks = tasks;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private TextView titleText;
        private TextView descriptionText;
        private TextView timeText;
        private TextView dateText;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskCheckBox);
            titleText = itemView.findViewById(R.id.taskTitle);
            descriptionText = itemView.findViewById(R.id.taskDescription);
            timeText = itemView.findViewById(R.id.taskTime);
            dateText = itemView.findViewById(R.id.taskDate);
        }

        public void bind(Task task) {
            checkBox.setChecked(task.isCompleted);
            titleText.setText(task.title);

            if (task.description != null && !task.description.isEmpty()) {
                descriptionText.setText(task.description);
                descriptionText.setVisibility(View.VISIBLE);
            } else {
                descriptionText.setVisibility(View.GONE);
            }

            if (task.isTimedTask() && task.time != null) {
                timeText.setText(task.time);
                timeText.setVisibility(View.VISIBLE);
            } else {
                timeText.setVisibility(View.GONE);
            }

            // Разное отображение даты для разных типов задач
            if (task.isWeeklyTask()) {
                dateText.setText("На всю неделю");
                dateText.setBackgroundResource(R.drawable.weekly_task_background);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
                dateText.setText(sdf.format(task.date));
                dateText.setBackgroundResource(R.drawable.date_background);
            }

            checkBox.setOnClickListener(v -> clickListener.onTaskClick(task));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onTaskLongClick(task);
                return true;
            });
        }
    }
}