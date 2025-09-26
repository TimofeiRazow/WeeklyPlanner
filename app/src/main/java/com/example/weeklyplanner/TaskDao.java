package com.example.weeklyplanner;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.Date;
import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date BETWEEN :startDate AND :endDate ORDER BY date, time")
    LiveData<List<Task>> getTasksForWeek(Date startDate, Date endDate);

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startOfDay AND :endOfDay AND type = 1 ORDER BY time")
    LiveData<List<Task>> getTimedTasksForDay(Date startOfDay, Date endOfDay);

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startOfDay AND :endOfDay AND type = 0")
    LiveData<List<Task>> getDailyTasksForDay(Date startOfDay, Date endOfDay);

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startOfDay AND :endOfDay AND type = 0")
    LiveData<List<Task>> getTasksForDay(Date startOfDay, Date endOfDay);

    // Новый запрос для недельных задач
    @Query("SELECT * FROM tasks WHERE type = 2 AND weekStartDate = :weekStart ORDER BY title")
    LiveData<List<Task>> getWeeklyTasks(Date weekStart);

    // Все задачи недели включая недельные
    @Query("SELECT * FROM tasks WHERE " +
            "(date BETWEEN :startDate AND :endDate AND type IN (0, 1)) OR " +
            "(type = 2 AND weekStartDate BETWEEN :weekStart AND :weekEnd) " +
            "ORDER BY date, time, title")
    LiveData<List<Task>> getAllTasksForWeek(Date startDate, Date endDate, Date weekStart, Date weekEnd);

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteById(int taskId);
}