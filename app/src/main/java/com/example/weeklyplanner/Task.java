package com.example.weeklyplanner;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public Date date;
    public String time; // HH:mm format or null for all-day tasks
    public boolean isCompleted;
    public int type; // 0 = daily task, 1 = timed task, 2 = weekly task
    public Date weekStartDate; // для недельных задач - дата начала недели

    @Ignore
    public Task() {}

    public Task(String title, String description, Date date, String time, int type) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.type = type;
        this.isCompleted = false;
        this.weekStartDate = null;
    }

    // Конструктор для недельных задач
    @Ignore
    public Task(String title, String description, Date weekStartDate, int type) {
        this.title = title;
        this.description = description;
        this.date = weekStartDate; // для совместимости
        this.weekStartDate = weekStartDate;
        this.time = null;
        this.type = type;
        this.isCompleted = false;
    }

    public boolean isTimedTask() {
        return type == 1 && time != null;
    }

    public boolean isDailyTask() {
        return type == 0;
    }

    public boolean isWeeklyTask() {
        return type == 2;
    }
}